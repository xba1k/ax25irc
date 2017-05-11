package ax25irc;

import ax25irc.ax25modem.sivantoledo.ax25.Packet;

public class AX25Packet {
    
    Packet frame;
    
    enum PacketType {
        
        OTHER,
        APRS,
        AX25Chat
        
    };

    PacketType type;
    
    public PacketType getType() {
        return type;
    }
    
    public Packet getFrame() {
        return frame;
    }
    
}
