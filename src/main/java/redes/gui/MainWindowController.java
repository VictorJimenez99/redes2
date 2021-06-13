package redes.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import redes.Logger;
import redes.RegistryPointer;
import redes.network.FileReceiverTask;
import redes.network.FileSearcher;
import redes.network.NetworkTask;
import redes.network.PeerEntry;

import java.rmi.registry.LocateRegistry;


public class MainWindowController {
    public Pane workspace;
    public TableColumn<PeerEntry, String> ipColumn;
    public TableColumn<PeerEntry, String> rmiColumn;
    public TableColumn<PeerEntry, String> multicastColumn;
    public Button joinButton;
    public Button destFolderButton;
    public Button srcFolderButton;
    public TableView<PeerEntry> peerTable;
    public TextField portTextField;
    public Label nextNodeLabel;
    public Label prevNodeLabel;
    public TextArea log;
    public ProgressIndicator loaderSpinnerIndicator;
    public TextField fileNameSearchBar;
    public ProgressBar progressBar;

    private String destinationFolder =
            "/home/victor/Documents/8voSem/Redes/Prácticas/Anillo/folders/dest9092";
    private String srcFolder =
            "/home/victor/Documents/8voSem/Redes/Prácticas/Anillo/folders/source9092";
    private Logger logger;

    /**For some reason once the main process is killed the registry is not
     * to close it we need to call this object's only method
     * */
    private RegistryPointer deleteThisAfterCloseRegistryPointer;


    @FXML
    private void initialize() {
        System.out.println("Inicio");
        loaderSpinnerIndicator.setVisible(false);
        srcFolderButton.setDisable(true);
        portTextField.setDisable(true);
        joinButton.setDisable(true);//return to true
        workspace.setDisable(true);

        ipColumn.setCellValueFactory(peer->
                new SimpleStringProperty(peer.getValue().getIpAddr()));
        multicastColumn.setCellValueFactory(peer->
                new SimpleStringProperty(peer.getValue().getMulticastPort()+""));
        rmiColumn.setCellValueFactory(peer->
                new SimpleStringProperty(peer.getValue().getRmiPort() + ""));
        logger = new Logger(log);
        logger.postMessageln("GUI Configuration done");
        deleteThisAfterCloseRegistryPointer = new RegistryPointer();
    }

    public void tryToUnlock() throws InterruptedException {
        System.out.println("Inicio del desbloqueo");
        System.out.println("posted first message");
        var portString = portTextField.getText();
        var port = -1;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException exception) {
            return;
        }
        portTextField.setDisable(true);
        var stage = (Stage)portTextField.getScene().getWindow();
        peerTable.getParent().getScene().getWindow().
                addEventFilter(WindowEvent.WINDOW_HIDING,
                event-> {
                    deleteThisAfterCloseRegistryPointer.deleteRegistry();
                });
        stage.setTitle("Topología de anillo puerto: " + portString);
        joinButton.setDisable(true);
        var networkTask = new NetworkTask(peerTable, srcFolder,port,
                prevNodeLabel, nextNodeLabel, loaderSpinnerIndicator, workspace,
                logger, deleteThisAfterCloseRegistryPointer);
        var thread = new Thread(networkTask);
        thread.setDaemon(true);
        thread.start();
        //workspace.setDisable(false);
    }

    public void setDestFolder() {
        var dir = new DirectoryChooser();
        dir.setTitle("Elige el directorio de destino");
        var stage = new Stage();
        var option = dir.showDialog(stage);
        stage.hide();
        System.out.println("selected: "+option.getAbsolutePath());
        destinationFolder = option.getAbsolutePath();
        srcFolderButton.setDisable(false);
        destFolderButton.setDisable(true);
    }

    public void setSrcFolder() {
        var dir = new DirectoryChooser();
        dir.setTitle("Elige el directorio de origen");
        var stage = new Stage();
        var option = dir.showDialog(stage);
        stage.hide();
        System.out.println("selected: "+option.getAbsolutePath());
        srcFolder = option.getAbsolutePath();
        joinButton.setDisable(false);
        portTextField.setDisable(false);
        srcFolderButton.setDisable(true);
    }

    public void searchFile() {
        var fileName = fileNameSearchBar.getText();
        if(fileName.equals("")) {
            logger.postMessageln("No has ingresado ningún nombre");
            return;
        }
        System.out.println("Buscando Archivo");
        var peer = nextNodeLabel.getText();
        var peerPort = Integer.parseInt(peer.split(":")[1]);
        System.out.println("next port: " + peerPort);
        try {
            var registry = LocateRegistry.getRegistry(peerPort);
            var searcher = (FileSearcher)registry.lookup("RemoteFileSearcher");
            var stage = (Stage)peerTable.getScene().getWindow();
            var myPortString = stage.getTitle();
            var myPort = Integer.parseInt(myPortString.split(":")[1].trim());
            var location = searcher.getFileLocation(fileName, myPort);

            if(location.equals("None")) {
                logger.postMessageln("\nNo se ha encontrado el archivo");
                return;
            }else if(location.equals("Own")) {
                logger.postMessageln("Se ha encontrado en tu carpeta");
                return;
            }
            location = location.trim();
            var ownersPort = Integer.parseInt(location);
            logger.postMessageln("Archivo encotrado en el cliente: " + ownersPort);

            var receiver = new FileReceiverTask(progressBar, "localhost", ownersPort + 100,
                    destinationFolder, fileName);

            var receiverThread = new Thread(receiver);
            receiverThread.setDaemon(true);
            receiverThread.start();



        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }

    }
}
