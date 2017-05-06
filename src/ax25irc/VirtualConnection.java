/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc;

import ax25irc.ircd.server.Connection;
import ax25irc.ircd.server.MessageListener;
import ax25irc.ircd.server.ServMessage;
import ax25irc.ircd.server.UserInfo;
import java.net.InetAddress;

/**
 *
 * @author alex
 */
public class VirtualConnection extends Connection {
    
    MessageListener listener;
    
    public VirtualConnection(String nick, String host) {
        this.m_nick = nick;
        this.m_host = InetAddress.getLoopbackAddress();
        this.m_user = new UserInfo(nick, host, nick, nick);
	this.listener = listener;
    }
    
    public void setListener(MessageListener listener) {
        this.listener = listener;
    }
    
    public void sendMsgAndFlush(ServMessage message) {
        
        if(listener!=null) {
            listener.onMessage(message);
        }
        
    }
    
    public void sendMsg(ServMessage message) {
        
        if(listener!=null) {
            listener.onMessage(message);
        }
    }
    
}
