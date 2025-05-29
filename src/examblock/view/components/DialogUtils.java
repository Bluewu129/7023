package examblock.view.components;

import examblock.model.CSSE7023;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class for displaying dialogs and messages.
 */
public class DialogUtils {

    /**
     * Options for text viewer dialogs.
     */
    public enum ViewerOptions {
        SCROLL, NO_SCROLL, WRAP, SCROLL_WRAP
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

        // Create text area
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        // Set initial wrap state
        boolean initialWrap = (options == ViewerOptions.WRAP || options == ViewerOptions.SCROLL_WRAP);
        textArea.setLineWrap(initialWrap);
        textArea.setWrapStyleWord(initialWrap);

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        if (options == ViewerOptions.SCROLL || options == ViewerOptions.SCROLL_WRAP) {
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveTextToFile(text, fileType));
        fileMenu.add(saveAsItem);
        menuBar.add(fileMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem wrapTextItem = new JCheckBoxMenuItem("Wrap Text", initialWrap);
        wrapTextItem.addActionListener(e -> {
            boolean wrap = wrapTextItem.isSelected();
            textArea.setLineWrap(wrap);
            textArea.setWrapStyleWord(wrap);
        });
        viewMenu.add(wrapTextItem);
        menuBar.add(viewMenu);

        dialog.setJMenuBar(menuBar);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Add close button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Save text to a file.
     */
    private static void saveTextToFile(String text, CSSE7023.FileType fileType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        // Set up file filter
        String extension = fileType.getExtension();
        String description = switch (fileType) {
            case EBD -> "Exam Block Files (*.ebd)";
            case EFR -> "Exam Block Finalise Reports (*.efr)";
            case TXT -> "Text Files (*.txt)";
        };

        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(description, extension));

        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Add extension if missing
            if (!file.getName().toLowerCase().endsWith("." + extension)) {
                file = new File(file.getAbsolutePath() + "." + extension);
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(text);
                showMessage("File saved successfully!");
            } catch (IOException ex) {
                showError("Error saving file: " + ex.getMessage());
            }
        }
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