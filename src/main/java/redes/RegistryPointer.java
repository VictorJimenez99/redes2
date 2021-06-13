package redes;

import redes.network.RemoteFileSearcher;

import java.rmi.NoSuchObjectException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**In order to fully close the application we need to kill this process*/
public class RegistryPointer {
    Registry pointer1;
    RemoteFileSearcher pointer2;

    public RegistryPointer() {
        pointer1 = null;
        pointer2 = null;
    }

    public void setPointer1(Registry pointer1) {
        this.pointer1 = pointer1;
    }

    public void setPointer2(RemoteFileSearcher pointer2) {
        this.pointer2 = pointer2;
    }

    public RegistryPointer(Registry p1, RemoteFileSearcher p2) {
        pointer1 = p1;
        pointer2 = p2;
    }
    public void deleteRegistry() {
        try {
            UnicastRemoteObject.unexportObject(pointer1, true);
            UnicastRemoteObject.unexportObject(pointer2,true);
        } catch (NoSuchObjectException ignored) {
        }
    }


}
