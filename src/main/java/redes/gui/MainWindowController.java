package redes.gui;

import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import redes.network.NetworkTask;
import redes.network.TopologyTesterTask;

public class MainWindowController {
    @FXML
    private void initialize() {
        var networkTask = new NetworkTask();
        var thread = new Thread(networkTask);
        thread.setDaemon(true);
        thread.start();
    }
}
