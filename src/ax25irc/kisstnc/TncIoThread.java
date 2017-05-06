/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc.kisstnc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amishhammer
 */
public class TncIoThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(TncIoThread.class.getName());
    private final KissTnc tnc;
    private final InputStream iStream;
    private final OutputStream oStream;
    public TncIoThread(KissTnc tnc) {
        this.tnc = tnc;
        this.iStream = tnc.getInputStream();
        this.oStream = tnc.getOutputStream();
    }
    @Override
    public void run() {
        KissFrame frame;
        boolean needResponse = false;
        KissFrameListener responseHandler = null;
        while (!this.isInterrupted()) {            
            // Look see if there is a frame to send
            frame = tnc.getNextTxFrame();
            
            if (frame != null) {
                LOGGER.log(Level.FINEST, "Sending frame: 0xC0 {0} {1} 0xC0", new Object[] {frame.getType().toHexString(), frame.toHexString()});
                needResponse = frame.expectResponse();
                responseHandler = frame.getResponseHandler();
                try {
                    frame.writeFramedBytes(oStream);
                } catch (IOException ex) {
                    Logger.getLogger(TncIoThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
            try {
                // Try to read a frame.
                frame = readFrame();
            } catch (IOException ex) {
                Logger.getLogger(TncIoThread.class.getName()).log(Level.SEVERE, null, ex);
            } 
            
            if (frame != null) {
                LOGGER.finest("Recieved frame: "+frame.toString());
                if (needResponse) {
                    LOGGER.finest("Sent frame expected a response, dispatching");
                    responseHandler.frameReceived(frame);
                    needResponse = false;
                    responseHandler = null;
                } else {
                    tnc.dispatchFrame(frame);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(TncIoThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private final byte[] buffer = new byte[1000];
    private boolean frameStarted = false;
    private int bufferPos = 0;
    
    private KissFrame readFrame() throws IOException {
        KissFrame ret = null;
        byte[] buf = new byte[1000];
        int avail = iStream.available();
        if (avail == 0) {
            return null;
        }
        int read = iStream.read(buf,0, avail);
        
        if (read == 0) {
            return null;
        }
        LOGGER.finest("Read bytes from TNC: "+read);

        
        
        int pos = 0;
        while (pos < read) {
            
            if (frameStarted) {
                for (; pos < read; pos++) {
                    byte b = buf[pos];
                    buffer[bufferPos++]=b;
                    if (b == KissFrame.FEND) {
                        byte[] frameData = Arrays.copyOf(buffer, bufferPos);
                        ret = new KissFrame(frameData);
                        frameStarted = false;
                        bufferPos = 0;
                        pos++;
                        break;
                    }
                }
            } else {
                for (; pos < read; pos++) {
                    byte b = buf[pos];

                    if (b == KissFrame.FEND) {
                        buffer[bufferPos++] = b;
                        frameStarted = true;
                        pos++;
                        break;
                    }
                }
            }
        }

        return ret;
    }
    
    
}
