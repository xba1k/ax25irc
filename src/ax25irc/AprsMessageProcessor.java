package ax25irc;

import ax25irc.aprs.parser.APRSPacket;
import ax25irc.aprs.parser.APRSTypes;
import ax25irc.aprs.parser.Digipeater;
import ax25irc.aprs.parser.MessagePacket;
import ax25irc.aprs.parser.ObjectPacket;
import ax25irc.aprs.parser.PositionPacket;
import ax25irc.aprs.parser.Position;
import ax25irc.aprs.parser.SymbolDescription;
import ax25irc.ircd.server.Channel;
import ax25irc.ircd.server.Client;
import ax25irc.ircd.server.IRCServer;
import ax25irc.ircd.server.MessageListener;
import ax25irc.ircd.server.ServMessage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

public class AprsMessageProcessor implements MessageListener {

    IRCServer server;
    PacketModem modem;

    String targetCallsign = "APZ017";
    Digipeater groupDigis[] = new Digipeater[]{new Digipeater("WIDE1-1")};
    Digipeater directDigis[] = new Digipeater[]{new Digipeater("WIDE1-1")};

    double baseLatitude = 45.52;
    double baseLongitude = -122.681944;
    String baseLocationName = "Portland, OR";
    
    NumberFormat positionFormatter = new DecimalFormat("##0.00000");     

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

        if (message.getConnection() != null && !(message.getConnection() instanceof VirtualConnection)) {

            List<String> params = message.getParameters();

            if (message.getCommand().equals("PRIVMSG")) {

                String destination = params.get(0);
                String messagebody = message.getText();

                if (destination.startsWith("#")) {
                    destination = "A";
                }

                if (messagebody.startsWith("\001DCC SEND")) {
                    
                    if(server.getHost().getHostName().equalsIgnoreCase(destination)) {
                        destination = "A";
                    }
                    
                    DCCFileTransfer transfer = new DCCFileTransfer(modem, message.getConnection().getNick(), destination, message.getConnection().getIpAddr(), messagebody.substring(1, messagebody.length()-1));
                    transfer.start();

                } else {

                    MessagePacket mp = new MessagePacket(destination, messagebody, "");
                    APRSPacket packet = new APRSPacket(message.getConnection().getNick(), targetCallsign, destination.length() == 1 ? Arrays.asList(groupDigis) : Arrays.asList(directDigis), mp);
                    byte[] frame = packet.toAX25Frame();
                    modem.sendPacket(frame);

                }

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

            if (msg.getTargetCallsign().length() > 1) {

                Client tgtClient = server.getClient(msg.getTargetCallsign());

                if (tgtClient != null) {
                    tgtClient.sendMsgAndFlush(new ServMessage(ap.getSourceCall(), "PRIVMSG", msg.getTargetCallsign(), msg.getMessageBody()));

                    if (msg.getMessageNumber().length() > 0) {
                        System.out.println("Message requires ACK. Sending...");

                        MessagePacket ackMessage = new MessagePacket(ap.getSourceCall(), "ack", msg.getMessageNumber());
                        APRSPacket ackPacket = new APRSPacket(msg.getTargetCallsign(), targetCallsign, Arrays.asList(directDigis), ackMessage);
                        modem.sendPacket(ackPacket.toAX25Frame());

                    }

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

                Position pos = pp.getPosition();

                if (pos.getAltitude() > 0) {
                    message.append("altitude: " + pos.getAltitude() + " ");
                }

                message.append("lat: " + positionFormatter.format(pos.getLatitude()) + ", ");
                message.append("lon: " + positionFormatter.format(pos.getLongitude()) + ", ");
                message.append("sym: " + SymbolDescription.decode(pos.getSymbolTable(), pos.getSymbolCode()) + ", ");

                Position basePos = new Position(getBaseLatitude(), getBaseLongitude());
                message.append("distance from " + getBaseLocationName() + ": " + Math.round(basePos.distance(pos)) + "Mi, bearing: " + basePos.bearing(pos) + ", ");
                message.append("comment: " + pp.getComment());

            }

            break;
            case T_WX:
                message.append("weather ");
                message.append(p.toString());
                break;
            case T_THIRDPARTY:
                message.append("3rd party ");
                message.append(p.toString());

                // BALDPK>APRS:}N7TNG-1>APMI06,TCPIP,BALDPK*:@060639z4538.30N/12240.53W-WX3in1Plus2.0 U=12.5V,T=7.8C/46.0F
                // BALDPK>APRS:}WB7QAZ-10>APMI06,TCPIP,BALDPK*:@060637z4516.50N/12237.80W_271/000g000t046r000p015P015h97b10192Canby WX
                break;
            case T_QUERY:
                message.append("query ");
                message.append(p.toString());
                break;
            case T_OBJECT: {
                ObjectPacket obj = (ObjectPacket) p.getAprsInformation();
                message.append("object ");
                message.append(obj.getObjectName() + " ");

                Position pos = obj.getPosition();

                if (pos.getAltitude() > 0) {
                    message.append("altitude: " + pos.getAltitude() + " ");
                }

                message.append("lat: " + positionFormatter.format(pos.getLatitude()) + " ");
                message.append("lon: " + positionFormatter.format(pos.getLongitude()) + " ");
                message.append("sym: " + SymbolDescription.decode(pos.getSymbolTable(), pos.getSymbolCode()) + ", ");
 
                Position basePos = new Position(getBaseLatitude(), getBaseLongitude());
                message.append("distance from " + getBaseLocationName() + ": " + Math.round(basePos.distance(pos)) + "Mi, bearing: " + basePos.bearing(pos) + ", ");
                message.append("comment: " + obj.getComment());

            }

            break;
            case T_ITEM:
                message.append("item ");
                message.append(p.toString());
                break;
            case T_NORMAL:
                message.append("normal ");
                message.append(p.toString());
                break;
            case T_KILL:
                message.append("kill ");
                message.append(p.toString());
                break;
            case T_STATUS:
                message.append("status ");
                message.append(p.toString());
                break;
            case T_STATCAPA:
                message.append("statcapa ");
                message.append(p.toString());
                break;
            case T_TELEMETRY:
                message.append("telemetry ");
                message.append(p.toString());
                break;
            case T_USERDEF:
                message.append("userdef ");
                message.append(p.toString());
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
