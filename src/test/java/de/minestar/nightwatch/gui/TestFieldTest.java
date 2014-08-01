package de.minestar.nightwatch.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestFieldTest extends Application {

    private Process process;
    private TextArea tx;

    private BufferedWriter bWriter;

    @Override
    public void start(Stage stage) throws Exception {

        BorderPane bp = new BorderPane();

        tx = new TextArea();
        tx.setEditable(false);
        bp.setCenter(tx);

        VBox controlls = new VBox(20);
        controlls.setAlignment(Pos.BOTTOM_CENTER);
        bp.setRight(controlls);

        Button clickor = new Button("Start");
        Button stopper = new Button("Stop");
        Button checker = new Button("Check");
        controlls.getChildren().add(clickor);
        clickor.setOnAction(event -> {
            System.out.println("SpawnServer");
            try {
                clickor.setDisable(true);
                stopper.setDisable(false);
                spawn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        controlls.getChildren().add(checker);
        checker.setOnAction(event -> {
            System.out.println("Alive: " + process.isAlive());
        });

        controlls.getChildren().add(stopper);
        stopper.setDisable(true);
        stopper.setOnAction(event -> {
            clickor.setDisable(false);
            stopper.setDisable(true);
            process.destroy();
            System.out.println();
        });

        TextField input = new TextField();
        bp.setBottom(input);

        input.setOnAction(event -> {
            String text = input.getText();
            tx.appendText(text + "\n");
            try {
                bWriter.write(text);
                bWriter.newLine();
                bWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Scene scene = new Scene(bp);

        stage.setTitle("Titel");
        stage.setX(400);
        stage.setX(100);

        stage.setHeight(500);
        stage.setWidth(600);
        stage.setScene(scene);
        stage.show();

    }

    private class ReadTask extends Task<String> {

        private TextArea console;
        private BufferedReader bReader;

        public ReadTask(TextArea console, InputStream source) {
            this.console = console;
            this.bReader = new BufferedReader(new InputStreamReader(source));
        }

        @Override
        protected String call() throws Exception {
            while (process.isAlive()) {
                String line = bReader.readLine();
                if (line != null) {
                    Platform.runLater(() -> console.appendText(line + "\n"));
                }
            }
            return null;
        }

    }

    private void spawn() throws Exception {

        int minMemoryMB = 1024;
        int maxMemoryMB = 1024;
        File jarFile = new File("server/vanilla/minecraft_server.1.7.10.jar");

        String minMemory = "-Xms" + minMemoryMB + "M";
        String maxMemory = "-Xmx" + maxMemoryMB + "M";
        ProcessBuilder pBuilder = new ProcessBuilder("java", minMemory, maxMemory, "-jar", jarFile.getAbsolutePath(), "nogui");
        pBuilder.directory(jarFile.getParentFile());
        pBuilder.redirectOutput(Redirect.PIPE);
        pBuilder.redirectInput(Redirect.PIPE);
        process = pBuilder.start();
        bWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        Thread th = new Thread(new ReadTask(tx, process.getInputStream()));
        th.setName("NewLiner");
        th.setDaemon(false);
        th.start();
    }
}
