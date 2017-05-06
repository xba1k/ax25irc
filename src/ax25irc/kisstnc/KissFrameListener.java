/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc.kisstnc;

import java.util.EventListener;

/**
 *
 * @author amishhammer
 */
public interface KissFrameListener extends EventListener {
    public void frameReceived(KissFrame frame);
}
