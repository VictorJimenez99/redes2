package redes.network;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkTask extends Task<Void> {
    @Override
    public Void call(){
        var tester = new TopologyTesterTask();
        AtomicBoolean result = new AtomicBoolean(false);
        tester.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            System.out.println("Done");
            var isNew = tester.getValue();
            result.set(isNew);
        });
        var testerThread = new Thread(tester);
        testerThread.start();
        try {
            System.out.print("Waiting for tester to finish...");
            testerThread.join();
            var isNewTopology = result.get();
            System.out.println("is there an existing topology?: " + isNewTopology);
            if(isNewTopology) {
                var server = new MulticastServerTask(9090, true);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
