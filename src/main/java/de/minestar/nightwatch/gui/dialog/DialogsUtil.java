package de.minestar.nightwatch.gui.dialog;

import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialog.Actions;

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

}
