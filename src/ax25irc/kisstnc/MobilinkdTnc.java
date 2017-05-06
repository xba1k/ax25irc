/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc.kisstnc;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amishhammer
 */
public class MobilinkdTnc extends KissTnc {
    private static final Logger LOGGER = Logger.getLogger(MobilinkdTnc.class.getName());
    private static final byte HANDLE_TX_DELAY = 33;
    private static final byte HANDLE_PERSISTENCE = 34;
    private static final byte HANDLE_SLOT_TIME = 35;
    private static final byte HANDLE_TX_TAIL = 36;
    private static final byte HANDLE_DUPLEX = 37;
    private static final byte HANDLE_RX_VOLUME = 4;
    private static final byte HANDLE_BATTERY_LEVEL = 6;
    private static final byte HANDLE_TX_VOLUME = 12;
    private static final byte HANDLE_INPUT_ATTEN = 13;
    private static final byte HANDLE_SQUELCH_LEVEL = 14;
    private static final byte HANDLE_VERBOSITY = 17;
    private static final byte HANDLE_FIRMWARE_VERSION = 40;
    private static final byte HANDLE_HARDWARE_VERSION = 41;
    private static final byte HANDLE_BLUETOOTH_NAME = 66;
    private static final byte HANDLE_CONNECTION_TRACKING = 70;
    private static final byte HANDLE_USB_POWER_ON = 74;
    private static final byte HANDLE_USB_POWER_OFF = 76;
    private static final byte HANDLE_CAPABILITIES = 126;
    private static final byte HANDLE_PTT_CHANNEL = 80;
    
    private boolean rendevous = false;
    private final Semaphore rendevousSem = new Semaphore(0);
    
    private KissFrameListener setHardwareListener;
    private String firmwareVersion;
            
    public MobilinkdTnc(InputStream iStream, OutputStream oStream) {
        super(iStream, oStream);
        this.setHardwareListener = new KissFrameListener() {
            
            @Override
            public void frameReceived(KissFrame frame) {
                LOGGER.finest("Got response frame type: "+frame.getType().getCode());
                if (frame.getType() != KissFrame.FrameType.SET_HARDWARE) {
                    return;
                }
                byte[] data = frame.getData();
                LOGGER.finest("Got response frame sub-type: "+data[0]);
                switch (data[0]) {
                    case HANDLE_TX_DELAY: {
                        break;
                    }
                    case HANDLE_PERSISTENCE: {
                        break;
                    }
                    case HANDLE_SLOT_TIME: {
                        break;
                    }
                    case HANDLE_TX_TAIL: {
                        break;
                    }
                    case HANDLE_DUPLEX: {
                        break;
                    }
                    case HANDLE_RX_VOLUME: {
                        break;
                    }
                    case HANDLE_BATTERY_LEVEL: {
                        break;
                    }
                    case HANDLE_TX_VOLUME: {
                        break;
                    }
                    case HANDLE_INPUT_ATTEN: {
                        break;
                    }
                    case HANDLE_SQUELCH_LEVEL: {
                        break;
                    }
                    case HANDLE_VERBOSITY: {
                        break;
                    }
                    case HANDLE_FIRMWARE_VERSION: {
                        firmwareVersion = new String(data, 1, data.length-1);
                        break;
                    }
                    case HANDLE_HARDWARE_VERSION: {
                        break;
                    }
                    case HANDLE_BLUETOOTH_NAME: {
                        break;
                    }
                    case HANDLE_CONNECTION_TRACKING: {
                        break;
                    }
                    case HANDLE_USB_POWER_ON: {
                        break;
                    }
                    case HANDLE_USB_POWER_OFF: {
                        break;
                    }
                    case HANDLE_CAPABILITIES: {
                        break;
                    }
                    case HANDLE_PTT_CHANNEL: {
                        break;
                    }
                    default: {
                        LOGGER.log(Level.SEVERE, "Unsupported hardware response code: {0}", data[0]);
                    }
                }
                if (rendevous) {
                    rendevousSem.release();
                }
            }
        };
        this.registerFrameListener(setHardwareListener);
    }
    
    public void getCurrentConfig() {
        stopTx();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        sendFrame(new KissFrame(new byte[] { 0x7F }, KissFrame.FrameType.SET_HARDWARE));
    }
    public void stopTx() {
        sendFrame(new KissFrame(new byte[] { 012 }, KissFrame.FrameType.SET_HARDWARE));
    }
    
    public String getFirmwareVersion() throws InterruptedException {
        stopTx();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        this.rendevous = true;
        sendFrame(new KissFrame(new byte[] { 050 }, KissFrame.FrameType.SET_HARDWARE, true, setHardwareListener));
        this.rendevousSem.acquire();
        this.rendevous = false;
        return this.firmwareVersion;
    }
    
}
