package de.minestar.nightwatch.gui;

import java.io.File;
import java.util.Optional;
import java.util.regex.Pattern;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.FontAwesome.Glyph;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.server.ObservedJava7Server;
import de.minestar.nightwatch.server.ObservedServer;

public class CreateServerDialog extends Dialog {

    protected StringProperty serverName = new SimpleStringProperty();
    protected ObjectProperty<File> serverFile = new SimpleObjectProperty<>();
    protected ObjectProperty<String> minMemory = new SimpleObjectProperty<>();
    protected ObjectProperty<String> maxMemory = new SimpleObjectProperty<>();
    protected BooleanProperty isJava7 = new SimpleBooleanProperty();
    protected ObjectProperty<String> permGenSize = new SimpleObjectProperty<>();
    protected BooleanProperty autoBackup = new SimpleBooleanProperty();
    protected BooleanProperty autoRestart = new SimpleBooleanProperty();

    private ValidationSupport val;

    public CreateServerDialog(Stage stage) {
        super(stage, "Create Server", false, DialogStyle.NATIVE);
        val = new ValidationSupport();
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

        TextField serverNameField = new TextField();
        serverNameField.setEditable(true);
        this.serverName = serverNameField.textProperty();
        val.registerValidator(serverNameField, Validator.createEmptyValidator("Must specify a server name!"));
        pane.addRow(row++, new Label("Name"), serverNameField, createToolTipNode("The unique name of the server"));

        TextField pathTextField = new TextField();
        pathTextField.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Server binary");
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.getExtensionFilters().add((new ExtensionFilter("Server File", "*.jar", "*.exe")));
            File f = fileChooser.showOpenDialog(getWindow());
            if (f == null) {
                return;
            }
            serverFile.set(f);
        });
        pathTextField.textProperty().bind(serverFile.asString());
        pathTextField.setPrefWidth(300);
        val.registerValidator(pathTextField, Validator.createEmptyValidator("Must specify the path to server binary!"));
        pane.addRow(row++, new Label("Server Path"), pathTextField, createToolTipNode("The path to the server program"));

        ComboBox<String> minMemoryBox = createMemoryComboBox(val);
        this.minMemory = minMemoryBox.valueProperty();
        pane.addRow(row++, new Label("MinMemory"), minMemoryBox, createToolTipNode("Amount of memory the server starts with"));

        ComboBox<String> maxMemoryBox = createMemoryComboBox(val);
        this.maxMemory = maxMemoryBox.valueProperty();
        pane.addRow(row++, new Label("MaxMemory"), maxMemoryBox, createToolTipNode("Amount of memeory the server can use until extensive garbage collection"));

        CheckBox isJava7Box = new CheckBox();
        isJava7Box.selectedProperty().addListener((observ, oldVal, newVal) -> {
            // If checkbox is activated and the java7 path never set -> open
            // dialog
            if (newVal && Core.mainConfig.java7Path().isEmpty().get()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Java7 binary");
                File f = fileChooser.showOpenDialog(getWindow());
                if (f == null) {
                    return;
                }
                Core.mainConfig.java7Path().set(f.getAbsolutePath());
            }
        });
        this.isJava7 = isJava7Box.selectedProperty();
        pane.addRow(row++, new Label("Java7"), isJava7Box, createToolTipNode("Use Java7 instead Java8. Forge has problems with Java8"));

        ComboBox<String> permGenSizeBox = new ComboBox<>(FXCollections.observableArrayList("128M", "256M", "512M"));
        permGenSizeBox.setEditable(true);
        permGenSizeBox.getSelectionModel().select("256M");
        permGenSizeBox.disableProperty().bind(isJava7Box.selectedProperty().not());
        this.permGenSize = permGenSizeBox.valueProperty();
        pane.addRow(row++, new Label("PermGenSize"), permGenSizeBox, createToolTipNode("The more mods are used the higher this parameter should be."));

        CheckBox doAutomaticBackups = new CheckBox();
        this.autoBackup = doAutomaticBackups.selectedProperty();
        pane.addRow(row++, new Label("Auto-Backup"), doAutomaticBackups, createToolTipNode("Create automatic backups of the server. Currently this happens at servers shutdown"));

        CheckBox doAutomaticRestarts = new CheckBox();
        this.autoRestart = doAutomaticRestarts.selectedProperty();
        pane.addRow(row++, new Label("Auto-Restart"), doAutomaticRestarts, createToolTipNode("Automatically restarts the server after shutdown. Do not restart if the button shutdown is pressed"));

        Actions.OK.disabledProperty().bind(val.invalidProperty());

        return pane;
    }

    private ComboBox<String> createMemoryComboBox(ValidationSupport val) {
        ComboBox<String> t = new ComboBox<>();

        t.getItems().addAll("512M", "1G", "2G", "4G", "8G", "16G");
        t.setEditable(true);
        t.getSelectionModel().select("1G");

        Pattern p = Pattern.compile("\\d+[MG]");
        val.registerValidator(t, (Control t1, String u) -> {
            boolean isValidFormat = p.matcher(u).matches();
            if (!isValidFormat) {
                shake();
                return ValidationResult.fromError(t, "Invalid format!");
            } else {
                return new ValidationResult();
            }
        });

        return t;
    }

    private Node createToolTipNode(String text) {
        Node n = new FontAwesome().fontColor(Color.ORANGE).create(Glyph.INFO_SIGN.getChar());

        Tooltip.install(n, new Tooltip(text));
        return n;
    }

    public Optional<ObservedServer> startDialog() {

        Action action = show();
        if (action == Actions.OK) {
            if (this.isJava7.not().get()) {
                return Optional.of(new ObservedServer(this.serverName.get(), this.serverFile.get(), this.minMemory.get(), this.maxMemory.get(), this.autoBackup.get(), this.autoRestart.get()));
            } else {
                return Optional.of(new ObservedJava7Server(this.serverName.get(), this.serverFile.get(), this.minMemory.get(), this.maxMemory.get(), this.autoBackup.get(), this.autoRestart.get(), this.permGenSize.get()));
            }
        } else
            return Optional.empty();
    }

}
