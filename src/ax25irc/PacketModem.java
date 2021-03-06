package ax25irc;

import ax25irc.aprs.parser.APRSPacket;
import ax25irc.aprs.parser.Parser;
import ax25irc.ax25modem.sivantoledo.ax25.Packet;
import ax25irc.ax25modem.sivantoledo.ax25.PacketHandler;

public class PacketModem extends Thread implements PacketHandler {

    AX25PacketListener listener;

    public PacketModem(AX25PacketListener listener) {
        this.listener = listener;
    }

    @Override
    public void handlePacket(byte[] packet) {
        
        Packet pkt = new Packet(packet);
        pkt.parse();
        
        if (pkt.payload[0] == '^') {

            switch (pkt.payload[1]) {

                case ' ':
                    listener.onPacket(new AX25ChatPacket(pkt));
                    break;
                case '1':
                case '2':
                    listener.onPacket(new AX25FileTransfer(pkt));
                    break;

            }

        } else {

            APRSPacket pkt2;

            try {

                pkt2 = Parser.parseAX25(packet);
                listener.onPacket(new AX25APRSPacket(pkt, pkt2));

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    public void sendPacket(byte[] packet) {

        for (int i = 0; i < packet.length; i++) {

            int c = packet[i] & 255;

            if (c >= 32 && c <= 126) {
                System.out.print((char) c);
            } else {
                System.out.print("<" + c + ">");
            }

        }

        System.out.println();

    }

}
