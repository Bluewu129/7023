package examblock.view.components;

import examblock.model.CSSE7023;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;


/**
 * A save dialog that prompts for overwrite,
 * as well as ensuring newVersion is greater than oldVersion
 * [13] Dialog implementation for file operations.
 * [17] Spinner components for date/time input.
 */
public class FileChooser {

    /**
     * The title of the Exam Block, not the filename.
     * This is used to display in the dialog interface and helps users identify
     * which exam block they are working with.
     */
    private String title;

    /**
     * The existing version of the Exam Block being saved.
     * This version number is used as a reference point to ensure that any new version
     * must be greater than this old version.
     */
    private double version;

    /**
     * The accessory panel component that provides additional UI elements.
     */
    private JPanel accessoryPanel;

    /**
     * The text field component for entering and displaying the exam block title.
     * Users can input or modify the title of the exam block through this field.
     */
    private JTextField titleField;

    /**
     * The spinner field component for entering and adjusting version numbers.
     * This component allows users to input version numbers with decimal precision
     * and provides up/down buttons for easy version number adjustment.
     * [17] Date input spinner functionality.
     */
    private DoubleSpinnerField versionField;

    /**
     * A flag indicating whether to ignore accessory panel functionality.
     */
    private final boolean ignoreAccessories;

    /**
     * The warning label displayed when there are title-related issues.
     * It provides immediate feedback to help users correct title input errors.
     */
    private JLabel titleWarning;

    /**
     * The warning label displayed when there are version-related issues.
     * It provides immediate feedback to help users enter valid version numbers.
     */
    private JLabel versionWarning;

    /**
     * The static parent component reference for centering dialogs.
     * This component serves as the reference point for positioning dialog windows
     * and ensures that file chooser dialogs appear centered relative to the parent.
     */
    private static Component parent = null;

    /**
     * Create a new FileChooser
     *
     * @param title            - the name of the Exam Block, not the filename.
     *                         This is used to display
     * @param oldVersion       - the existing version of the Exam Block being saved.
     *                         The new version must be greater than the old version.
     * @param suggestedVersion - this value is placed into the textbox as a default.
     *                         The user can override this, but the new version must be
     *                         greater than the old.
     */
    public FileChooser(String title, double oldVersion, double suggestedVersion) {
        this.title = title == null ? "" : title;
        this.version = oldVersion;
        this.ignoreAccessories = CSSE7023.isBadVersion(oldVersion);
        accessoryPanel = createAccessoryPanel();
        if (!CSSE7023.isBadVersion(suggestedVersion)) {
            suggestedVersion(suggestedVersion);
        }
    }

    /**
     * Create a new FileChooser
     *
     * @param title      - the name of the Exam Block, not the filename. This is used to display
     * @param oldVersion - the existing version of the Exam Block being saved.
     */
    public FileChooser(String title, double oldVersion) {
        this(title, oldVersion, CSSE7023.BAD_VERSION);
    }

    /**
     * Create a new FileChooser with empty title and version,
     * used when those values aren't and the accessory panel is not needed.
     */
    public FileChooser() {
        this(null, CSSE7023.BAD_VERSION, CSSE7023.BAD_VERSION);
    }

    /**
     * set the frame for dialogs
     *
     * @param frame - where we should be centered
     */
    public static void setParent(Component frame) {
        FileChooser.parent = frame;
    }

    /**
     * getter
     *
     * @return new Title
     */
    public String title() {
        return title;
    }

    /**
     * getter
     *
     * @return new Version
     */
    public double version() {
        return versionField.getDouble();
    }

    /**
     * If you want to set an acceptable version number that the user can just accept,
     * call this with the version you want. The dialog will not allow the user
     * to save a file with a lower version number.
     *
     * @param suggestedVersion - the version the user can accept as-is.
     */
    public void suggestedVersion(double suggestedVersion) {
        version = suggestedVersion - 1;
        versionField.setDouble(suggestedVersion);
    }

    /**
     * On linux (at least) the save file dialog does not protect you
     * from overwriting existing files. This method will prompt the user
     * to confirm the overwrite.
     * [13] File chooser dialog implementation.
     *[14] Input validation for file operations and version control.
     *
     * @param hint     - initial suggested filename, or null (or empty) for none
     * @param fileType - Select this file filter by default.
     * @return the selected new filename, or an empty string if none chosen.
     */
    public String save(String hint, CSSE7023.FileType fileType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        JButton saveButton = findSaveButton(fileChooser);
        if (saveButton == null) {
            JOptionPane.showMessageDialog(parent,
                    "Could not find Save button in file chooser",
                    "Internal Error",
                    JOptionPane.ERROR_MESSAGE);
            return "";
        }

        saveButton.setEnabled(this.ignoreAccessories);

        FileNameExtensionFilter ebdFilter = new FileNameExtensionFilter(
                "Exam Block Files (*.ebd)", "ebd");
        fileChooser.addChoosableFileFilter(ebdFilter);
        
        
        FileNameExtensionFilter ebrFilter = new FileNameExtensionFilter(
                "Exam Block Finalise Reports (*.efr)", "efr");
        fileChooser.addChoosableFileFilter(ebrFilter);
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter(
                "Text Files (*.txt)", "txt");
        fileChooser.addChoosableFileFilter(txtFilter);

        FileNameExtensionFilter selectedFilter;
        switch (fileType) {
            case EBD:
                selectedFilter = ebdFilter;
                break;
            case EFR:
                selectedFilter = ebrFilter;
                break;
            case TXT:
                selectedFilter = txtFilter;
                break;
            default:
                selectedFilter = ebdFilter;
                break;
        }
        fileChooser.setFileFilter(selectedFilter);

        if (!this.ignoreAccessories) {
            fileChooser.setAccessory(accessoryPanel);
        }

        fileChooser.setCurrentDirectory(new File("."));

        if (hint != null && !hint.isEmpty()) {
            fileChooser.setSelectedFile(new File(hint));
        }

        Runnable updateValidation = () -> {
            boolean isTitleValid = !titleField.getText().trim().isEmpty();
            titleWarning.setVisible(!isTitleValid);

            boolean isVersionValid = false;
            if (!versionField.getText().isEmpty()) {
                double newVersion = versionField.getDouble();
                isVersionValid = newVersion > this.version;
            }
            versionWarning.setVisible(!isVersionValid);

            saveButton.setEnabled(this.ignoreAccessories || (isTitleValid
                    && isVersionValid));
        };

        if (!ignoreAccessories) {
            updateValidation.run();
            titleField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                private void update() {
                    updateValidation.run();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });

            versionField.getDocument().addDocumentListener(new DocumentListener() {
                private void update() {
                    updateValidation.run();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });

            titleField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateValidation.run();
                }
            });

            versionField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateValidation.run();
                }
            });
        }

        fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                    updateValidation.run();
                }
            }
        });

        while (true) {
            int result = fileChooser.showSaveDialog(parent);
            if (result != JFileChooser.APPROVE_OPTION) {
                return "";
            }

            File selectedFile = fileChooser.getSelectedFile();
            String selectedName = selectedFile.getAbsolutePath();
            int extensionIndex = selectedFile.getName().lastIndexOf(".");
            if (extensionIndex == -1) {
                String extensionDescription =
                        fileChooser.getFileFilter().getDescription();
                String newExtension = switch (extensionDescription) {
                    case "Exam Block Files (*.ebd)" -> ".ebd";
                    case "Exam Block Finalise Reports (*.efr)" -> ".efr";
                    case "Text Files (*.txt)" -> ".txt";
                    default -> "";
                };
                selectedFile = new File(selectedFile.getParent(),
                        selectedFile.getName()
                                + newExtension);
                selectedName = selectedFile.getAbsolutePath();
            }

            if (selectedFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(parent,
                        "File already exists. Do you want to overwrite it?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_CANCEL_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.out.println("Saving file: " + selectedName);
                    return selectedName;
                }
            } else {
                System.out.println("Saving file: " + selectedName);
                return selectedName;
            }
        }
    }

    /**
     * Display the load/open dialog, and return the selected filename.
     *
     * @param hint     - initial suggested filename, or null (or empty) for none
     * @param fileType - Select the this ile filter by default.
     * @return the selected existing file as a File, or null if none chosen.
     */
    public File open(String hint, CSSE7023.FileType fileType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        FileNameExtensionFilter ebdFilter = new FileNameExtensionFilter(
                "Exam Block Files (*.ebd)", "ebd");
        fileChooser.addChoosableFileFilter(ebdFilter);
        FileNameExtensionFilter ebrFilter = new FileNameExtensionFilter(
                "Exam Block Finalise Reports (*.efr)", "efr");
        fileChooser.addChoosableFileFilter(ebrFilter);
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter(
                "Text Files (*.txt)", "txt");
        fileChooser.addChoosableFileFilter(txtFilter);

        FileNameExtensionFilter selectedFilter;
        switch (fileType) {
            case EBD:
                selectedFilter = ebdFilter;
                break;
            case EFR:
                selectedFilter = ebrFilter;
                break;
            case TXT:
                selectedFilter = txtFilter;
                break;
            default:
                selectedFilter = ebdFilter;
                break;
        }
        fileChooser.setFileFilter(selectedFilter);

        fileChooser.setCurrentDirectory(new File("."));

        if (hint != null && !hint.isEmpty()) {
            fileChooser.setSelectedFile(new File(hint));
        }

        int result = fileChooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selectedFile = fileChooser.getSelectedFile();
        int extensionIndex = selectedFile.getName().lastIndexOf(".");
        if (extensionIndex == -1) {
            String extensionDescription =
                    fileChooser.getFileFilter().getDescription();
            String newExtension = switch (extensionDescription) {
                case "Exam Block Files (*.ebd)" -> ".ebd";
                case "Exam Block Finalise Reports (*.efr)" -> ".efr";
                case "Text Files (*.txt)" -> ".txt";
                default -> "";
            };
            selectedFile = new File(selectedFile.getParent(), selectedFile.getName()
                    + newExtension);
        }

        return selectedFile;
    }

    // [14] Input validation setup for file chooser dialog.
    private JPanel createAccessoryPanel() {
        JPanel accessoryPanel = new JPanel(new GridBagLayout());
        accessoryPanel.setBorder(BorderFactory.createTitledBorder("Exam Block Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);

        JLabel titleLabel = new JLabel("Title: ");
        accessoryPanel.add(titleLabel, gbc);

        gbc.gridx++;
        titleField = new JTextField(12);
        titleField.setText(this.title);
        accessoryPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        titleWarning = new JLabel("Title cannot be empty");
        titleWarning.setForeground(Color.RED);
        titleWarning.setVisible(this.title.trim().isEmpty());

        JPanel titleWarningPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleWarningPanel.add(titleWarning);
        titleWarningPanel.setPreferredSize(titleWarning.getPreferredSize());
        accessoryPanel.add(titleWarningPanel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;

        JLabel versionLabel = new JLabel("Version:");
        accessoryPanel.add(versionLabel, gbc);

        gbc.gridx = 1;
        versionField = new DoubleSpinnerField();
        versionField.setMinimum(version);
        versionField.setColumns(12);
        versionField.setDouble(this.version);
        accessoryPanel.add(versionField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        versionWarning = new JLabel("Version must be greater than "
                + String.format("%.1f", version));
        versionWarning.setForeground(Color.RED);
        versionWarning.setVisible(versionField.getText().isEmpty()
                || versionField.getDouble() <= version);

        JPanel versionWarningPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        versionWarningPanel.add(versionWarning);
        versionWarningPanel.setPreferredSize(versionWarning.getPreferredSize());
        accessoryPanel.add(versionWarningPanel, gbc);

        return accessoryPanel;
    }

    private JButton findSaveButton(Component comp) {
        if (comp instanceof JButton) {
            JButton button = (JButton) comp;
            String actionCommand = button.getActionCommand();
            String text = button.getText();

            if (JFileChooser.APPROVE_SELECTION.equals(actionCommand)
                    || "Save".equals(actionCommand)) {
                System.out.println("Save button found via action command: "
                        + actionCommand);
                return button;
            }

            String saveButtonText =
                    UIManager.getString("FileChooser.saveButtonText");
            if (saveButtonText != null && saveButtonText.equals(text)) {
                System.out.println("Save button found via UIManager text: " + text);
                return button;
            }

            if (text != null && text.toLowerCase().contains("save")) {
                System.out.println("Save button found via fallback text: " + text);
                return button;
            }
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                JButton found = findSaveButton(child);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}