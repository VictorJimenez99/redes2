package redes.network;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FileReceiverTask extends Task<Void> {

    private final ProgressBar progressBar;
    private final String host;
    private final int port;
    private final String destinationFolder;
    private final String fileName;

    public FileReceiverTask(ProgressBar progressBar,
                            String host, int port,
                            String destinationFolder, String fileName) {
        this.progressBar = progressBar;
        this.host = host;
        this.port = port;
        this.destinationFolder = destinationFolder;
        this.fileName = fileName;
    }

    @Override
    public Void call() {

        Socket socket = null;
        try {
            socket = new Socket(host, port);
            var dataOutStream = new DataOutputStream(socket.getOutputStream());
            var dataInStream = new DataInputStream(socket.getInputStream());
            dataOutStream.writeUTF(fileName);

            var size = dataInStream.readLong();

            var fileStream = new DataOutputStream(
                    new FileOutputStream(destinationFolder + "/" + fileName));

            var buffer = new byte[Common.BUFFER_SIZE];

            long received = 0;
            int n;
            double percent;
            while (received<size) {
                n = dataInStream.read(buffer);
                fileStream.write(buffer, 0, n);
                fileStream.flush();
                received += n;
                percent = (double) received/size;
                double tempPercent = percent;
                Platform.runLater(()->
                progressBar.setProgress(tempPercent));
            }
            fileStream.close();
            dataOutStream.close();
            dataInStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }





        return null;
    }
}
