package redes.network;

import javafx.concurrent.Task;
import javafx.scene.PerspectiveCamera;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Collections;

public class MulticastClientTask extends Task<Void> {
    private final MulticastSocket receiver;
    private final TableView<PeerEntry> table;

    public MulticastClientTask(TableView<PeerEntry> table) throws IOException {
        receiver = new MulticastSocket(Common.multicastGroupPort);
        receiver.joinGroup(Common.multicastGroup);
        this.table = table;
    }
    @Override
    public Void call() throws IOException {
        System.out.println("Creating client ");
        byte[] buffer = new byte[Common.BUFFER_SIZE];
        while (true) {
            StringBuilder complete = new StringBuilder();
            var peerEntry = new PeerEntry("0", -1,-1);
            while (true) {
                var packet = new DatagramPacket(buffer, buffer.length);
                receiver.receive(packet);
                var msg = new String(packet.getData(),
                        packet.getOffset(), packet.getLength());
                msg = msg.trim();
                if (Arrays.equals(packet.getData(), Common.END_SIGNAL)) {
                    peerEntry.setIpAddr(packet.getAddress()+"");
                    break;
                }
                complete.append(msg);
            }
            var msg = Common.splitMessage(complete.toString().trim());
            var peerRMIPort = Common.getPropertyFromMessage(msg, "RMIPort");
            peerEntry.setRmiPort(Integer.parseInt(peerRMIPort));
            var peerMultiCastSocket = Common.getPropertyFromMessage(msg, "MulticastSocket");
            peerEntry.setMulticastPort(Integer.parseInt(peerMultiCastSocket));

            var items = table.getItems();
            var isNew = true;
            for(PeerEntry entry : items) {
                if(entry.getKey().equals(peerEntry.getKey())) {
                    isNew = false;
                }
            }
            if(isNew) {
                items.add(peerEntry);
            }

        }
    }
}
