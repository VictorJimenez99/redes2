package redes.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import redes.network.NetworkTask;
import redes.network.PeerEntry;

public class MainWindowController {
    public Pane workspace;
    public TableColumn<PeerEntry, String> ipColumn;
    public TableColumn<PeerEntry, String> rmiColumn;
    public TableColumn<PeerEntry, String> multicastColumn;
    public Button joinButton;
    public Button destFolderButton;
    public Button srcFolderButton;
    public TableView<PeerEntry> peerTable;

    private String destinationFolder;
    private String srcFolder;


    @FXML
    private void initialize() {
        srcFolderButton.setDisable(true);
        joinButton.setDisable(false);//return to true
        workspace.setDisable(true);

        ipColumn.setCellValueFactory(peer->
                new SimpleStringProperty(peer.getValue().getIpAddr()));
        multicastColumn.setCellValueFactory(peer->
                new SimpleStringProperty(peer.getValue().getMulticastPort()+""));
        rmiColumn.setCellValueFactory(peer->
                new SimpleStringProperty(peer.getValue().getRmiPort() + ""));
    }

    public void tryToUnlock() throws InterruptedException {
        joinButton.setDisable(true);
        var networkTask = new NetworkTask(peerTable);
        var thread = new Thread(networkTask);
        thread.setDaemon(true);
        thread.start();
        workspace.setDisable(false);
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
        srcFolderButton.setDisable(true);
    }
}
