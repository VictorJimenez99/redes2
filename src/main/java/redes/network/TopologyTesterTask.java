package redes.network;

import javafx.concurrent.Task;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;


public class TopologyTesterTask extends Task<Boolean> {
    @Override
    protected Boolean call() throws Exception {
        System.out.println("Testing Topology");
        var tester = new MulticastSocket(Common.multicastGroupPort);
        tester.joinGroup(Common.multicastGroup);
        byte[] data = new byte[Common.BUFFER_SIZE];
        var packet = new DatagramPacket(data, data.length);
        tester.setSoTimeout(6000);
        try {
            tester.receive(packet);
            //System.out.println(Arrays.toString(packet.getData()));
            //System.out.println("new package received returning true");
            return true;
        } catch (SocketTimeoutException te){
            //System.out.println("no package received returning false");
            return false;
        }
    }
}
