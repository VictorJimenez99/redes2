package redes.network;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import redes.Logger;
import redes.RegistryPointer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerTask extends Task<Void> {
    private final String path;
    private final int port;
    private final Logger logger;
    private final Label label;
    private final RegistryPointer registryPointer;

    public RMIServerTask(String path, int port, Label nextNodeLabel,
                         RegistryPointer pointer, Logger logger) {
        this.path = path;
        this.port = port;
        this.logger = logger;
        this.label = nextNodeLabel;
        this.registryPointer = pointer;
    }
    @Override
    public Void call() {

        try {
            var registry = LocateRegistry.createRegistry(port);
            var remoteSearcher = new RemoteFileSearcher(path, port, label,logger);
            registry.rebind("RemoteFileSearcher", remoteSearcher);
            registryPointer.setPointer1(registry);
            registryPointer.setPointer2(remoteSearcher);
        } catch (RemoteException e) {
            System.out.println("Unable to create RMI Server");
            e.printStackTrace();
        }

        System.out.println("RMI server created at: " + port);
        System.out.println("My src folder: " + path);

        return null;
    }
}
