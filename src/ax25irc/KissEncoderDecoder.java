package ax25irc;

import ax25irc.kisstnc.KissFrame;
import ax25irc.kisstnc.KissFrameListener;
import ax25irc.kisstnc.MobilinkdTnc;
import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;

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

    @Override
    public void frameReceived(KissFrame frame) {

        if (frame.getType() == KissFrame.FrameType.DATA) {

            this.handlePacket(frame.getData());

        } else {

            System.out.println("received KISS frame of type " + frame.getType().name());

        }

    }

    @Override
    public void sendPacket(byte[] packet) {

        if (mobilinkdTnc != null) {

            mobilinkdTnc.sendFrame(new KissFrame(packet, KissFrame.FrameType.DATA));

        } else {

            System.out.println("TNC isn't initialized, can't send the packet.");

        }

    }

    public void setupPort(String portName) {

        try {

            serialPort = SerialPort.getCommPort(portName);
            
            if (serialPort.openPort()) {

                serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();

            } else {
                System.err.println("Unable to open port "+portName+", perhaps invalid name/path?");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void setupTnc() {

        if (in != null && out != null) {
            mobilinkdTnc = new MobilinkdTnc(in, out);
        }
    }

    @Override
    public void run() {

        System.out.println("processing input from KISS TNC at " + portName);

        setupPort(portName);
        setupTnc();

        if (mobilinkdTnc != null) {

            mobilinkdTnc.registerFrameListener(this);
            mobilinkdTnc.start();

        } else {
            System.err.println("TNC isn't initialized.");
        }

    }

}
