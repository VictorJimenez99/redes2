package redes.gui;

import java.io.IOException;
import javafx.fxml.FXML;
import redes.App;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}