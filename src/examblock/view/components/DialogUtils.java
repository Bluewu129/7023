package examblock.view.components;

import examblock.model.CSSE7023;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for displaying dialogs and messages.
 */
public class DialogUtils {

    /**
     * Options for text viewer dialogs.
     */
    public enum ViewerOptions {
        SCROLL, NO_SCROLL
    }

    /**
     * Shows a simple message dialog.
     *
     * @param message the message to display
     */
    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a question dialog with Yes/No options.
     *
     * @param question the question to ask
     * @return JOptionPane.YES_OPTION or JOptionPane.NO_OPTION
     */
    public static int askQuestion(String question) {
        return JOptionPane.showConfirmDialog(null, question, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Shows a text viewer dialog for displaying large amounts of text.
     *
     * @param text the text to display
     * @param title the title of the dialog
     * @param options whether to include scroll bars
     * @param fileType the type of file (for styling purposes)
     */
    public static void showTextViewer(String text, String title, ViewerOptions options, CSSE7023.FileType fileType) {
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);

        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        if (options == ViewerOptions.SCROLL) {
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            dialog.add(scrollPane, BorderLayout.CENTER);
        } else {
            dialog.add(textArea, BorderLayout.CENTER);
        }

        // Add close button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Shows an error message dialog.
     *
     * @param message the error message to display
     */
    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a warning message dialog.
     *
     * @param message the warning message to display
     */
    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}