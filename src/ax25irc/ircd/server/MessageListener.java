/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc.ircd.server;

import ax25irc.ircd.server.ServMessage;

/**
 *
 * @author alex
 */
public interface MessageListener {
    
    public void onMessage(ServMessage message);
    
}
