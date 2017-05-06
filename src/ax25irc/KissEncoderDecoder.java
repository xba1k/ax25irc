/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc;

import ax25irc.aprs.parser.APRSPacket;
import ax25irc.aprs.parser.Parser;
import ax25irc.ax25modem.sivantoledo.ax25.Packet;
import ax25irc.ax25modem.sivantoledo.ax25.PacketHandler;
import ax25irc.kisstnc.KissFrame;
import ax25irc.kisstnc.KissFrameListener;
import ax25irc.kisstnc.MobilinkdTnc;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 *
 * @author alex
 */
public class KissEncoderDecoder extends PacketModem implements KissFrameListener {

    SerialPort serialPort;
    InputStream in;
    OutputStream out;
    String portName;
    
    MobilinkdTnc mobilinkdTnc;

    public KissEncoderDecoder(AX25PacketListener listener, String portName) {

        super(listener);
        this.portName = portName;

    }
    
    public void frameReceived(KissFrame frame) {
        
        if(frame.getType() == KissFrame.FrameType.DATA) {

            this.handlePacket(frame.getData());
            
        } else {
            
            System.out.println("received KISS frame of type "+frame.getType().name());
            
        }
        
    }
    
    public void sendPacket(byte[] packet) {
        
        if(mobilinkdTnc!=null) {
        
            mobilinkdTnc.sendFrame(new KissFrame(packet, KissFrame.FrameType.DATA));
            
        } else {
            
            System.out.println("TNC isn't initialized, can't send the packet.");
            
        }
        
    }

    public void setupPort(String portName) {

        try {
            
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("Error: Port is currently in use");
            } else {

                CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

                if (commPort instanceof SerialPort) {

                    serialPort = (SerialPort) commPort;
                    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                    in = serialPort.getInputStream();
                    out = serialPort.getOutputStream();

                } else {
                    System.out.println("Port type " + commPort.getClass().getCanonicalName() + " is not supported ");
                }
            }
            
        } catch(gnu.io.NoSuchPortException nspex) {
            
            System.out.println("Port "+portName+" isn't recognized. Perhaps you meant one of these ?");
            
            Enumeration ports = CommPortIdentifier.getPortIdentifiers();
            
            while(ports.hasMoreElements()) {
                
                System.out.println("\t"+((CommPortIdentifier)ports.nextElement()).getName());
                
            }
  

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    public void setupTnc() {
        
        if(in != null && out != null) {
            mobilinkdTnc = new MobilinkdTnc(in, out);
        } 
    }

    public void run() {

        
        System.out.println("processing input from KISS TNC at "+portName);

        
        setupPort(portName);
        setupTnc();

        if(mobilinkdTnc!=null) {
        
        mobilinkdTnc.registerFrameListener(this);
        mobilinkdTnc.start();
        
        } else {
            System.out.println("TNC isn't initialized.");
        }
        
        
    }

}
