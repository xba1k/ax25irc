package ax25irc;

import ax25irc.aprs.parser.APRSPacket;
import ax25irc.ax25modem.sivantoledo.ax25.Packet;

public class AX25APRSPacket extends AX25Packet {
    
    APRSPacket packet;
    Packet ax25frame;
    
    public AX25APRSPacket(Packet ax25frame, APRSPacket packet) {
        
        this.type = PacketType.APRS;
        this.packet = packet;
	this.ax25frame = ax25frame;
        
    }
    
    public APRSPacket getPacket() {
        return packet;
    }
    
    public Packet getAx25Frame() {
	return ax25frame;
    }
    
}
