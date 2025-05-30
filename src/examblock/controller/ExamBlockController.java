package examblock.controller;

import examblock.given.SessionHandler;
import examblock.model.*;
import examblock.view.ExamBlockView;
import examblock.view.components.DialogUtils;
import examblock.view.components.Verbose;

import javax.swing.*;
import java.io.File;

/**
 * Main controller to coordinate between model and view
 * As per specification - requires no-args constructor
 */
public class ExamBlockController {

    private ExamBlockModel model;
    private ExamBlockView view;
    private String latestFinaliseReport = "";

    /**
     * Constructs a new ExamBlockController.
     * As per specification - no parameters
     */
    public ExamBlockController() {
        model = new ExamBlockModel();

        // Create view with the model's registry
        view = new ExamBlockView(model.getRegistry());

        // Set up the relationships
        view.setController(this);
        model.addObserver(new ModelObserver() {
            @Override
            public void modelChanged(String property) {
                SwingUtilities.invokeLater(() -> view.updateView());
            }
        });

        view.setVisible(true);

        // Show file open dialog on startup as per workflow
        Timer timer = new Timer(100, e -> {
            ((Timer)e.getSource()).stop();
            handleOpen();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Method to handle actions
     */
    public void handleAction(String command) {
        switch (command) {
            case "New":
                handleNew();
                break;
            case "Open":
                handleOpen();
                break;
            case "Save":
                handleSave();
                break;
            case "Exit":
                handleExit();
                break;
            case "Schedule Exam":
            case "Add":
                handleScheduleExam();
                break;
            case "Finalise":
                handleFinalise();
                break;
            case "About":
                handleAbout();
                break;
            case "Verbose":
                handleVerbose();
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
    }

    /**
     * Handles the New menu action.
     */
    private void handleNew() {
        int result = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to create a new exam block? All unsaved changes will be lost.",
                "New Exam Block",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            model = new ExamBlockModel();
            model.addObserver(new ModelObserver() {
                @Override
                public void modelChanged(String property) {
                    SwingUtilities.invokeLater(() -> view.updateView());
                }
            });
            view.updateView();
        }
    }

    /**
     * Handles the Open menu action.
     */
    private void handleOpen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open");

        // Set up file filters
        fileChooser.setAcceptAllFileFilterUsed(true);
        javax.swing.filechooser.FileNameExtensionFilter ebdFilter =
                new javax.swing.filechooser.FileNameExtensionFilter("Exam Block Files (*.ebd)", "ebd");
        fileChooser.addChoosableFileFilter(ebdFilter);
        fileChooser.setFileFilter(ebdFilter);

        fileChooser.setCurrentDirectory(new File("."));

        File defaultFile = new File("2025s1a2");
        if (defaultFile.exists()) {
            fileChooser.setSelectedFile(defaultFile);
        }

        int result = fileChooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            ExamBlockModel newModel = new ExamBlockModel();
            newModel.loadFromFile(null, selectedFile.getAbsolutePath());

            model = newModel;
            model.addObserver(new ModelObserver() {
                @Override
                public void modelChanged(String property) {
                    SwingUtilities.invokeLater(() -> view.updateView());
                }
            });
            view.updateView();

            String title = "Exam Block Manager - " + model.getTitle() + " (v" + model.getVersion() + ")";
            view.setTitle(title);

            if (Verbose.isVerbose()) {
                DialogUtils.showMessage("File loaded successfully: " + selectedFile.getName());
            }
        } else if (result == JFileChooser.CANCEL_OPTION) {
            if (model.getSubjects().size() == 0 &&
                    model.getExams().size() == 0 &&
                    model.getVenues().size() == 0) {
                view.updateView();
            }
        }
    }

    /**
     * Handles the Save menu action.
     */
    private void handleSave() {
        JDialog saveDialog = new JDialog(view, "Save", true);
        saveDialog.setLayout(new BoxLayout(saveDialog.getContentPane(), BoxLayout.Y_AXIS));
        saveDialog.setSize(400, 200);
        saveDialog.setLocationRelativeTo(view);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(model.getTitle(), 20);
        titlePanel.add(titleField);
        panel.add(titlePanel);

        JPanel versionPanel = new JPanel();
        versionPanel.add(new JLabel("Version:"));
        JTextField versionField = new JTextField(String.format("%.1f", model.getVersion() + 0.1), 10);
        versionPanel.add(versionField);
        JLabel versionWarning = new JLabel("Version must be greater than " + model.getVersion());
        versionWarning.setForeground(java.awt.Color.RED);
        panel.add(versionPanel);
        panel.add(versionWarning);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                double newVersion = Double.parseDouble(versionField.getText());
                if (newVersion <= model.getVersion()) {
                    versionWarning.setVisible(true);
                    return;
                }

                String newTitle = titleField.getText().trim();
                if (newTitle.isEmpty()) {
                    DialogUtils.showMessage("Title cannot be empty");
                    return;
                }

                model.setTitle(newTitle);
                model.setVersion(newVersion);
                saveDialog.dispose();

                performSave();
            } catch (NumberFormatException ex) {
                DialogUtils.showMessage("Invalid version number format");
            }
        });

        cancelButton.addActionListener(e -> saveDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        saveDialog.add(panel);
        saveDialog.setVisible(true);
    }

    private void performSave() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Exam Block Data");

        String defaultFilename = "scheduled.ebd";
        fileChooser.setSelectedFile(new File(defaultFilename));

        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Exam Block Files (*.ebd)", "ebd"));

        int result = fileChooser.showSaveDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".ebd")) {
                file = new File(file.getAbsolutePath() + ".ebd");
            }

            if (model.saveToFile(model.getRegistry(), file.getAbsolutePath(),
                    model.getTitle(), model.getVersion())) {
                DialogUtils.showMessage("File saved successfully!");
            }
        }
    }

    /**
     * Handles the Exit menu action.
     */
    private void handleExit() {
        int result = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to exit? All unsaved changes will be lost.",
                "Exit Application",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * Handles the Add button action (Schedule Exam).
     */
    private void handleScheduleExam() {
        Exam selectedExam = view.getSelectedExam();
        Venue selectedVenue = view.getSelectedVenue();

        if (selectedExam == null) {
            DialogUtils.showMessage("Please select an exam to schedule.");
            return;
        }

        if (selectedVenue == null) {
            DialogUtils.showMessage("Please select a venue for the exam.");
            return;
        }

        int aaraStudents = 0;
        int nonAaraStudents = 0;

        for (Student student : model.getStudents().all()) {
            boolean takesSubject = false;
            for (Subject subject : student.getSubjects().all()) {
                if (subject.equals(selectedExam.getSubject())) {
                    takesSubject = true;
                    break;
                }
            }

            if (takesSubject) {
                if (student.isAara()) {
                    aaraStudents++;
                } else {
                    nonAaraStudents++;
                }
            }
        }

        boolean venueIsAara = selectedVenue.isAara();
        int expectedStudents = venueIsAara ? aaraStudents : nonAaraStudents;

        if (expectedStudents == 0) {
            String venueType = venueIsAara ? "AARA" : "non-AARA";
            DialogUtils.showMessage("No " + venueType + " students are taking " +
                    selectedExam.getSubject().getTitle() + ".");
            return;
        }

        if (venueIsAara && aaraStudents == 0) {
            DialogUtils.showMessage("This is an AARA venue but no AARA students are taking this exam.");
            return;
        }

        if (!venueIsAara && nonAaraStudents == 0) {
            DialogUtils.showMessage("This is a non-AARA venue but no non-AARA students are taking this exam.");
            return;
        }

        boolean scheduled = SessionHandler.scheduleExam(model, selectedExam, selectedVenue, venueIsAara);

        if (scheduled) {
            view.updateView();
            if (Verbose.isVerbose()) {
                DialogUtils.showMessage("Exam scheduled successfully!");
            }
        }
    }

    /**
     * Handles the Finalise action.
     */
    private void handleFinalise() {
        boolean allScheduled = true;
        for (Exam exam : model.getExams().all()) {
            boolean found = false;
            for (Session session : model.getSessions().all()) {
                if (session.getExams().contains(exam)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                allScheduled = false;
                break;
            }
        }

        if (!allScheduled) {
            DialogUtils.showMessage("Not all exams have been scheduled. Please schedule all exams before finalising.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save");

        JPanel accessoryPanel = new JPanel();
        accessoryPanel.setLayout(new BoxLayout(accessoryPanel, BoxLayout.Y_AXIS));
        accessoryPanel.setBorder(BorderFactory.createTitledBorder("Exam Block Details"));

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(model.getTitle(), 15);
        titlePanel.add(titleField);
        accessoryPanel.add(titlePanel);

        JPanel versionPanel = new JPanel();
        versionPanel.add(new JLabel("Version:"));
        JTextField versionField = new JTextField(String.format("%.1f", model.getVersion() + 0.2), 10);
        versionPanel.add(versionField);
        accessoryPanel.add(versionPanel);

        fileChooser.setAccessory(accessoryPanel);
        fileChooser.setSelectedFile(new File("finalised.ebd"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Exam Block Files (*.ebd)", "ebd"));

        int result = fileChooser.showSaveDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                double newVersion = Double.parseDouble(versionField.getText());
                model.setVersion(newVersion);

                SessionHandler.finaliseExamBlock(model);

                latestFinaliseReport = SessionHandler.printEverything(model);

                view.updateView();
                view.setLatestFinaliseReport(latestFinaliseReport);

                StringBuilder deskAllocations = new StringBuilder();
                deskAllocations.append("Desk Allocations\n");
                deskAllocations.append("================\n\n");

                for (Session session : model.getSessions().all()) {
                    Venue venue = session.getVenue();
                    deskAllocations.append("-".repeat(75)).append("\n");
                    deskAllocations.append(venue.venueId());
                    if (venue.isAara()) {
                        deskAllocations.append(" (").append(venue.deskCount()).append(" AARA desks)");
                    } else {
                        deskAllocations.append(" (").append(venue.deskCount()).append(" Non-AARA desks)");
                    }
                    deskAllocations.append("\n");
                    deskAllocations.append("-".repeat(75)).append("\n");
                    deskAllocations.append(session.toString()).append("\n\n");

                    session.printDesks();
                }

                view.setLatestDeskAllocations(deskAllocations.toString());
            } catch (NumberFormatException e) {
                DialogUtils.showMessage("Invalid version number");
            }
        }
    }

    /**
     * Handles the About menu action.
     */
    private void handleAbout() {
        String aboutText = "ExamBlock Application\n" +
                "Version " + model.getVersion() + "\n" +
                "CSSE7023 Assignment 2\n" +
                "University of Queensland";
        DialogUtils.showMessage(aboutText);
    }

    /**
     * Handles the Verbose toggle action.
     */
    private void handleVerbose() {
        boolean currentVerbose = Verbose.isVerbose();
        Verbose.setVerbose(!currentVerbose);
        view.updateVerboseStatus(!currentVerbose);

        String status = !currentVerbose ? "enabled" : "disabled";
        if (!currentVerbose) {
            DialogUtils.showMessage("Verbose output " + status);
        }
    }

    /**
     * Gets the current model.
     */
    public ExamBlockModel getModel() {
        return model;
    }
}
