module redes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;

    opens redes to javafx.fxml;
    exports redes;
    exports redes.gui;
    opens redes.gui to javafx.fxml;
    exports redes.network;
    opens redes.network to javafx.fxml;
}