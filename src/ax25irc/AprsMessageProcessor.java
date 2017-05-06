/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc;

import ax25irc.aprs.parser.APRSPacket;
import ax25irc.aprs.parser.APRSTypes;
import ax25irc.aprs.parser.Digipeater;
import ax25irc.aprs.parser.MessagePacket;
import ax25irc.aprs.parser.ObjectPacket;
import ax25irc.aprs.parser.Parser;
import ax25irc.aprs.parser.PositionPacket;
import ax25irc.aprs.parser.Position;
import ax25irc.aprs.parser.SymbolDescription;
import ax25irc.ircd.server.Channel;
import ax25irc.ircd.server.Client;
import ax25irc.ircd.server.IRCServer;
import ax25irc.ircd.server.MessageListener;
import ax25irc.ircd.server.ServMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author alex
 */
public class AprsMessageProcessor implements MessageListener {

    IRCServer server;
    PacketModem modem;
    
    String targetCallsign = "APZ017";
    Digipeater groupDigis[] = new Digipeater[] { new Digipeater("WIDE1-1") };
    Digipeater directDigis[] = new Digipeater[] { new Digipeater("WIDE1-1") };
    
    double baseLatitude = 45.52;
    double baseLongitude = -122.681944;
    String baseLocationName = "Portland, OR";

    public double getBaseLatitude() {
        return baseLatitude;
    }

    public double getBaseLongitude() {
        return baseLongitude;
    }

    public String getBaseLocationName() {
        return baseLocationName;
    }
    
    public AprsMessageProcessor(IRCServer server, PacketModem modem) {

	this.server = server;
        this.modem = modem;

    }
    
    public Digipeater[] getGroupDigis() {
        return groupDigis;
    }
    
    public Digipeater[] getDirectDigis() {
        return directDigis;
    }
    
    public void setGroupDigis(String digis) {
        groupDigis = Digipeater.parseList(digis, true).toArray(new Digipeater[0]);
    }
    
    public void setDirectDigis(String digis) {
        directDigis = Digipeater.parseList(digis, true).toArray(new Digipeater[0]);        
    }
    
    public void onMessage(ServMessage message) {

	if(message.getConnection()!=null && !(message.getConnection() instanceof VirtualConnection)) {
	    
	    List<String> params = message.getParameters();
	    
	    if(message.getCommand().equals("PRIVMSG")) {
		//System.out.println("New APRS Message from "+message.getConnection().getHostName()+" :"+message);
		
		String destination = params.get(0);
		String messagebody = message.getText();
		
		if(destination.startsWith("#")) {
		    destination = "A";
		} 
		
		MessagePacket mp = new MessagePacket(destination, messagebody, "");
                
            	APRSPacket packet = new APRSPacket( message.getConnection().getNick(), targetCallsign, destination.length()==1 ? Arrays.asList(groupDigis) : Arrays.asList(directDigis), mp);
                
                byte[] frame = packet.toAX25Frame();
                
                modem.sendPacket(frame);
/*                
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
                
                    System.out.println(Parser.parseAX25(frame).toString());
                    
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
*/		
	    } 
	    
	} 
	
    }

    public void process(AX25APRSPacket packet) {

	Channel aprsChannel = server.getChannel("#APRS");
	Channel aprsRawChannel = server.getChannel("#APRS-RAW");
	Channel aprsChatChannel = server.getChannel("#APRS-CHAT");

	APRSPacket ap = packet.getPacket();

	Client client = server.getClient(ap.getSourceCall());

	if (client == null) {

	    client = new Client(new VirtualConnection(ap.getSourceCall(), "APRS"));
	    client.getConnection().getUser().setRealName(ap.getAprsInformation().getComment());
            client.getConnection().setParentClient(client);

	    server.addClient(client);

	    aprsRawChannel.clientJoin(client);

	    if (ap.getType() == APRSTypes.T_MESSAGE) {
		aprsChatChannel.clientJoin(client);
	    } else {
		aprsChannel.clientJoin(client);
	    }

	} else {
	    //client.getConnection().getUser().setRealName(ap.getAprsInformation().getComment());
	}

	if (ap.getType() == APRSTypes.T_MESSAGE) {
	    MessagePacket msg = (MessagePacket) ap.getAprsInformation();
            
            if(msg.getMessageNumber().length()>0) {
                System.out.println("Message requires ACK. Sending...");
                
		MessagePacket ackMessage = new MessagePacket(ap.getSourceCall(), "ack", msg.getMessageNumber());
            	APRSPacket ackPacket = new APRSPacket(ap.getSourceCall(), targetCallsign, Arrays.asList(directDigis), ackMessage);
                modem.sendPacket(ackPacket.toAX25Frame());
                
            }
            
            if(msg.getTargetCallsign().length()>1) {
                
                Client tgtClient = server.getClient(msg.getTargetCallsign());
                
                if(tgtClient != null) {
                    tgtClient.sendMsgAndFlush(new ServMessage(ap.getSourceCall(), "PRIVMSG", msg.getTargetCallsign(), msg.getMessageBody()));
                } 
                
            } else {
            
                aprsChatChannel.sendMsg(new ServMessage(ap.getSourceCall(), "PRIVMSG", "#APRS-CHAT", msg.getMessageBody()));
                
            }
            
	} else {
	    aprsChannel.sendMsg(new ServMessage(ap.getSourceCall(), "PRIVMSG", "#APRS", packetToString(ap)));
	}
        
        client.updateLastActive();

	aprsChannel.sendMsg(new ServMessage(packet.getAx25Frame().source, "PRIVMSG", "#APRS-RAW", ap.toString()));

    }

    public String packetToString(APRSPacket p) {

	StringBuilder message = new StringBuilder();

	switch (p.getType()) {
	    case T_UNSPECIFIED:
		message.append("Unspecified format ");
		message.append(new String(p.toString()));
		break;
	    case T_POSITION: {

		PositionPacket pp = (PositionPacket) p.getAprsInformation();

		message.append("position ");
		message.append("source: " + pp.getPositionSource() + ", ");

		Position pos = pp.getPosition();

		if(pos.getAltitude() > 0) message.append("altitude: " + pos.getAltitude() + " ");
                
		message.append("lat: " + pos.getLatitude() + ", ");
		message.append("lon: " + pos.getLongitude() + ", ");
		message.append("sym: " + SymbolDescription.decode(pos.getSymbolTable(),pos.getSymbolCode()) + ", ");
		message.append("ts: " + pos.getTimestamp() + ", ");
		//message.append("ambiguity: " + pos.getPositionAmbiguity() + ", ");
                
                Position basePos = new Position(getBaseLatitude(), getBaseLongitude());
                message.append("distance from "+getBaseLocationName()+": "+Math.round(basePos.distance(pos))+"Mi, bearing: "+basePos.bearing(pos)+", ");
                
		message.append("comment: " + pp.getComment());

	    }

	    break;
	    case T_WX:
		message.append("weather ");
		message.append(new String(p.toString()));
		break;
	    case T_THIRDPARTY:
		message.append("3rd party ");
		message.append(new String(p.toString()));
		break;
	    case T_QUERY:
		message.append("query ");
		message.append(new String(p.toString()));
		break;
	    case T_OBJECT: {
		ObjectPacket obj = (ObjectPacket) p.getAprsInformation();
		message.append("object ");
		message.append(obj.getObjectName() + " ");

		Position pos = obj.getPosition();

		if(pos.getAltitude() > 0) message.append("altitude: " + pos.getAltitude() + " ");
                
		message.append("lat: " + pos.getLatitude() + " ");
		message.append("lon: " + pos.getLongitude() + " ");
		message.append("sym: " + SymbolDescription.decode(pos.getSymbolTable(),pos.getSymbolCode()) + ", ");
		message.append("ts: " + pos.getTimestamp() + " ");
                
                Position basePos = new Position(getBaseLatitude(), getBaseLongitude());
                message.append("distance from "+getBaseLocationName()+": "+Math.round(basePos.distance(pos))+"Mi, bearing: "+basePos.bearing(pos)+", ");

		message.append("comment: " + obj.getComment());

	    }

	    break;
	    case T_ITEM:
		message.append("item ");
		message.append(new String(p.toString()));
		break;
	    case T_NORMAL:
		message.append("normal ");
		message.append(new String(p.toString()));
		break;
	    case T_KILL:
		message.append("kill ");
		message.append(new String(p.toString()));
		break;
	    case T_STATUS:
		message.append("status ");
		message.append(new String(p.toString()));
		break;
	    case T_STATCAPA:
		message.append("statcapa ");
		message.append(new String(p.toString()));
		break;
	    case T_TELEMETRY:
		message.append("telemetry ");
		message.append(new String(p.toString()));
		break;
	    case T_USERDEF:
		message.append("userdef ");
		message.append(new String(p.toString()));
		break;
	    case T_MESSAGE:
		MessagePacket msg = (MessagePacket) p.getAprsInformation();

		message.append("message ");
		message.append("to: " + msg.getTargetCallsign() + ", ");
		message.append("num: " + msg.getMessageNumber() + ", ");
		message.append("body: " + msg.getMessageBody());

		break;
	    case T_NWS:
		message.append("nws ");
		message.append(new String(p.toString()));
		break;
	}

	return message.toString();

    }

}
