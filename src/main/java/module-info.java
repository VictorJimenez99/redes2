module redes {
    requires javafx.controls;
    requires javafx.fxml;

    opens redes to javafx.fxml;
    exports redes;
}