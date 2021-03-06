package ax25irc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ax25irc.ax25modem.sivantoledo.ax25.Afsk1200MultiDemodulator;
import ax25irc.ax25modem.sivantoledo.soundcard.SoundcardConsumer;

public class StdinDecoder extends PacketModem {

    private SoundcardConsumer consumer;
    private byte[] capture_buffer;
    int rate = 22050;
    int latency_ms = 100;

    public StdinDecoder(AX25PacketListener listener) {

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

    @Override
    public void run() {

        System.out.println("processing input from STDIN");

        try {
            receive(System.in);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
