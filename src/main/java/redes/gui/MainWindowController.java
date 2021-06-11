package redes.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import redes.network.NetworkTask;
import redes.network.PeerEntry;

import java.util.concurrent.atomic.AtomicReference;

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

    private String destinationFolder;
    private String srcFolder;

    private AtomicReference<String> connectionChannelAddress;


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
        var list = peerTable.getItems();
        connectionChannelAddress = new AtomicReference<>();


    }

    public void tryToUnlock() throws InterruptedException {
        var portString = portTextField.getText();
        var port = -1;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException exception) {
            return;
        }
        portTextField.setDisable(true);
        var stage = (Stage)portTextField.getScene().getWindow();
        stage.setTitle("Topología de anillo puerto: " + portString);
        joinButton.setDisable(true);
        var networkTask = new NetworkTask(peerTable, port, prevNodeLabel, nextNodeLabel);
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
