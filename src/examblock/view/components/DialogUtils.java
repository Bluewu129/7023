package examblock.view.components;

import examblock.model.CSSE7023;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

/**
 * Dialogs for easy use
 * [13] Dialog usage for file operations and user interactions.
 */
public class DialogUtils {
    /**
     * How to handle long lines of text in the TextViewer
     */
    public static enum ViewerOptions {
        /** Scroll the window to show the text */
        SCROLL,
        /** Wrap the text in the window */
        WRAP
    }

    /** Parent component for centering dialogs */
    private static Component parent = null;

    /**
     * Default constructor
     */
    public DialogUtils() {
        // Empty constructor
    }

    /**
     * Set the frame for dialogs
     *
     * @param frame - where we should be centered
     */
    public static void setParent(Component frame) {
        DialogUtils.parent = frame;
    }

    /**
     * Put up a dialog box with a JTextBox in it
     *[14] Input validation and user interaction handling.
     * @param message - what to say
     * @param title - title for the popup window
     * @param initialValue - if any
     * @return the entered text
     */
    public static String getUserInput(String message, String title, String initialValue) {
        JTextField textField = new JTextField(initialValue != null ? initialValue : "", 20);

        // Create a panel to hold the message and text field
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(message), BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);

        // Add focus listener to select all text when focused
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.selectAll();
            }
        });

        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            return textField.getText();
        }

        return null;
    }

    /**
     * Displays a text viewer dialog with a wrap text option.
     * Shows the provided text in a scrollable text area,
     * with a View menu to toggle line wrapping.
     * The option parameter sets the initial wrap state.
     *
     * @param text - the text to display (e.g., Finalisation report or desk allocations)
     * @param title - the dialog title
     * @param option - how to handle long lines of text in the TextViewer
     * @param fileType - the file type to save the text as (e.g., EBD or TXT)
     */
    public static void showTextViewer(String text, String title,
                                      ViewerOptions option, CSSE7023.FileType fileType) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        boolean wrap = option == ViewerOptions.WRAP;
        textArea.setLineWrap(wrap);
        textArea.setWrapStyleWord(wrap);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save As...");
        fileMenu.add(saveItem);

        saveItem.setToolTipText("Save the current Exam Block to a file");
        saveItem.addActionListener(e -> {

            FileChooser dialog = new FileChooser();
            String selectedFile = dialog.save(null, fileType);

            if (!selectedFile.isEmpty()) {
                try {
                    java.nio.file.Files.write(java.nio.file.Paths.get(selectedFile),
                            text.getBytes());
                    JOptionPane.showMessageDialog(parent,
                            "File saved successfully to: " + selectedFile,
                            "Save Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(parent,
                            "Failed to save file: " + ex.getMessage(),
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);

        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem wrapTextItem = new JCheckBoxMenuItem("Wrap Text", wrap);
        wrapTextItem.addActionListener(e -> {
            boolean wrapState = wrapTextItem.isSelected();
            textArea.setLineWrap(wrapState);
            textArea.setWrapStyleWord(wrapState);
        });
        viewMenu.add(wrapTextItem);
        menuBar.add(viewMenu);

        JDialog dialog = new JDialog(parent == null
                ? null
                : SwingUtilities.getWindowAncestor(parent), title);
        dialog.setJMenuBar(menuBar);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    /**
     * Simple informational box
     *
     * @param message - what to say
     */
    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(
                parent, message, "Information", JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Prompt for a Yes/No/Cancel response from the user
     *
     * @param message - text to display
     * @return return code
     */
    public static int askQuestion(String message) {
        return JOptionPane.showConfirmDialog(
                parent,
                message,
                "Question",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
    }
}
