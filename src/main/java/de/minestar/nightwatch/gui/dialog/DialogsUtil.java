package de.minestar.nightwatch.gui.dialog;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.FontAwesome.Glyph;

/**
 * Util class to create standard looking dialogs
 */
public class DialogsUtil {

    private DialogsUtil() {

    }

    /**
     * Create a dialog with the buttons "OK" and "CANCEL" and display a message. This dialogs uses the native style
     * 
     * @param message
     *            The message to display
     * @return The ready dialog to show
     */
    public static Dialogs createOkCancelDialog(String message) {
        return Dialogs.create().style(DialogStyle.NATIVE).actions(Actions.OK, Actions.CANCEL).message(message);
    }

    /**
     * Create a dialog with the buttons "OK" and "CANCEL" , display a message and set the title. This dialogs uses the native style
     * 
     * @param message
     *            The message to display
     * @param title
     *            The title of the dialog
     * @return The ready dialog to show
     */
    public static Dialogs createOkCancelDialog(String message, String title) {
        return createOkCancelDialog(message).title(title);
    }

    /**
     * Shortcut method to create a tool tip and attach it to a explanation mark
     * 
     * @param text
     *            The content of the tool tip
     * @return A node containing an explanation mark with the tool tip
     */
    public static Node createToolTipNode(String text) {
        Node n = new FontAwesome().fontColor(Color.ORANGE).create(Glyph.INFO_SIGN.getChar());

        Tooltip.install(n, new Tooltip(text));
        return n;
    }

}
