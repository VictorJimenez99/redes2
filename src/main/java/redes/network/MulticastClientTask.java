package redes.network;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

public class MulticastClientTask extends Task<Void> {
    private final MulticastSocket receiver;
    private final TableView<PeerEntry> table;

    public MulticastClientTask(TableView<PeerEntry> table,
                               int myRMIPort,
                               Label prevNodeLabel,
                               Label nextNodeLabel) throws IOException {
        System.out.println("Multicast listener registered with rmi port: " + myRMIPort);
        receiver = new MulticastSocket(Common.multicastGroupPort);
        receiver.joinGroup(Common.multicastGroup);
        this.table = table;
        var list = table.getItems();
        list.addListener((ListChangeListener<? super PeerEntry>) c -> {
            System.out.println("new peer added at table row: " + c);
            if(c.next()) {
                if(list.size() == 1) {
                    Platform.runLater(()-> {
                        prevNodeLabel.setText(list.get(c.getFrom())+ "");
                        nextNodeLabel.setText(list.get(c.getFrom())+ "");
                    });
                    return;
                }
                var foundNext = false;
                var position = 0;
                for (var entry: list) {
                    if(myRMIPort + 1 == entry.getRmiPort()) {
                        foundNext = true;
                        break;
                    }
                    position += 1;
                }
                PeerEntry ref = null;
                if(foundNext) {
                    System.out.println("found a new nextNode at position: " + position);
                    ref = list.get(position);
                } else {
                    System.out.println("No new nextNodeFound wrapping to node 0");
                    var startingNodeIndex = 0;
                    for(var node: list) {
                        if(node.getRmiPort() == 9000){
                            break;
                        }
                        startingNodeIndex += 1;
                    }
                    try {
                        ref = list.get(startingNodeIndex);
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("waiting to connect to every other peer");
                    }
                }
                if(ref == null) {
                    ref = list.get(list.size()-1);
                }
                PeerEntry finalRef = ref;
                Platform.runLater(()->nextNodeLabel.setText(finalRef + ""));
                var foundPrev = false;
                position = 0;
                for (var entry: list) {
                    if(myRMIPort - 1 == entry.getRmiPort()) {
                        foundPrev = true;
                        break;
                    }
                    position += 1;
                }
                if(foundPrev) {
                    System.out.println("found a previous nextNode at position: " + position);
                    ref = list.get(position);
                } else {
                    System.out.println("No new prevNodeFound wrapping to lastNode");
                    var startingNodeIndex = 0;
                    var array = new PeerEntry[0];
                    array = list.toArray(array);
                    var ports = new int[array.length];
                    var i = 0;
                    for(var entry: array) {
                        ports[i] = entry.getRmiPort();
                    }
                    var highestPort = Arrays.stream(ports).max().orElseGet(()->0);//we know it exists
                    for(var node: list) {
                        if(node.getRmiPort() == highestPort){
                            break;
                        }
                        startingNodeIndex += 1;
                    }
                    ref = list.get(startingNodeIndex);
                }
                PeerEntry finalRef1 = ref;
                Platform.runLater(()->prevNodeLabel.setText(finalRef1 + ""));
            }
    });
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
                    peerEntry.setIpAddr((packet.getAddress()+"").substring(1));
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
