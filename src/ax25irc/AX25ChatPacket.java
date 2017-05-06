package ax25irc;

import ax25irc.ax25modem.sivantoledo.ax25.Packet;

public class AX25ChatPacket extends AX25Packet {
    
    public AX25ChatPacket(Packet frame) {
        this.type = PacketType.AX25Chat;
        this.frame = frame;
    }
    
}
