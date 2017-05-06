/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc.kisstnc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author amishhammer
 */
public class KissFrame {
    private static final Logger LOGGER = Logger.getLogger(KissFrame.class.getName());
    public static final byte FRAME_TYPE_DATA = 0x00;
    public static final byte FRAME_TYPE_TX_DELAY = 0x01;
    public static final byte FRAME_TYPE_PERSISTENCE = 0x02;
    public static final byte FRAME_TYPE_SLOT_TIME = 0x03;
    public static final byte FRAME_TYPE_TX_TAIL = 0x04;
    public static final byte FRAME_TYPE_FULL_DUPLEX = 0x05;
    public static final byte FRAME_TYPE_SET_HARDWARE = 0x06;
    public static final byte FRAME_TYPE_RETURN = (byte)0xff;
    
    public enum FrameType {
        DATA(FRAME_TYPE_DATA),
        TX_DELAY(FRAME_TYPE_TX_DELAY),
        PERSISTENCE(FRAME_TYPE_PERSISTENCE),
        SLOT_TIME(FRAME_TYPE_SLOT_TIME),
        TX_TAIL(FRAME_TYPE_TX_TAIL),
        FULL_DUPLEX(FRAME_TYPE_FULL_DUPLEX),
        SET_HARDWARE(FRAME_TYPE_SET_HARDWARE),
        RETURN(FRAME_TYPE_RETURN);
        
        private final byte code;
        FrameType(byte code) {
            this.code = code;
        }
        protected byte getCode() {
            return this.code;
        }
        protected String toHexString() {
            return String.format("0x%02X", getCode());
        }
    };
    static final byte FEND = (byte)0xC0;
    static final byte FESC = (byte)0xDB;
    static final byte TFEND = (byte)0xDC;
    static final byte TFESC = (byte)0xDD;

    // FEND is sent as FESC, TFEND
    static final byte[] FESC_TFEND = new byte[] { FESC, TFEND };

    //FESC is sent as FESC, TFESC
    static final byte[] FESC_TFESC = new byte[] { FESC, TFESC };
    
    private byte[] data;
    private FrameType type;
    private boolean expectResponse = false;
    private KissFrameListener responseHandler = null;
    
    public KissFrame(byte[] data, FrameType type) {
        this.data = data;
        this.type = type;
    }
    public KissFrame(byte[] data, FrameType type, boolean expectResponse, KissFrameListener responseHandler) {
        this.data = data;
        this.type = type;
        this.expectResponse= expectResponse;
        this.responseHandler = responseHandler;
    }
    public KissFrame(byte[] data) {
        switch (data[1]) {
            case FRAME_TYPE_DATA: {
                this.type = FrameType.DATA;
                break;
            }
            case FRAME_TYPE_FULL_DUPLEX: {
                this.type = FrameType.FULL_DUPLEX;
                break;
            }
            case FRAME_TYPE_PERSISTENCE: {
                this.type = FrameType.PERSISTENCE;
                break;
            }
            case FRAME_TYPE_RETURN: {
                this.type = FrameType.RETURN;
                break;
            }
            case FRAME_TYPE_SET_HARDWARE: {
                this.type = FrameType.SET_HARDWARE;
                break;
            }
            case FRAME_TYPE_SLOT_TIME: {
                this.type = FrameType.SLOT_TIME;
                break;
            }
            case FRAME_TYPE_TX_DELAY: {
                this.type = FrameType.TX_DELAY;
                break;
            }
            case FRAME_TYPE_TX_TAIL: {
                this.type = FrameType.TX_TAIL;
                break;
            }
            default: {
                LOGGER.log(Level.SEVERE, "Invalid frame type: {0}", data[1]);
            }
        }
        unTranslateData(data);
    }
    
    private void unTranslateData(byte[] data) {
        byte[] tmp = new byte[data.length-3];
        boolean handlingEscape = false;
        int wPos =0;
        for (int rPos = 2; rPos < data.length-1; rPos++) {
            if (handlingEscape) {
                if (data[rPos] == TFEND) {
                    tmp[wPos++] = FEND;
                } else if (data[rPos] == TFESC) {
                    tmp[wPos++] = FESC;
                } else {
                    System.out.println("Bad Escaped byte: "+data[rPos]);
                }
                handlingEscape = false;
            } else if (data[rPos] == FESC) {
                handlingEscape = true;
            } else {
                tmp[wPos++] = data[rPos];
            }
        }
        this.data = new byte[wPos];
        System.arraycopy(tmp, 0, this.data, 0, wPos);
    }
    protected int writeFramedBytes(OutputStream oStream) throws IOException {
        int count = 0;
        // FRAME Start
        oStream.write(FEND);
        count++;
        
        // FRAME Type
        oStream.write(type.getCode());
        count++;
        
        for (byte b : data) {
            if (b == FEND) {
                oStream.write(FESC_TFEND);
                count+=2;
            } else if (b == FESC) {
                oStream.write(FESC_TFESC);
                count+=2;
            } else {
                oStream.write(b);
                count++;
            }
        }
        oStream.write(FEND);
        count++;
        return count;
    }
    
    public boolean expectResponse() {
        return this.expectResponse;
    }
    
    public KissFrameListener getResponseHandler() {
        return this.responseHandler;
    }
    
    public FrameType getType() {
        return this.type;
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    public String toHexString() {
         StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("0x%02X ", b));
        }
        return sb.toString();
    }
    @Override
    public String toString() {
        return new String(data);
    }
}
