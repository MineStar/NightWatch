package de.minestar.nightwatch.threading;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.concurrent.Task;

public class ServerCommandTask extends Task<Void> {

    private LinkedBlockingQueue<String> commandQueue;
    private List<String> commandBuffer;
    private BufferedWriter serverInput;

    public ServerCommandTask(OutputStream serverInput, LinkedBlockingQueue<String> commandQueue) {
        this.commandQueue = commandQueue;
        this.commandBuffer = new ArrayList<>();
        this.serverInput = new BufferedWriter(new OutputStreamWriter(serverInput));
    }

    public void addCommand(String command) {
        this.commandQueue.add(command);
    }

    @Override
    protected Void call() throws Exception {
        while (!isCancelled()) {
            Thread.sleep(50);
            flush();
        }
        return null;
    }

    public void flush() throws Exception {
        this.commandBuffer.clear();
        this.commandQueue.drainTo(commandBuffer);
        for (String command : commandBuffer) {
            serverInput.write(command);
            serverInput.newLine();
            serverInput.flush();
        }
    }

    @Override
    protected void cancelled() {
        try {
            serverInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
