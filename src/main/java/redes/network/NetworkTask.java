package redes.network;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//**This task should be the one that manages every other task in this package (redes.network)*/
public class NetworkTask extends Task<Void> {
    private int rmiTextFieldResult;
    private MulticastServerTask serverTask;
    private TableView<PeerEntry> peerTable;

    public NetworkTask(TableView<PeerEntry> table) {
        this.peerTable = table;
    }

    @Override
    public Void call() throws InterruptedException, IOException {
        AtomicBoolean result = new AtomicBoolean(false);

        var tester = new TopologyTesterTask();
        tester.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            System.out.println("Done");
            var isNew = tester.getValue();
            System.out.println("got: " + isNew + " from tester");
            result.set(isNew);
            var alreadyCreated = result.get();
            if (!alreadyCreated) {
                System.out.println("Creating a new Topology...");
                createServer(9000);
                System.out.println("Created a new Topology");
            }else {
                System.out.println("Joining the created topology...");
                var port = askForHighestRMIPort();
                createServer(port+1);
                //set rmi Port to 'port + 1'
                System.out.println("Done");
            }
        });
        var testerThread = new Thread(tester);
        testerThread.start();
        System.out.print("Waiting for tester to finish...");
        testerThread.join();
        return null;
    }


    private void createServer(int rmiPort) {
        try {
            serverTask = new MulticastServerTask(rmiPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        var serverThread = new Thread(serverTask);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private int askForHighestRMIPort() {
        var result = new AtomicInteger(0);
        var arrayMax = new ArrayList<Integer>();
        var flag = new AtomicBoolean(true);
        var getHighestRMIPortTask = new Task<Void>() {
            @Override
            public Void call() throws IOException {
                MulticastSocket receiver = null;
                receiver = new MulticastSocket(Common.multicastGroupPort);
                receiver.joinGroup(Common.multicastGroup);

                byte[] buffer = new byte[Common.BUFFER_SIZE];
                while (flag.get()) {
                    StringBuilder complete = new StringBuilder();
                    while (true) {
                        var packet = new DatagramPacket(buffer, buffer.length);
                        receiver.receive(packet);
                        var msg = new String(packet.getData(),
                                packet.getOffset(), packet.getLength());
                        msg = msg.trim();
                        if (Arrays.equals(packet.getData(), Common.END_SIGNAL)) {
                            break;
                        }
                        complete.append(msg);
                    }
                    var msg = Common.splitMessage(complete.toString().trim());
                    var peerPort = Common.getPropertyFromMessage(msg, "RMIPort");
                    System.out.println("Got peerPort: " + peerPort);
                    arrayMax.add(Integer.parseInt(peerPort));
                    result.set(Collections.max(arrayMax));
                }
                return null;
            }
        };
        var victim = new Thread(getHighestRMIPortTask);
        victim.setDaemon(true);
        victim.start();
        try {
            Thread.sleep(9000);
            flag.set(false);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.get();
    }
}
