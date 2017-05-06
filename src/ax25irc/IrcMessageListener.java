/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc;

import ax25irc.ircd.server.ServMessage;

/**
 *
 * @author alex
 */
public interface IrcMessageListener {
    public void onMessage(ServMessage message);
}
