package redes.network;

import javafx.scene.control.Label;
import javafx.stage.Stage;
import redes.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.io.File;

public class RemoteFileSearcher extends UnicastRemoteObject implements FileSearcher {

    private final File directory;
    private final Logger logger;
    private final int port;
    private final Label label;

    protected RemoteFileSearcher(String srcFolderPath, int port, Label label, Logger logger) throws RemoteException {
        super();
        directory = new File(srcFolderPath);
        this.logger = logger;
        this.port = port;
        this.label = label;
    }

    @Override
    public String getFileLocation(String fileName, int caller) throws RemoteException {
        if(port == caller) {
            logger.postMessage("Se ha completado toda la busqueda en la topología");
            logger.postMessage(" sin embargo, no ha sido posible encontrar el archivo en el resto");
            logger.postMessage(" de los nodos\n");

            var dirList = directory.list();
            if (dirList == null) { dirList = new String[0]; }
            for(var localFile: dirList) {
                if(fileName.equals(localFile)) {
                    logger.postMessage("Buscando en carpeta propia");
                    logger.postMessageln("\nTienes una copia en tu carpeta: " +fileName);
                    return "Own";
                }
            }
            return "None";
        }
        logger.postMessageln(caller+": Buscando el archivo: " + fileName);
        var dirList = directory.list();
        var found = false;
        if (dirList == null) { dirList = new String[0]; }
        for(var localFile: dirList) {
            if(fileName.equals(localFile)) {
                found = true;
            }
        }
        if(found) {
            logger.postMessageln("Se ha encontrado en la carpeta");
            return port +"";
        } else {

            try {

                var nextPortName = label.getText();
                var nextPort = Integer.parseInt(nextPortName.split(":")[1].trim());
                var registry = LocateRegistry.getRegistry(nextPort);
                var searcher = (FileSearcher)registry.lookup("RemoteFileSearcher");
                var location = searcher.getFileLocation(fileName, caller);
                logger.postMessageln("file location: " + location);
                return location;


            } catch (Exception e) {
                System.out.println("Error");
                e.printStackTrace();
            }
        }
        return "None";
    }
}
