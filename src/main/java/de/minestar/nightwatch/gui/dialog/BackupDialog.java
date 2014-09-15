package de.minestar.nightwatch.gui.dialog;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;

import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.threading.BackupTask;

public class BackupDialog extends Dialog {

    private static final String DIALOG_TITLE = "Backup server";

    private static final ReadOnlyIntegerProperty STEPS = Core.mainConfig.backupDelay();

    private Task<Void> restartTask;
    private BackupTask backupTask;

    public BackupDialog(Stage stage, BackupTask backupTask) {
        super(stage, DIALOG_TITLE, false, DialogStyle.NATIVE);
        this.setClosable(false);
        this.backupTask = backupTask;
        setContent(createContent());
    }

    private Node createContent() {
        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));
        vBox.setAlignment(Pos.CENTER);

        Label timeLabel = new Label();
        Label infoLabel = new Label("Backup in ");
        HBox infoPane = new HBox(infoLabel, timeLabel);
        infoPane.setAlignment(Pos.CENTER);
        vBox.getChildren().add(infoPane);

        ProgressBar progress = new ProgressBar();
        vBox.getChildren().add(progress);

        restartTask = new Task<Void>() {
            protected Void call() throws Exception {
                int steps = STEPS.get();
                for (int i = 0; i <= steps; ++i) {
                    updateMessage("" + (steps - i));
                    updateProgress(i, steps);
                    Thread.sleep(1000);
                }
                // Initiate backup task
                Platform.runLater(() -> {
                    timeLabel.textProperty().unbind();
                    timeLabel.setText("");

                    progress.progressProperty().bind(backupTask.progressProperty());
                    infoLabel.textProperty().bind(backupTask.messageProperty());
                });
                Thread backupThread = new Thread(backupTask, "BackupTask");
                backupThread.start();
                // Wait until backup is finished
                backupThread.join();
                // Close the dialog
                Platform.runLater(() -> setResult(Dialog.Actions.OK));
                return null;
            };

            @Override
            protected void cancelled() {
                // Close the dialog
                Platform.runLater(() -> setResult(Dialog.Actions.CANCEL));
                super.cancelled();
            }
        };
        progress.progressProperty().bind(restartTask.progressProperty());

        timeLabel.textProperty().bind(restartTask.messageProperty());

        getActions().setAll(Actions.CANCEL);
        return vBox;
    }

    @Override
    public Action show() {
        Thread thread = new Thread(restartTask);
        thread.start();
        Action result = super.show();
        if (result == Actions.CANCEL) {
            this.restartTask.cancel(true);
            this.backupTask.cancel(true);
        }

        return result;
    }
}
