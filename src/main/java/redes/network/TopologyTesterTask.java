package redes.network;

import javafx.beans.value.ObservableBooleanValue;
import javafx.concurrent.Task;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;


public class TopologyTesterTask extends Task<Boolean> {
    @Override
    protected Boolean call() throws Exception {
        System.out.println("Testing Topology");
        var tester = new MulticastSocket(Common.multicastGroupPort);
        tester.joinGroup(Common.multicastGroup);
        byte[] data = new byte[1024];
        var packet = new DatagramPacket(data, data.length);
        tester.setSoTimeout(6000);
        try {
            tester.receive(packet);
            return true;
        } catch (SocketTimeoutException te){
            return false;
        }
    }
}
