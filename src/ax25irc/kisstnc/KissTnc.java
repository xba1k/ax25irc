/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc.kisstnc;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 *
 * @author amishhammer
 */
public class KissTnc {
    private TransferQueue<KissFrame> sendQueue;
    private List<KissFrameListener> listeners;
    private InputStream iStream;
    private OutputStream oStream;
    private TncIoThread ioThread;
    
    private static final byte DEFAULT_TX_DELAY = 40;
    private static final byte DEFAULT_PERSISTENCE = 63;
    private static final byte DEFAULT_SLOT_TIME = 20;
    private static final byte DEFAULT_TX_TAIL = 30;
    private static final byte DEFAULT_FULL_DUPLEX = 0;
    
    
    public KissTnc(InputStream iStream, OutputStream oStream) {
        this.sendQueue = new LinkedTransferQueue<KissFrame>();
        this.listeners = new ArrayList<KissFrameListener>();
        this.iStream = iStream;
        this.oStream = oStream;
        this.ioThread = new TncIoThread(this);
        this.setFullDuplex(DEFAULT_FULL_DUPLEX);
        this.setTxDelay(DEFAULT_TX_DELAY);
        this.setPersistence(DEFAULT_PERSISTENCE);
        this.setSlotTime(DEFAULT_SLOT_TIME);
        this.setTxTail(DEFAULT_TX_TAIL);
    }
    
    /** Start processing data from the TNC
     * 
     */
    public void start() {
        this.ioThread.start();
    }
    /** Send a frame through the TNC
     * 
     * @param frame The frame to send
     */
    public void sendFrame(KissFrame frame) {
        if (frame == null) {
            throw new NullPointerException("Frame may not be null");
        }
        sendQueue.add(frame);
    }
    /** Get the next frame to be transmitted
     * 
     * @return Next frame to be transmitted or NULL if there is no data waiting
     */
    protected KissFrame getNextTxFrame() {
        return sendQueue.poll();
    }
    
    /** Register an event listener for received frames.
     * 
     * @param listener Frame listener
     */
    public void registerFrameListener(KissFrameListener listener) {
        if (listener == null) {
            throw new NullPointerException("Listener is not allowed to be null");
        }
        listeners.add(listener);
    }
    /** Dispatch a received frame to the listeners
     * 
     * @param frame Received frame
     */
    protected void dispatchFrame(KissFrame frame) {
        for (KissFrameListener l : listeners) {
            l.frameReceived(frame);
        }
    }
    
    protected InputStream getInputStream() {
        return iStream;
    }
    protected OutputStream getOutputStream() {
        return oStream;
    }

    public final void setTxDelay(byte delay) {
        writeSetting(KissFrame.FrameType.TX_DELAY, new byte[] { delay });
    }
    public final void setPersistence(byte persistence) {
        writeSetting(KissFrame.FrameType.PERSISTENCE, new byte[] { persistence });
    }
    public final void setSlotTime(byte slotTime) {
        writeSetting(KissFrame.FrameType.SLOT_TIME, new byte[] { slotTime });
    }
    public final void setTxTail(byte txTail) {
        writeSetting(KissFrame.FrameType.TX_TAIL, new byte[] { txTail });
    }
    public final void setFullDuplex(byte duplex) {
        writeSetting(KissFrame.FrameType.FULL_DUPLEX, new byte[] { duplex });
    }
    public final void writeSetting(KissFrame.FrameType type, byte[] value) {
        sendFrame(new KissFrame(value, type));
    }

}
