/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc;

import ax25irc.ax25modem.sivantoledo.ax25.Packet;

/**
 *
 * @author alex
 */
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
