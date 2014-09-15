package de.minestar.nightwatch.gui.dialog;

import java.io.File;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;

import de.minestar.nightwatch.core.Configuration;
import de.minestar.nightwatch.core.Core;

/**
 * This dialogs is a dialog to set the values of {@link Configuration}
 */
public class GeneralOptionsDialog extends Dialog {

    private static final String ICON_SELECT_BACKUP_FOLDER = "/icons/backup.png";
    private static final String ICON_SELECT_JAVA7 = "/icons/backup.png";

    public GeneralOptionsDialog(Stage stage) {
        super(stage, "Options", false, DialogStyle.NATIVE);
        setClosable(true);
        getActions().addAll(Dialog.Actions.OK, Dialog.Actions.CANCEL);
        setContent(createContent());
    }

    private Node createContent() {
        GridPane pane = new GridPane();
        pane.setPrefHeight(200);
        pane.setPrefWidth(500);
        pane.setVgap(10);
        pane.setHgap(20);

        int row = 0;

        // Java 7 Executable
        TextField java7Path = new TextField();
        java7Path.setPrefWidth(200);
        java7Path.textProperty().bind(Core.mainConfig.java7Path());
        // Button with only image
        Button selectJava7PathButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream(ICON_SELECT_JAVA7))));
        selectJava7PathButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Java executable");
            File f = fileChooser.showOpenDialog(getWindow());
            if (f == null) {
                return;
            }
            Core.mainConfig.java7Path().set(f.getAbsolutePath());
        });

        pane.addRow(row++, new Label("Java7 Binary"), new HBox(java7Path, selectJava7PathButton), DialogsUtil.createToolTipNode("The path to a java 7 binary. It will be used to start the server"));

        // Backup Folder
        TextField backupPath = new TextField();
        backupPath.setPrefWidth(200);
        backupPath.textProperty().bind(Core.mainConfig.backupFolder());
        // Button with image only
        Button backupPathButton = new Button("", new ImageView(new Image(getClass().getResourceAsStream(ICON_SELECT_BACKUP_FOLDER))));
        backupPathButton.setOnAction(e -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select backup folder");
            File f = fileChooser.showDialog(getWindow());
            if (f == null) {
                return;
            }
            Core.mainConfig.backupFolder().set(f.getAbsolutePath());
        });
        pane.addRow(row++, new Label("Backup Folder"), new HBox(backupPath, backupPathButton), DialogsUtil.createToolTipNode("The path to the backup folder"));

        // Restart and backup delay
        ValidationSupport validator = new ValidationSupport();

        TextField restartDelayField = new TextField(Core.mainConfig.restartDelay().getValue().toString());
        validator.registerValidator(restartDelayField, (t, u) -> {
            try {
                Core.mainConfig.restartDelay().set(Integer.parseInt((String) u));
                return new ValidationResult();
            } catch (Exception e) {
                return ValidationResult.fromWarning(restartDelayField, "Not a number");
            }
        });

        pane.addRow(row++, new Label("Restart Delay"), restartDelayField, DialogsUtil.createToolTipNode("The time to cancel a restart"));

        TextField backupDelayField = new TextField(Core.mainConfig.backupDelay().getValue().toString());
        validator.registerValidator(backupDelayField, (t, u) -> {
            try {
                Core.mainConfig.backupDelay().set(Integer.parseInt((String) u));
                return new ValidationResult();
            } catch (Exception e) {
                return ValidationResult.fromWarning(backupDelayField, "Not a number");
            }
        });
        pane.addRow(row++, new Label("Backup Delay"), backupDelayField, DialogsUtil.createToolTipNode("The time to cancel a backup"));

        return pane;
    }

}
