package de.minestar.nightwatch.gui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;

public class RestartDialog extends Dialog {

    private BooleanProperty wasCanceled;

    private static final int STEPS = 5;

    private Task<Boolean> restartTask;

    public RestartDialog(Stage stage) {
        super(stage, "Restarting server", false, DialogStyle.NATIVE);
        System.out.println(stage);
        this.setClosable(false);
        this.wasCanceled = new SimpleBooleanProperty(false);
        setContent(createContent());
    }

    private Node createContent() {
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));
        vBox.setAlignment(Pos.CENTER);

        Label timeLabel = new Label();
        HBox infoPane = new HBox(new Label("Restart in "), timeLabel);
        infoPane.setAlignment(Pos.CENTER);
        vBox.getChildren().add(infoPane);

        ProgressBar progressBar = new ProgressBar();
        vBox.getChildren().add(progressBar);

        restartTask = new Task<Boolean>() {
            protected Boolean call() throws Exception {
                updateValue(false);
                for (int i = 0; i <= STEPS; ++i) {
                    updateMessage("" + (STEPS - i));

                    Thread.sleep(1000);
                }
                // Close the dialog
                Platform.runLater(() -> setResult(Dialog.Actions.OK));
                return false;
            };

            @Override
            protected void cancelled() {
                updateValue(true);
                // Close the dialog
                Platform.runLater(() -> setResult(Dialog.Actions.CANCEL));
                super.cancelled();
            }
        };

        timeLabel.textProperty().bind(restartTask.messageProperty());
        wasCanceled.bind(restartTask.valueProperty());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> restartTask.cancel(true));
        vBox.getChildren().add(cancelButton);

        return vBox;

    }

    @Override
    public Action show() {
        Thread thread = new Thread(restartTask);
        thread.start();
        return super.show();
    }

    public BooleanProperty wasCanceledProperty() {
        return wasCanceled;
    }

}
