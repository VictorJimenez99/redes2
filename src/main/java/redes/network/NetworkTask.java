package redes.network;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import redes.Logger;
import redes.RegistryPointer;

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
    private final int multicastPort;
    private int myRMIPort;
    private MulticastServerTask serverTask;
    private final TableView<PeerEntry> peerTable;
    private final Label prevNodeLabel;
    private final Label nextNodeLabel;
    private final ProgressIndicator spinner;
    private final Pane workspace;
    private final Logger logger;
    private final String srcFolder;
    private final RegistryPointer registryPointer;

    public NetworkTask(TableView<PeerEntry> table, String srcFolder, int multicastPort,
                       Label prevNodeLabel, Label nextNodeLabel,
                       ProgressIndicator spinner, Pane workspace, Logger logger,
                       RegistryPointer registryPointer) {
        this.peerTable = table;
        this.multicastPort = multicastPort;
        this.nextNodeLabel = nextNodeLabel;
        this.prevNodeLabel = prevNodeLabel;
        this.spinner = spinner;
        this.workspace = workspace;
        this.logger = logger;
        this.srcFolder = srcFolder;
        this.registryPointer = registryPointer;
        logger.postMessageln("Se hará inicio de la configuración del servidor");
        spinner.setVisible(true);
    }

    @Override
    public Void call() throws InterruptedException, IOException {
        var result = new AtomicBoolean(false);
        var testerFinished = new AtomicBoolean(false);

        var tester = new TopologyTesterTask();
        tester.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            System.out.println("Done");
            var isNew = tester.getValue();
            System.out.println("got: " + isNew + " from tester");
            result.set(isNew);
            var port = 0;
            var alreadyCreated = result.get();
            if (!alreadyCreated) {
                System.out.println("Creating a new Topology...");
                createServer(9000);
                myRMIPort = 9000;
                System.out.println("Created a new Topology");
            }else {
                System.out.println("Joining the created topology...");
                port = askForHighestRMIPort();
                myRMIPort = port+1;
                createServer(myRMIPort);
                System.out.println("Highest RMI Port: " + port);
                System.out.println("Setting myRMIPort to: " + myRMIPort);
                //set rmi Port to 'port + 1'
            }
            testerFinished.set(true);
        });
        var testerThread = new Thread(tester);
        testerThread.start();
        System.out.print("Waiting for tester to finish...");
        testerThread.join();
        while (!testerFinished.get()) {
            Thread.sleep(1);
        }
        System.out.println("Done");
        var clientTask = new MulticastClientTask(peerTable, myRMIPort,
                prevNodeLabel, nextNodeLabel);
        var clientTaskThread = new Thread(clientTask);
        clientTaskThread.setDaemon(true);
        clientTaskThread.start();

        Platform.runLater(()-> {
            var stage = (Stage)peerTable.getScene().getWindow();
            stage.setTitle("Topología de anillo puerto: " + myRMIPort);
        });

        var rmiTask = new RMIServerTask(srcFolder, myRMIPort,
                nextNodeLabel, registryPointer,logger);
        var rmiTaskThread = new Thread(rmiTask);
        rmiTaskThread.setDaemon(true);
        rmiTaskThread.start();


        var fileServerPort = myRMIPort + 100;
        var fileServer = new FileServerTask(fileServerPort, srcFolder);
        var fileServerThread = new Thread(fileServer);
        fileServerThread.setDaemon(true);
        fileServerThread.start();


        logger.postMessageln("Terminada toda la configuración de red: " + myRMIPort);
        logger.postMessageln("Puerto de servidor de archivos: " + fileServerPort);
        spinner.setVisible(false);
        workspace.setDisable(false);
        return null;
    }


    private void createServer(int rmiPort) {
        try {
            serverTask = new MulticastServerTask(multicastPort, rmiPort);
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
        var hasFinished = new AtomicBoolean(false);
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
                    var peerRMIPort = Common.
                            getPropertyFromMessage(msg, "RMIPort");
                    var peerMultiCastSocket = Common.
                            getPropertyFromMessage(msg, "MulticastSocket");
                    System.out.println("peer:\n\tRMI: " +
                            peerRMIPort+"\n\tMulticast: " + peerMultiCastSocket);

                    arrayMax.add(Integer.parseInt(peerRMIPort));
                    result.set(Collections.max(arrayMax));
                }
                hasFinished.set(true);
                return null;
            }
        };
        var victim = new Thread(getHighestRMIPortTask);
        victim.setDaemon(true);
        victim.start();
        try {
            Thread.sleep(9000);
            flag.set(false);
            while (!hasFinished.get()) {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.get();
    }
}
