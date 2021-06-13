package redes;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class Logger {
    private final TextArea log;
    public Logger(TextArea log) {
        this.log = log;
    }
    public synchronized void postMessageln(String message){
        Platform.runLater(()-> log.appendText(message + "\n"));
    }
    public synchronized void postMessage(String message) {
        Platform.runLater(()-> log.appendText(message));
    }


}
