package ax25irc.ircd.server;

import ax25irc.ircd.util.CaseIMap;
import ax25irc.ircd.util.Macros;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Client {

    private Connection m_connection;
    private Map<String, Channel> m_channels;
    private int m_pingtimer;
    private long m_lastactive;

    private List<MessageListener> listeners;

    public Client(Connection connection) {
        m_connection = connection;
        m_channels = Collections.synchronizedMap(new CaseIMap<>());
        m_pingtimer = 100;
        
        listeners = new ArrayList<>();
    }

    public void addListener(MessageListener listener) {
        this.listeners.add(listener);
    }

    public void updateLastActive() {

        m_lastactive = System.currentTimeMillis();

    }

    public void updateIdentifiedClient() {
        /* Read input from client */
        List<String> input_data = new ArrayList<String>();

        try {
            String line = null;
            while (m_connection.getInput().ready() && (line = m_connection.getInput().readLine()) != null && !line.isEmpty()) {
                input_data.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Return if input data was empty */
        if (input_data.isEmpty()) {
            return;
        }

        /* Log the input to console */
        //Macros.LOG("Input from %s: %s", m_connection, input_data);

        /* Parse input and handle it appropriately */
        for (String l : input_data) {
            CliMessage message = new CliMessage(l);
            String privmsg = (l.split("\\s+", 3).length >= 3) ? l.split("\\s+", 3)[2] : null;
            String prefix = message.getPrefix();
            String command = message.getCommand();
            List<String> params = message.getParameters();

            switch (command) {
                case "PONG":
                    m_pingtimer = 100;
                    break;
                case "JOIN":
                    if (!params.isEmpty()) {
                        for (String p : params) {
                            Channel channel = m_connection.getIRCServer().getChannel(p);

                            if (channel == null) {
                                channel = new Channel(m_connection.getIRCServer(), p, "", "", 0);
                                m_connection.getIRCServer().getChannels().put(p, channel);
                            }

                            channel.clientJoin(this);
                        }
                    } else {
                        m_connection.sendMsgAndFlush(new ServMessage(m_connection, CMDs.ERR_NEEDMOREPARAMS, command, "Not enough parameters."));
                    }
                    break;
                case "PART":
                    if (!params.isEmpty()) {
                        for (String p : params) {
                            Channel channel = m_connection.getIRCServer().getChannel(p);

                            if (channel != null) {
                                channel.clientPart(this, "For an unknown reason.");
                            }
                        }
                    } else {
                        m_connection.sendMsgAndFlush(new ServMessage(m_connection, CMDs.ERR_NEEDMOREPARAMS, command, "Not enough parameters."));
                    }
                    break;
                case "PRIVMSG":
                case "NOTICE":
                    if (!params.isEmpty() || privmsg == null) {

                        ServMessage servMessage = new ServMessage(m_connection, command, params.get(0), privmsg);

                        if (params.get(0).startsWith("#")) {
                            Channel channel = m_channels.get(params.get(0));

                            if (channel != null) {
                                channel.sendMsgAndFlush(this, servMessage);
                            }
                            
                        } else {
                            
                            Client client = m_connection.getIRCServer().getClient(params.get(0));

                            if (client != null) {
                                client.getConnection().sendMsgAndFlush(servMessage);
                            }
                            
                        }
                        
                        for (MessageListener listener : listeners) {
                            listener.onMessage(servMessage);
                        }
                        
                    } else {
                        m_connection.sendMsgAndFlush(new ServMessage(m_connection, CMDs.ERR_NEEDMOREPARAMS, command, "Not enough parameters."));
                    }
                    break;
                case "WHOIS":
                    if (!params.isEmpty()) {
                        for (String p : params) {
                            Client client = m_connection.getIRCServer().getClient(p);

                            if (client != null) {
                                Connection c = client.getConnection();
                                UserInfo u = c.getUser();
                                m_connection.sendMsg(new ServMessage(m_connection.getIRCServer(), CMDs.RPL_WHOISUSER, m_connection.getNick(), c.getNick(), u.getUserName(), u.getHostName(), "*", u.getRealName() + " "));
                                m_connection.sendMsgAndFlush(new ServMessage(m_connection.getIRCServer(), CMDs.RPL_ENDOFWHOIS, m_connection.getNick(), c.getNick(), "End of /WHOIS list."));
                            } else {
                                m_connection.sendMsgAndFlush(new ServMessage(m_connection.getIRCServer(), CMDs.ERR_NOSUCHNICK, m_connection.getNick(), p, "No such nick."));
                            }
                        }
                    }
                    break;
                case "LIST": {

                    Map<String, Channel> channels = m_connection.getIRCServer().getChannels();

                    for (Channel channel : channels.values()) {
                        m_connection.sendMsg(new ServMessage(m_connection.getIRCServer(), CMDs.RPL_LIST, m_connection.getNick(), channel.getName(), String.valueOf(channel.getClients().size()), ":" + channel.getTopic()));
                    }

                    m_connection.sendMsgAndFlush(new ServMessage(m_connection.getIRCServer(), CMDs.RPL_LISTEND, m_connection.getNick(), "End of /LIST"));

                }

                case "TOPIC":
                    if (!params.isEmpty()) {
                        Channel channel = m_connection.getIRCServer().getChannel(params.get(0));
                        String topic = privmsg;

                        if (channel != null && !topic.isEmpty()) {
                            channel.setTopic(this, topic);
                        }
                    }
                    break;
                case "QUIT":
                    quitChans(message.getParameter(0));
                    m_connection.setState(ConnState.DISCONNECTED);
                    break;
            }
        }
    }

    public void sendMsg(ServMessage message) {
        m_connection.sendMsg(message);
    }

    public void sendMsgAndFlush(ServMessage message) {
        m_connection.sendMsgAndFlush(message);
    }

    public void flush() {
        m_connection.flush();
    }

    public void sendMsgToChans(ServMessage message) {
        Iterator<Entry<String, Channel>> i = m_channels.entrySet().iterator();

        while (i.hasNext()) {
            Channel c = (Channel) i.next();
            c.sendMsg(message);
        }
    }

    public void sendMsgToChansAndFlush(ServMessage message) {
        Iterator<Entry<String, Channel>> i = m_channels.entrySet().iterator();

        while (i.hasNext()) {
            Channel c = (Channel) i.next();
            c.sendMsgAndFlush(message);
        }
    }

    public void quitChans(String reason) {
        Iterator<Entry<String, Channel>> i = m_channels.entrySet().iterator();

        while (i.hasNext()) {
            Entry<String, Channel> e = i.next();
            Channel channel = (Channel) e.getValue();
            channel.clientQuit(this, reason);
        }
    }

    public void quitChan(Channel channel, String reason) {
        if (m_channels.get(channel.getName()) != null) {
            channel.clientQuit(this, reason);
        }
    }

    public void addChan(Channel channel) {
        m_channels.put(channel.getName(), channel);
    }

    public void removeChans() {
        m_channels.clear();
    }

    public void removeChan(Channel channel) {
        m_channels.remove(channel.getName());
    }

    public void setPingTimer(int pingtimer) {
        m_pingtimer = pingtimer;
    }

    public Connection getConnection() {
        return m_connection;
    }

    public Map<String, Channel> getChannels() {
        return m_channels;
    }

    public Channel getChannel(String key) {
        return m_channels.get(key);
    }

    public int getPingTimer() {
        return m_pingtimer;
    }

    public long getLastActive() {
        return m_lastactive;
    }

    public void setLastActive(long active) {
        this.m_lastactive = active;
    }

}
