package redes.network;

import javafx.concurrent.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MulticastServerTask extends Task<Void> {
    private final DatagramSocket socket;
    private final AtomicInteger MulticastPort;
    private final AtomicInteger RMIPort;
    //private final AtomicBoolean lastFlag;

    public MulticastServerTask(int multicastServerPort, int rmiPort) throws IOException {
        System.out.print("\tCreating a new Multicast Socket...");
        this.RMIPort = new AtomicInteger(rmiPort);
        //this.lastFlag = new AtomicBoolean(lastFlag);
        socket = new DatagramSocket(multicastServerPort);
        this.MulticastPort = new AtomicInteger(multicastServerPort);
        System.out.println("Done");
    }

    @Override
    protected Void call() throws Exception {
        //we spawn a new task so we can manage its state from the parent
        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                var initialConfig = "RMIPort:" + RMIPort.get() + "\nMulticastSocket:" + MulticastPort.get();
                System.out.println(initialConfig);
                while (true) {
                    var msg = "RMIPort:" + RMIPort.get() + "\nMulticastSocket:" + MulticastPort.get();
                    sendMessage(msg);
                    Thread.sleep(5000);
                }
            }
        };
        var childThread = new Thread(task);
        childThread.setDaemon(true);
        childThread.start();
        return null;
    }

    public void sendMessage(String message) {
        var array = message.getBytes(StandardCharsets.UTF_8);
        long numPackets = Math.round(Math.ceil(array.length/(float)Common.BUFFER_SIZE));
        for(int i = 0; i<numPackets; i++) {
            int from = Common.BUFFER_SIZE * i;
            int to = Common.BUFFER_SIZE*(i+1);
            var chunk = Arrays.copyOfRange(array,from,to);
            //System.out.println("sending partial message " +(i+1) +"/"+ numPackets);
            //System.out.println(Arrays.toString(chunk));

            DatagramPacket packet = new DatagramPacket(chunk,chunk.length,
                    Common.multicastGroup, Common.multicastGroupPort);
            try {
                //System.out.println("sent"+ Arrays.toString(packet.getData()));
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
    public void setRMIPort(int value) {
        RMIPort.set(value);
    }

    public int getRMIPort() {
        return RMIPort.get();
    }
}
