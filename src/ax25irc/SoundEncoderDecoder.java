package ax25irc;

import ax25irc.ax25modem.sivantoledo.ax25.Afsk1200Modulator;
import ax25irc.ax25modem.sivantoledo.ax25.Afsk1200MultiDemodulator;
import ax25irc.ax25modem.sivantoledo.ax25.Packet;
import ax25irc.ax25modem.sivantoledo.ax25.PacketDemodulator;
import ax25irc.ax25modem.sivantoledo.sampledsound.Soundcard;
import ax25irc.ax25modem.sivantoledo.soundcard.SoundcardConsumer;
import ax25irc.ax25modem.sivantoledo.soundcard.SoundcardProducer;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class SoundEncoderDecoder extends PacketModem {

    InputStream in;
    OutputStream out;

    SoundcardConsumer consumer;
    SoundcardProducer producer;
    Soundcard soundcard;
    Afsk1200Modulator modulator;
    PacketDemodulator demodulator;

    String soundDeviceName;

    int rate = 22050;
    int latency_ms = 100;
    int TXDelay = 50;
    int Persist = 63;
    int SlotTime = 10;
    int FullDuplex = 0;
    int TXTail = 10;

    LinkedBlockingQueue<Packet> pendingPackets;

    public SoundEncoderDecoder(AX25PacketListener listener, String soundDeviceName) {

        super(listener);

        this.soundDeviceName = soundDeviceName;
        pendingPackets = new LinkedBlockingQueue<>();

    }

    public void sendPacket(byte[] packet) {
        pendingPackets.offer(new Packet(packet));
    }

    public void sendPacketLoop() {

        while (!interrupted()) {

            Packet p = null;
            
            try { 
                p = pendingPackets.take();
            } catch(InterruptedException ex) {}
            
            if(p == null)
                continue;

            while (demodulator.dcd()) {
                yield();
            }

            while (Math.random() > Persist) {

                try {
                    Thread.sleep(10 * SlotTime);
                } catch (InterruptedException ie) {
                }

                while (demodulator.dcd()) {
                    yield(); // wait for a channel that is not busy
                }

            }

            modulator.prepareToTransmit(p);
            soundcard.transmit();

        }

    }

    public void setupSoundCard() {

        try {
            modulator = new Afsk1200Modulator(rate);
            demodulator = new Afsk1200MultiDemodulator(rate, this);
            modulator.setTxDelay(TXDelay);
            soundcard = new Soundcard(rate, soundDeviceName, soundDeviceName, latency_ms, demodulator, modulator);
        } catch (Exception e) {
            System.err.println("Afsk1200 constructor exception: " + e.getMessage());
        }

    }

    public void run() {

        System.out.println("processing input/output on default mixer ");
        setupSoundCard();

        soundcard.enumerate();
        
        new Thread() { public void run() { sendPacketLoop(); } }.start();
        
        soundcard.receive();

    }

}
