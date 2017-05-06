package ax25irc.ircd.server;

import ax25irc.ircd.util.CaseIMap;
import ax25irc.ircd.util.Consts;
import ax25irc.ircd.util.FileUtils;
import ax25irc.ircd.util.IniFile;
import ax25irc.ircd.util.Macros;
import ax25irc.ircd.util.VarMap;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class IRCServer extends VarMap implements Runnable {

    private InetAddress m_host;
    private InetAddress m_ip;
    private int m_port;
    private ServerSocket m_socket;
    private Map<String, List<Connection>> m_connections;
    private Map<String, Client> m_clients;
    private Map<String, Server> m_servers;
    private Map<String, Channel> m_channels;
    private IniFile m_inifile;
    private List<String> m_motd;
    private int m_deltaTime;

    private List<MessageListener> messageListeners;
    private List<ClientConnectionListener> clientConnectionListeners;

    public IRCServer(String ip, String port) throws NumberFormatException, IOException {
	m_host = InetAddress.getLocalHost();
	m_ip = InetAddress.getByName(ip);
	m_port = Integer.parseInt(port);
	m_socket = new ServerSocket(m_port, 1000, m_ip);
	m_socket.setSoTimeout(0);
	m_connections = Collections.synchronizedMap(new CaseIMap<>());
	m_clients = Collections.synchronizedMap(new CaseIMap<>());
	m_servers = Collections.synchronizedMap(new CaseIMap<>());
	m_channels = Collections.synchronizedMap(new CaseIMap<>());
	m_inifile = new IniFile("main.ini");
	m_motd = new ArrayList<String>();
	m_deltaTime = 0;

	putString("sName", m_inifile.getString("[server]", "name", "mirage-ircd"));
	putString("sMOTD", m_inifile.getString("[server]", "motd", "motd.txt"));
	putInteger("sMaxConns", m_inifile.getInt("[server]", "maxconns", 1028));
	putInteger("cMaxConns", m_inifile.getInt("[client]", "maxconns", 10));
	putInteger("cPingTime", m_inifile.getInt("[client]", "pingtime", 600));
	putInteger("cIdentTime", m_inifile.getInt("[client]", "identtime", 300));

	m_motd = FileUtils.loadTextFile(getString("sMOTD"), false);

	if (m_motd == null) {
	    m_motd = new ArrayList<String>();
	    m_motd.add("No message of the day set on this server. Please refer to the main.ini file for loading it.");
	}

	putString("sCreationDate", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
	
	messageListeners = new ArrayList<>();
        clientConnectionListeners = new ArrayList<>();
	
    }
    
    public void addMessageListener(MessageListener listener) {
	messageListeners.add(listener);
    }
    
    public void addClientConnectionListener(ClientConnectionListener listener) {
	clientConnectionListeners.add(listener);
    }

    public void addChannel(Channel channel) {
	m_channels.put(channel.getKey(), channel);
    }

    public void addChannel(String key, Channel channel) {
	m_channels.put(key, channel);
    }

    public void addClient(Client client) {
	m_clients.put(client.getConnection().getNick(), client);
    }
    
    public void removeClient(Client client) {
        m_clients.remove(client.getConnection().getNick());
    }

    @Override
    public void run() {
	while (m_socket.isBound() && !m_socket.isClosed()) {
	    try {
		/* Accept connection, create i/o, create connection object */
		Socket socket = m_socket.accept();
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
		Connection connection = new Connection(this, socket, input, output);

		/* Check if max connections per server limit has been reached */
		if (m_connections.size() >= getInteger("sMaxConns")) {
		    connection.sendMsgAndFlush(new ServMessage(this, "NOTICE", connection.getNick(), "*** Server connection limit reached. " + m_connections.size() + "/" + getInteger("sMaxConns")));
		    connection.kill();
		    Macros.LOG("Too many connections on the server, " + connection + " disconnected.");
		    continue;
		}

		/* Look up the hostname or ip, depending which one is available */
		connection.sendMsgAndFlush(new ServMessage(this, "NOTICE", connection.getNick(), "*** Connection accepted. Looking up your hostname..."));
		String key = connection.getHost().getHostName();
		connection.sendMsgAndFlush(new ServMessage(this, "NOTICE", connection.getNick(), "*** Found your hostname."));

		/* Check if connections from same host already exist, check if max limit per hostname has been reached */
		if (m_connections.containsKey(key)) {
		    if (m_connections.get(key).size() >= getInteger("cMaxConns")) {
			connection.sendMsgAndFlush(new ServMessage(this, "NOTICE", connection.getNick(), "*** Sorry, but your ip exceeds max connections per ip."));
			connection.kill();
			Macros.LOG("Too many connections from " + connection + ", disconnecting...");
			continue;
		    }
		} else {
		    List<Connection> connections = new ArrayList<Connection>();
		    m_connections.put(key, connections);
		}

		/* Add the accepted connection to the connections hashmap-list */
		m_connections.get(key).add(connection);
		Macros.LOG("New incoming " + connection + ".");

	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    public void processConnections() {
	Iterator<Entry<String, List<Connection>>> it_con_map = getConnections().entrySet().iterator();

	while (it_con_map.hasNext()) {
	    Entry<String, List<Connection>> entry_map = it_con_map.next();
	    List<Connection> con_list = (ArrayList<Connection>) entry_map.getValue();
	    Iterator<Connection> it_con_list = con_list.iterator();

	    while (it_con_list.hasNext()) {
		Connection c = it_con_list.next();

		/* First check, if the socket was closed for some reason */
		if (c.getSocket().isClosed() || c.getOutput().checkError()) {
		    c.setState(ConnState.DISCONNECTED);
		}

		/* Handle unidentified connections */
		if (c.getState() == ConnState.UNIDENTIFIED) {
		    if (c.getIdentTime() == -1) {
			c.sendMsgAndFlush(new ServMessage(this, "NOTICE", c.getNick(), "*** Checking ident..."));
		    }

		    c.updateUnidentified();

		    /* Wait for x seconds before disconnecting the connection */
		    if (c.getIdentTime() > getInteger("cIdentTime")) {
			c.sendMsgAndFlush(new ServMessage(this, "NOTICE", c.getNick(), "*** Failed to identify the connection, disconnected."));
			c.setState(ConnState.DISCONNECTED);
		    }

		    c.setIdentTime(c.getIdentTime() + 1);
		}

		/* Handle identified client connections */
		if (c.getState() == ConnState.IDENTIFIED_AS_CLIENT) {
		    /* Add the connection as a client and inform them for the success */
		    c.sendMsgAndFlush(new ServMessage(this, "NOTICE", c.getNick(), "*** Found your ident, identified as a client."));
		    c.setState(ConnState.CONNECTED_AS_CLIENT);
		    c.getUser().setHostName(c.getHost().getHostName());
		    Client client = new Client(c);
		    c.setParentClient(client);
		    m_clients.put(c.getNick(), client);
                    
		    /* Send some info about the server */
		    c.sendMsg(new ServMessage(this, "001", c.getNick(), "Welcome to the " + getString("sName") + " IRC network, " + c.getNick()));
		    c.sendMsg(new ServMessage(this, "002", c.getNick(), "Your host is " + getHost().getHostName() + ", running version mirage-ircd-" + Consts.VERSION));
		    c.sendMsg(new ServMessage(this, "003", c.getNick(), "This server was created on " + getString("sCreationDate")));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSERCLIENT, c.getNick(), "There are " + m_connections.size() + " users and 0 invisible on 1 server."));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSEROP, c.getNick(), "0", "IRC Operators online."));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSERUNKNOWN, c.getNick(), "0", "Unknown connections."));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSERCHANNELS, c.getNick(), Integer.toString(m_channels.size()), "Channels formed."));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSERME, c.getNick(), "I have " + m_clients.size() + " clients and " + m_servers.size() + " servers."));

		    /* Send MOTD to the client */
		    c.sendMsg(new ServMessage(this, CMDs.RPL_MOTDSTART, c.getNick(), "- Message of the day -"));

		    for (String s : m_motd) {
			c.sendMsg(new ServMessage(this, CMDs.RPL_MOTD, c.getNick(), "- " + s));
		    }
                    
                    for(ClientConnectionListener listener : clientConnectionListeners) {
                        listener.onClient(client);
                    }

		    c.sendMsgAndFlush(new ServMessage(this, CMDs.RPL_ENDOFMOTD, c.getNick(), "End of /MOTD command."));
		} /* Handle identified server connections */ else if (c.getState() == ConnState.IDENTIFIED_AS_SERVER) {
		    /* Add the connection as a server and inform them for the success */
		    c.sendMsgAndFlush(new ServMessage(this, "NOTICE", c.getServer().getName(), "*** Found your ident, identified as a server."));
		    c.setState(ConnState.CONNECTED_AS_SERVER);
		    c.getUser().setHostName(c.getHost().getHostName());
		    Server server = new Server(c);
		    c.setParentServer(server);
		    m_servers.put(c.getServer().getName(), server);

		    /* Send some info about the server */
		    c.sendMsg(new ServMessage(this, "001", c.getServer().getName(), "Welcome to the " + getString("sName") + " IRC network, " + c.getServer().getName()));
		    c.sendMsg(new ServMessage(this, "002", c.getServer().getName(), "Your host is " + getHost().getHostName() + ", running version mirage-ircd-" + Consts.VERSION));
		    c.sendMsg(new ServMessage(this, "003", c.getServer().getName(), "This server was created on " + getString("sCreationDate")));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSERCLIENT, c.getServer().getName(), "There are " + m_connections.size() + " users and 0 invisible on 1 server."));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSEROP, c.getServer().getName(), "0", "IRC Operators online."));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSERUNKNOWN, c.getServer().getName(), "0", "Unknown connections."));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSERCHANNELS, c.getServer().getName(), Integer.toString(m_channels.size()), "Channels formed."));
		    c.sendMsg(new ServMessage(this, CMDs.RPL_LUSERME, c.getServer().getName(), "I have " + m_clients.size() + " clients and " + m_servers.size() + " servers."));

		    /* Send MOTD to the server */
		    c.sendMsg(new ServMessage(this, CMDs.RPL_MOTDSTART, c.getServer().getName(), "- Message of the day -"));

		    for (String s : m_motd) {
			c.sendMsg(new ServMessage(this, CMDs.RPL_MOTD, c.getServer().getName(), "- " + s));
		    }

		    c.sendMsgAndFlush(new ServMessage(this, CMDs.RPL_ENDOFMOTD, c.getServer().getName(), "End of /MOTD command."));
		}

		/* Handle connected client connections */
		if (c.getState() == ConnState.CONNECTED_AS_CLIENT) {
		    Client client = c.getParentClient();

		    /* Send a PING request between intervals */
		    int pingtime = getInteger("cPingTime");
		    if (client.getPingTimer() >= pingtime && client.getPingTimer() % (pingtime / 10) == 0) {
			c.sendMsgAndFlush(new ServMessage("", "PING", c.getNick()));
		    }

		    client.updateIdentifiedClient();

		    /* Disconnect if it didn't respond to the PING request given enough time */
		    if (client.getPingTimer() > (int) (pingtime * 1.5)) {
			c.setState(ConnState.DISCONNECTED);
		    }

		    client.setPingTimer(client.getPingTimer() + 1);

		} /* Handle connected server connections */ else if (c.getState() == ConnState.CONNECTED_AS_SERVER) {
		    c.getParentServer().updateIdentifiedServer();
		}

		/* Unregister the connection if it was closed */
		if (c.getState() == ConnState.DISCONNECTED) {
		    Client client = c.getParentClient();
		    Server server = c.getParentServer();

		    /* Is it a client? */
		    if (client != null) {
			client.quitChans("Connection reset by peer...");
			m_clients.remove(c.getNick());
		    }

		    /* Is it a server? Should't be both. */
		    if (server != null) {
			m_servers.remove(c.getServer().getName());
		    }

		    c.kill();
		    it_con_list.remove();
		    Macros.LOG(c + " Has disconnected.");
		}
	    }

	    /* Remove the key from connection list map if the list is empty */
	    if (con_list.isEmpty()) {
		it_con_map.remove();
	    }
	}
    }

    public void removeEmptyChannels() {
	Iterator<Entry<String, Channel>> it_chan_map = m_channels.entrySet().iterator();

	while (it_chan_map.hasNext()) {
	    Entry<String, Channel> e = it_chan_map.next();
	    Channel c = (Channel) e.getValue();

	    /* Delete empty channels */
	    if (c.getState() == ChanState.EMPTY) {
		it_chan_map.remove();
	    }
	}
    }

    public void setDeltaTime(int deltaTime) {
	m_deltaTime = deltaTime;
    }

    public InetAddress getHost() {
	return m_host;
    }

    public InetAddress getIp() {
	return m_ip;
    }

    public int getPort() {
	return m_port;
    }

    public ServerSocket getSocket() {
	return m_socket;
    }

    public synchronized Map<String, List<Connection>> getConnections() {
	return m_connections;
    }

    public synchronized List<Connection> getConnection(String key) {
	return m_connections.get(key);
    }

    public synchronized Map<String, Client> getClients() {
	return m_clients;
    }

    public synchronized Client getClient(String key) {
	return m_clients.get(key);
    }

    public synchronized Map<String, Server> getServers() {
	return m_servers;
    }

    public synchronized Server getServer(String key) {
	return m_servers.get(key);
    }

    public synchronized Map<String, Channel> getChannels() {
	return m_channels;
    }

    public synchronized Channel getChannel(String key) {
	return m_channels.get(key);
    }

    public List<String> getMotd() {
	return m_motd;
    }

    public int getDeltaTime() {
	return m_deltaTime;
    }

}
