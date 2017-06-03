package ax25irc;

import ax25irc.ax25modem.sivantoledo.ax25.Packet;
import ax25irc.ircd.server.IRCServer;
import ax25irc.ircd.server.Client;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class DCCFileTransfer extends Thread {
    
    private PacketModem modem;
    private IRCServer server;
    private String src, dest, filename, ip;
    private int port;
    private int size;
    private byte buf[] = new byte[BUFSIZ];

    public static int BUFSIZ = 252;
    public static String FILE_TRANSFER_INFO_HEADER = "^1";
    public static String FILE_TRANSFER_DATA_HEADER = "^2";

    private boolean senderMode = false;
    private boolean done = false;

    LinkedBlockingQueue<Packet> pendingPackets;
    private ServerSocket serverSocket;

    public DCCFileTransfer(PacketModem modem, String src, String dest, String ip, String transferParams) {

        this.senderMode = true;
        this.modem = modem;
        this.dest = dest;
        this.src = src;
        this.ip = ip;

        String params[] = transferParams.split(" ");

        filename = params[2].replaceAll("^\"", "").replaceAll("\"$", "");
        port = Integer.parseInt(params[4]);
        size = params.length == 6 ? Integer.parseInt(params[5]) : 0;
        
    }

    public DCCFileTransfer(IRCServer server, String src, String dest, Packet packet) {

        this.senderMode = false;
        this.server = server;
        this.dest = dest;
        this.src = src;

        pendingPackets = new LinkedBlockingQueue<>();

        if (packet.payload[1] == '1') {

            ByteBuffer buffer = ByteBuffer.wrap(packet.payload);
            byte fileNameBytes[] = new byte[BUFSIZ];

            buffer.position(2);

            byte fileNameLength = buffer.get();

            buffer.get(fileNameBytes, 0, fileNameLength);

            filename = new String(fileNameBytes, 0, fileNameLength);
            size = buffer.getInt();

        }

        try {

            serverSocket = new ServerSocket(0);

            this.port = serverSocket.getLocalPort();

            Client client = server.getClient(dest);

            if (client != null) {

                ByteBuffer localAddr = ByteBuffer.wrap(client.getConnection().getSocket().getLocalAddress().getAddress());
                this.ip = String.valueOf(0xffffffff & localAddr.getInt());

            } else {

                this.ip = "2130706433";

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "DCCFileTransfer{" + "dest=" + dest + ", filename=" + filename + ", ip=" + ip + ", port=" + port + ", size=" + size + '}';
    }

    @Override
    public void run() {

        if (senderMode) {
            runSender();
        } else {
            runReceiver();
        }

    }

    public void addPacket(Packet packet) {
        pendingPackets.offer(packet);
    }

    public boolean isDone() {
        return done;
    }

    public void runReceiver() {

        System.out.println("Starting DCC File Transfer (receive): " + toString());

        try {

            Socket client = serverSocket.accept();
            InputStream is = client.getInputStream();
            OutputStream os = client.getOutputStream();

            int bytesProcessed = 0;

            while (bytesProcessed < size && !isInterrupted()) {
                
                Packet packet = pendingPackets.take();
                int dataLength = packet.payload.length - 2;

                os.write(packet.payload, 2, dataLength);
                bytesProcessed += dataLength;

                is.read(new byte[4]);

            }

            client.close();
            serverSocket.close();

            System.out.println("Transfer completed with " + bytesProcessed + " bytes");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void runSender() {

        System.out.println("Starting DCC File Transfer (send): " + toString());

        try {

            Socket s = new Socket(ip, port);
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            ByteBuffer lbuf = ByteBuffer.allocate(4);
            ByteBuffer payload = ByteBuffer.allocate(BUFSIZ + 2);

            int len = 0;
            int bytesSent = 0;
            Packet packet = null;

            payload.put(FILE_TRANSFER_INFO_HEADER.getBytes());
            payload.put((byte) filename.length());
            payload.put(filename.getBytes());
            payload.putInt(size);

            packet = new Packet(dest, src, new String[]{}, Packet.AX25_CONTROL_APRS, Packet.AX25_PROTOCOL_NO_LAYER_3, Arrays.copyOfRange(payload.array(), 0, payload.position()));
            
            byte[] packetBytes = packet.bytesWithoutCRC();
            modem.sendPacket(packetBytes);

            Thread.sleep(2000 + packetBytes.length*8000/1200); // 2 seconds + packet send time at 1200bps

            while ((len = is.read(buf)) > 0) {

                payload.rewind();
                payload.put(FILE_TRANSFER_DATA_HEADER.getBytes());
                payload.put(buf, 0, len);

                packet = new Packet(dest, src, new String[]{}, Packet.AX25_CONTROL_APRS, Packet.AX25_PROTOCOL_NO_LAYER_3, Arrays.copyOfRange(payload.array(), 0, payload.position()));
      
                packetBytes = packet.bytesWithoutCRC();
                modem.sendPacket(packetBytes);

                lbuf.putInt(len);
                // we're supposed to write back number of bytes 'received', but this doesn't seem to work with Pidgin
                // os.write(lbuf.array()); 
                lbuf.rewind();
                bytesSent += len;

                Thread.sleep(2000 + packetBytes.length*8000/1200);

            }

            s.close();

            System.out.println("Transfer completed with " + bytesSent + " bytes");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getSize() {
        return size;
    }

    public String getFilename() {
        return filename;
    }

}
