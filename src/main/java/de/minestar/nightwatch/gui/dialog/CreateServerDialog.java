package de.minestar.nightwatch.gui.dialog;

import java.io.File;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.server.ObservedMinecraftServer;
import de.minestar.nightwatch.server.ObservedMinecraftServer.Builder;
import de.minestar.nightwatch.util.DurationUtil;

public class CreateServerDialog extends Dialog {

    protected StringProperty serverName;
    protected ObjectProperty<File> serverFile;
    protected ObjectProperty<String> minMemory;
    protected ObjectProperty<String> maxMemory;
    protected BooleanProperty isJava7;
    protected ObjectProperty<String> permGenSize;
    protected BooleanProperty autoBackup;
    protected BooleanProperty autoRestart;
    protected StringProperty vmOptions;

    protected BooleanProperty doAutoRestarts;
    protected StringProperty restartTimes;
    protected StringProperty restartWarnings;

    private ValidationSupport val;

    public CreateServerDialog(Stage stage) {
        super(stage, "Create Server", false, DialogStyle.NATIVE);
        val = new ValidationSupport();
        setClosable(true);
        getActions().addAll(Dialog.Actions.OK, Dialog.Actions.CANCEL);
        setContent(createContent());
    }

    private Node createContent() {

        this.getStylesheets().add(getClass().getResource("/styles/createServerDialog.css").toExternalForm());

        // TODO: Fix the uglyness
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(createMainOptionsTab(), createAutoRestartTab());
        tabPane.setPrefHeight(400);
        tabPane.setPrefWidth(500);
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        return tabPane;
    }

    private Tab createMainOptionsTab() {

        GridPane pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setPrefHeight(200);
        pane.setPrefWidth(500);
        pane.setVgap(10);
        pane.setHgap(20);

        int row = 0;

        TextField serverNameField = new TextField();
        serverNameField.setEditable(true);
        this.serverName = serverNameField.textProperty();
        val.registerValidator(serverNameField, Validator.createEmptyValidator("Must specify a server name!"));
        pane.addRow(row++, new Label("Name"), serverNameField, DialogsUtil.createToolTipNode("The unique name of the server"));

        TextField pathTextField = new TextField();
        this.serverFile = new SimpleObjectProperty<>();
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
        pathTextField.setText("");
        pathTextField.textProperty().bind(serverFile.asString());
        serverFile.set(new File(""));
        pathTextField.setPrefWidth(300);
        val.registerValidator(pathTextField, Validator.createEmptyValidator("Must specify the path to server binary!"));
        pane.addRow(row++, new Label("Server Path"), pathTextField, DialogsUtil.createToolTipNode("The path to the server program"));

        CheckBox doAutomaticBackups = new CheckBox();
        this.autoBackup = doAutomaticBackups.selectedProperty();
        this.autoBackup.set(true);
        pane.addRow(row++, new Label("Shutdown Backup"), doAutomaticBackups, DialogsUtil.createToolTipNode("Create an automatic backup of the server at shutdown."));

        CheckBox doAutomaticRestarts = new CheckBox();
        this.autoRestart = doAutomaticRestarts.selectedProperty();
        this.autoRestart.set(true);
        pane.addRow(row++, new Label("Shutdown Restart"), doAutomaticRestarts, DialogsUtil.createToolTipNode("Automatically restarts the server after shutdown."));

        pane.add(new Separator(), 0, row++, 3, 1);

        ComboBox<String> minMemoryBox = createMemoryComboBox(val);
        this.minMemory = minMemoryBox.valueProperty();
        pane.addRow(row++, new Label("MinMemory"), minMemoryBox, DialogsUtil.createToolTipNode("Amount of memory the server starts with"));

        ComboBox<String> maxMemoryBox = createMemoryComboBox(val);
        this.maxMemory = maxMemoryBox.valueProperty();
        pane.addRow(row++, new Label("MaxMemory"), maxMemoryBox, DialogsUtil.createToolTipNode("Amount of memeory the server can use until extensive garbage collection"));

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
        pane.addRow(row++, new Label("Java7"), isJava7Box, DialogsUtil.createToolTipNode("Use Java7 instead Java8. Forge has problems with Java8"));

        ComboBox<String> permGenSizeBox = new ComboBox<>(FXCollections.observableArrayList("128M", "256M", "512M"));
        permGenSizeBox.setEditable(true);
        permGenSizeBox.getSelectionModel().select("256M");
        permGenSizeBox.disableProperty().bind(isJava7Box.selectedProperty().not());
        this.permGenSize = permGenSizeBox.valueProperty();
        pane.addRow(row++, new Label("PermGenSize"), permGenSizeBox, DialogsUtil.createToolTipNode("The more mods are used the higher this parameter should be."));

        TextField vmOptionsField = new TextField();
        vmOptions = vmOptionsField.textProperty();
        pane.addRow(row++, new Label("VM Options"), vmOptionsField, DialogsUtil.createToolTipNode("Additional VM option the server starts with. You need to know what you do!"));

        Actions.OK.disabledProperty().bind(val.invalidProperty());

        Tab tab = new Tab("Main Options");
        tab.setContent(pane);

        return tab;
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
                return ValidationResult.fromError(t, "Invalid format!");
            } else {
                return new ValidationResult();
            }
        });

        return t;
    }

    private Tab createAutoRestartTab() {
        // TODO: Find a better way to do this
        Tab tab = new Tab("Auto Restarts");

        CheckBox doAutoRestartsBox = new CheckBox("Auto restart ");
        this.doAutoRestarts = doAutoRestartsBox.selectedProperty();

        TextArea restartTimeArea = new TextArea("12:00");
        restartTimes = restartTimeArea.textProperty();
        restartTimeArea.setMaxHeight(200);
        restartTimeArea.setMaxWidth(50);
        // Check if all lines are possible local times
        val.registerValidator(restartTimeArea, (c, s) -> {
            String[] split = s.toString().split("[\r\n]+");
            for (int i = 0; i < split.length; i++) {
                if (split[i].isEmpty())
                    continue;
                try {
                    LocalTime.parse(split[i]);
                } catch (Exception e) {
                    return ValidationResult.fromError(c, "Invalid time format " + split[i] + "!");
                }

            }
            return new ValidationResult();
        });

        TextArea restartWarningsArea = new TextArea("30m");
        restartWarnings = restartWarningsArea.textProperty();
        restartWarningsArea.setMaxHeight(200);
        restartWarningsArea.setMaxWidth(50);
        val.registerValidator(restartWarningsArea, (c, s) -> {
            String[] split = s.toString().split("[\r\n]+");
            for (int i = 0; i < split.length; i++) {
                if (split[i].isEmpty())
                    continue;
                Duration dur = DurationUtil.parse(split[i]);
                if (dur == Duration.ZERO)
                    return ValidationResult.fromError(c, "Invalid duration format " + split[i] + "!");
            }
            return new ValidationResult();
        });

        restartTimeArea.disableProperty().bind(doAutoRestartsBox.selectedProperty().not());
        restartWarningsArea.disableProperty().bind(doAutoRestartsBox.selectedProperty().not());

        Label infoLabel = new Label("Restart times in format HH:MM. Midnight is 00:00, not 24:00. Each restart in one line");
        Label infoLabel2 = new Label("Warning intervals before restart. Format 1h 2m 3s. Each warning in one line");
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(5));
        content.getChildren().addAll(doAutoRestartsBox, infoLabel, restartTimeArea, new Separator(), infoLabel2, restartWarningsArea);
        tab.setContent(content);

        return tab;
    }
    public Optional<ObservedMinecraftServer> startDialog() {

        Action action = show();
        // Disable the validator
        Actions.OK.disabledProperty().unbind();
        Actions.OK.disabledProperty().set(false);
        if (action == Actions.OK) {
            // Start constructing server
            Builder serverBuilder = ObservedMinecraftServer.create(serverName.get(), serverFile.get());
            // Fill heap information
            serverBuilder.minHeapSize(minMemory.get()).maxHeapSize(maxMemory.get());
            // Add other vm options
            serverBuilder.otherVmOptions(vmOptions.get());
            // Enable / Disable backup and restart after shutdown
            serverBuilder.backupAfterShutdown(autoBackup.get()).restartAfterShutdown(autoRestart.get());
            // Enable / Disable java 7 and set the perm gem size
            serverBuilder.useJava7(isJava7.get()).maxPermGen(permGenSize.get());
            // If the do restarts, parse restart times and warnings
            if (doAutoRestarts.get()) {
                // Parse restarts
                List<LocalTime> restartTimes = new ArrayList<>();
                String restartTimesString = this.restartTimes.get();
                String[] split = restartTimesString.split("[\r\n]+");
                for (int i = 0; i < split.length; i++) {
                    String time = split[i];
                    restartTimes.add(LocalTime.parse(time));
                }

                // Parse warnings
                List<Duration> restartWarnings = new ArrayList<>();
                String restartWarningsString = this.restartWarnings.get();
                split = restartWarningsString.split("[\r\n]+");
                for (int i = 0; i < split.length; i++) {
                    String duration = split[i];
                    restartWarnings.add(DurationUtil.parse(duration));
                }
                serverBuilder.restartTimes(restartTimes).warningIntervals(restartWarnings);
            }

            return Optional.of(serverBuilder.build());
        } else
            return Optional.empty();
    }

}
