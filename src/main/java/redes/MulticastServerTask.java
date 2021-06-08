package redes;

import javafx.concurrent.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MulticastServerTask extends Task<Void> {
    private final MulticastSocket socket;
    private final int RMIPort;
    private final Boolean lastFlag;

    public MulticastServerTask(int rmiPort, Boolean lastFlag) throws IOException {
        this.RMIPort = rmiPort;
        this.lastFlag = lastFlag;
        socket = new MulticastSocket(Common.multicastGroupPort);
        socket.joinGroup(Common.multicastGroup);
    }

    @Override
    protected Void call() throws Exception {
        while (true) {
            sendMessage("rmiPort:" + RMIPort  + "\nflag:" + lastFlag);
            Thread.sleep(5000);
        }
    }

    public void sendMessage(String message) {
        var array = message.getBytes(StandardCharsets.UTF_8);
        long numPackets = Math.round(Math.ceil(array.length)/(float)Common.BUFFER_SIZE);
        for(int i = 0; i< numPackets; i++) {
            int from = Common.BUFFER_SIZE * i;
            int to = Common.BUFFER_SIZE*(i+1);
            var chunk = Arrays.copyOfRange(array,from,to);

            DatagramPacket packet = new DatagramPacket(chunk,chunk.length,
                    Common.multicastGroup, Common.multicastGroupPort);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            socket.send(Common.END_SIGNAL_packet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
