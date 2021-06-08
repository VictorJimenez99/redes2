module redes {
    requires javafx.controls;
    requires javafx.fxml;

    opens redes to javafx.fxml;
    exports redes;
    exports redes.gui;
    opens redes.gui to javafx.fxml;
    exports redes.network;
    opens redes.network to javafx.fxml;
}