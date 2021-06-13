package redes.network;

import javafx.concurrent.Task;

import java.io.*;
import java.net.ServerSocket;

public class FileServerTask extends Task<Void> {

    private int port;
    private String srcFolderPath;

    public FileServerTask(int port, String srcFolderPath) {
        this.port = port;
        this.srcFolderPath = srcFolderPath;
    }

    @Override
    public Void call(){
        try {
            var serverSocket = new ServerSocket(port);
            while (true) {
                var clientSocket = serverSocket.accept();
                var dataInStream = new DataInputStream(clientSocket.getInputStream());
                var dataOutStream = new DataOutputStream(clientSocket.getOutputStream());
                var FileName = dataInStream.readUTF();

                var file = new File(srcFolderPath, FileName);
                var size = file.length();

                var fileStream = new DataInputStream(
                        new FileInputStream(file.getAbsolutePath()));

                dataOutStream.writeLong(size);
                dataOutStream.flush();

                var buffer = new byte[Common.BUFFER_SIZE];
                long sent = 0;
                int n = 0;
                float percent = 0.0f;
                while (sent < size) {
                    n = fileStream.read(buffer);
                    dataOutStream.write(buffer, 0, n);
                    sent += n;
                    percent = (float) sent * 100 / n ;
                }
                dataInStream.close();
                fileStream.close();
                dataOutStream.close();
                clientSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
