/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc;

/**
 *
 * @author alex
 */
public interface AX25PacketListener {
    
    public void onPacket(AX25Packet packet);
    
}
