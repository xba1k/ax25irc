package ax25irc;

import ax25irc.ax25modem.sivantoledo.ax25.Packet;
import ax25irc.ircd.server.Channel;
import ax25irc.ircd.server.Client;
import ax25irc.ircd.server.IRCServer;
import ax25irc.ircd.server.MessageListener;
import ax25irc.ircd.server.ServMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AX25MessageProcessor implements MessageListener {

    Map<String, DCCFileTransfer> fileTransfers;

    IRCServer server;
    PacketModem modem;

    public AX25MessageProcessor(IRCServer server, PacketModem modem) {

        this.server = server;
        this.modem = modem;

        fileTransfers = new HashMap<>();

    }

    public void process(AX25Packet packet) {

        Packet frame = packet.getFrame();

        if (packet instanceof AX25ChatPacket) {

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

        } else if (packet instanceof AX25FileTransfer) {

            DCCFileTransfer transfer = fileTransfers.get(frame.source);
            
            if(transfer != null && ((AX25FileTransfer)packet).getState() == AX25FileTransfer.TransferState.HEADER) {

                System.out.println("Received header while in data mode, resetting previous transfer");
                
                fileTransfers.remove(frame.source);
                transfer.interrupt();
                transfer = null;
                
            }

            if (transfer == null) {

                Client client = server.getClient(frame.destination);
                
                if(client != null) {

                    transfer = new DCCFileTransfer(server, frame.source, frame.destination, ((AX25FileTransfer) packet).frame);
                    fileTransfers.put(frame.source, transfer);
                    transfer.start();

                    client.sendMsg(new ServMessage(frame.source, "PRIVMSG", frame.destination, "\001DCC SEND \"" + transfer.getFilename() + "\" " + transfer.getIp() + " " + transfer.getPort() + " " + transfer.getSize()+"\001"));
                
                } else {
                    
                    System.out.println("Ignoring file transfer to uknown client "+frame.destination);
                    
                }
                
            } else {

                if (transfer.isDone()) {
                    fileTransfers.remove(frame.source);
                    transfer = null;
                } else {
                    transfer.addPacket(frame);
                }
            }

        }

    }

    @Override
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

                    DCCFileTransfer transfer = new DCCFileTransfer(modem, message.getConnection().getNick(), destination, message.getConnection().getIpAddr(), messagebody.substring(1, messagebody.length() - 1));
                    transfer.start();

                } else {

                    String header = "^ ";
                    Packet packet = new Packet(destination, message.getConnection().getNick(), new String[]{}, Packet.AX25_CONTROL_APRS, Packet.AX25_PROTOCOL_NO_LAYER_3, (header + messagebody).getBytes());
                    modem.sendPacket(packet.bytesWithoutCRC());

                }

            }
        }

    }

}
