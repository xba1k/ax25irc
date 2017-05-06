package ax25irc;

import ax25irc.aprs.parser.APRSTypes;
import ax25irc.aprs.parser.Parser;
import ax25irc.ax25modem.sivantoledo.ax25.Packet;
import ax25irc.ircd.server.Channel;
import ax25irc.ircd.server.Client;
import ax25irc.ircd.server.IRCServer;
import ax25irc.ircd.server.MessageListener;
import ax25irc.ircd.server.ServMessage;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author alex
 */
public class AX25MessageProcessor implements MessageListener {

    IRCServer server;
    PacketModem modem;

    public AX25MessageProcessor(IRCServer server, PacketModem modem) {

	this.server = server;
        this.modem = modem;

    }

    public void process(AX25ChatPacket packet) {
        
        Packet frame = packet.getFrame();
        Channel ax25ChatChannel = server.getChannel("#AX25-CHAT");
        
        Client client = server.getClient(frame.source);
        
	if (client == null) {

	    client = new Client(new VirtualConnection(frame.source, "AX25"));
            client.getConnection().setParentClient(client);

	    server.addClient(client);

	    ax25ChatChannel.clientJoin(client);

	}
        
        client.updateLastActive();
        
        ax25ChatChannel.sendMsg(new ServMessage(frame.source, "PRIVMSG", frame.destination.length() > 1 ? frame.destination : "#AX25-CHAT", new String(frame.payload).substring(2)));

    }

    public void onMessage(ServMessage message) {

	if (message.getConnection() != null && !(message.getConnection() instanceof VirtualConnection)) {
	    List<String> params = message.getParameters();

	    if(message.getCommand().equals("PRIVMSG")) {
		//System.out.println("New AX25CHAT Message from " + message.getConnection().getHostName() + ": " + message);
                
		String destination = params.get(0);
		String messagebody = message.getText();
		
		if(destination.startsWith("#")) {
		    destination = "*";
		}
                
                String header = "^ ";

                Packet packet = new Packet(destination, message.getConnection().getNick(), new String[] {}, Packet.AX25_CONTROL_APRS, Packet.AX25_PROTOCOL_NO_LAYER_3, (header+messagebody).getBytes());
                
                modem.sendPacket(packet.bytesWithCRC());
/*                
                byte[] frame = packet.bytesWithoutCRC();
                
                System.out.println(packet);
                
                for(int i = 0; i < frame.length; i++) {
                    
                    int c = frame[i] & 255;
                    
                    if(c >= 32 && c <= 126) {
                        System.out.print((char)c);
                    } else {
                        System.out.print("<"+c+">");
                    }
                    
                }
                
                System.out.println("");
                
                try {
                
                    System.out.println(new Packet(frame).toString());
                    
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                
  */              
	    }
	}

    }

}
