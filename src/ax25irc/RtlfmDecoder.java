package ax25irc;

import ax25irc.ircd.server.CMDs;
import ax25irc.ircd.server.Channel;
import ax25irc.ircd.server.Client;
import ax25irc.ircd.server.IRCServer;
import ax25irc.ircd.server.ServMessage;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ax25irc.aprs.parser.APRSPacket;
import ax25irc.aprs.parser.InformationField;
import ax25irc.aprs.parser.MessagePacket;
import ax25irc.aprs.parser.ObjectPacket;
import ax25irc.aprs.parser.Parser;
import ax25irc.aprs.parser.Position;
import ax25irc.aprs.parser.PositionPacket;
import ax25irc.ax25modem.sivantoledo.ax25.Afsk1200MultiDemodulator;
import ax25irc.ax25modem.sivantoledo.ax25.Packet;
import ax25irc.ax25modem.sivantoledo.ax25.PacketHandler;
import ax25irc.ax25modem.sivantoledo.soundcard.SoundcardConsumer;
import java.io.BufferedInputStream;

public class RtlfmDecoder extends PacketModem {

    public static String CMD = "rtl_fm -f 144390000 -s 22050 -o 4 -g 100 -C -";

    private SoundcardConsumer consumer;
    private byte[] capture_buffer;
    int rate = 22050;
    int latency_ms = 100;

    public RtlfmDecoder(AX25PacketListener listener) {

        super(listener);

        try {

            consumer = new Afsk1200MultiDemodulator(rate, this);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void receive(InputStream in) throws IOException {

        int j = 0;
        int buffer_size_in_samples = (int) Math.round(latency_ms * ((double) rate / 1000.0) / 4.0);
        capture_buffer = new byte[2 * buffer_size_in_samples];

        float min = 1.0f;
        float max = -1.0f;
        ByteBuffer bb = ByteBuffer.wrap(capture_buffer).order(ByteOrder.LITTLE_ENDIAN);
        float[] f = new float[capture_buffer.length / 2];
        System.err.printf("Listening for packets\n");
        while (!isInterrupted()) {
            int rv;
            rv = in.read(capture_buffer, 0, capture_buffer.length);
            bb.rewind();
            //System.out.printf("read %d bytes of audio\n",rv);
            for (int i = 0; i < rv / 2; i++) {
                short s = bb.getShort();
                f[i] = (float) s / 32768.0f;
                j++;
                //System.out.printf("j=%d\n",j);			  	
                //if (f[i] > max) max = f[i];
                //if (f[i] < min) min = f[i];
                if (j == rate) {
                    //System.err.printf("Audio in range [%f, %f]\n",min,max);
                    //System.err.printf("Audio level %d\n", consumer.peak());
                    j = 0;
                    //min =  1.0f;
                    //max = -1.0f;
                }
            }
            consumer.addSamples(f, rv / 2);
        }
    }

    public void run() {

        System.out.println("processing input from: " + CMD);

        try {

            Process p = new ProcessBuilder().command("/bin/sh", "-c", CMD).start();

            Thread.sleep(2000);

            if (p.isAlive()) {
                receive(p.getInputStream());
            }

            System.out.println("Command terminated with code " + p.waitFor());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
