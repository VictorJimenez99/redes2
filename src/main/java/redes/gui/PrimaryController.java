package redes.gui;

import java.io.IOException;
import javafx.fxml.FXML;
import redes.App;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
