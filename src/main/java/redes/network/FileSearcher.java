package redes.network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileSearcher extends Remote {
    String getFileLocation(String fileName, int portCaller, int relay) throws RemoteException;
}
