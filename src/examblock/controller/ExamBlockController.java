package examblock.controller;

import examblock.given.SessionHandler;
import examblock.model.*;
import examblock.view.ExamBlockView;
import examblock.view.components.DialogUtils;
import examblock.view.components.Verbose;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Controller class for the ExamBlock application.
 * Handles communication between the model and view.
 */
public class ExamBlockController implements ActionListener, ModelObserver {

    private ExamBlockModel model;
    private ExamBlockView view;

    /**
     * Constructs a new ExamBlockController.
     */
    public ExamBlockController() {
        model = new ExamBlockModel();
        view = new ExamBlockView(this);
        model.addObserver(this);

        view.setVisible(true);

        // Show file open dialog on startup as per workflow
        SwingUtilities.invokeLater(() -> {
            handleOpen();
        });
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
            model.addObserver(this);
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
        fileChooser.setAcceptAllFileFilterUsed(false);
        javax.swing.filechooser.FileNameExtensionFilter ebdFilter =
                new javax.swing.filechooser.FileNameExtensionFilter("Exam Block Files (*.ebd)", "ebd");
        fileChooser.addChoosableFileFilter(ebdFilter);
        fileChooser.setFileFilter(ebdFilter);

        // Set default directory to current directory
        fileChooser.setCurrentDirectory(new File("."));

        int result = fileChooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Create new model for loading
            ExamBlockModel newModel = new ExamBlockModel();

            if (newModel.loadFromFile(selectedFile.getAbsolutePath())) {
                // Successfully loaded, replace the model
                if (model != null) {
                    model.removeObserver(this);
                }
                model = newModel;
                model.addObserver(this);
                view.updateView();

                // Update window title with loaded file info
                String title = "Exam Block Manager - " + model.getTitle() + " (v" + model.getVersion() + ")";
                view.setTitle(title);

                if (Verbose.isVerbose()) {
                    DialogUtils.showMessage("File loaded successfully: " + selectedFile.getName());
                }
            } else {
                // Load failed, keep existing model
                DialogUtils.showError("Failed to load file: " + selectedFile.getName());
            }
        } else if (result == JFileChooser.CANCEL_OPTION) {
            // User cancelled - if no data loaded, disable most UI elements
            if (model.getSubjects().size() == 0 &&
                    model.getExams().size() == 0 &&
                    model.getVenues().size() == 0) {
                // The view should handle enabling/disabling controls based on empty model
                view.updateView();
            }
        }
    }

    /**
     * Handles the Save menu action.
     */
    private void handleSave() {
        // Get version number
        double newVersion = model.getVersion() + 0.1; // Default increment

        String versionInput = JOptionPane.showInputDialog(view,
                "Enter new version number (current: " + model.getVersion() + "):",
                "Save - Version Number",
                JOptionPane.QUESTION_MESSAGE);

        if (versionInput != null && !versionInput.trim().isEmpty()) {
            try {
                newVersion = Double.parseDouble(versionInput.trim());
                if (newVersion <= model.getVersion()) {
                    DialogUtils.showWarning("Version must be greater than " + model.getVersion());
                    return;
                }
            } catch (NumberFormatException e) {
                DialogUtils.showError("Invalid version number format");
                return;
            }
        } else {
            return; // User cancelled
        }

        model.setVersion(newVersion);

        // Show save dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Exam Block Data");
        fileChooser.setSelectedFile(new File(model.getTitle() + " (v" + newVersion + ").ebd"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Exam Block Files (*.ebd)", "ebd"));

        int result = fileChooser.showSaveDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure .ebd extension
            if (!file.getName().toLowerCase().endsWith(".ebd")) {
                file = new File(file.getAbsolutePath() + ".ebd");
            }

            if (model.saveToFile(model.getRegistry(), file.getAbsolutePath(),
                    model.getTitle(), newVersion)) {
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
        // Get selected exam and venue from view
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

        // Check if venue type matches requirements (AARA or regular)
        boolean needsAara = false;
        // Count AARA students for this exam
        for (Student student : model.getStudents().all()) {
            if (student.isAara() && student.getSubjects().all().contains(selectedExam.getSubject())) {
                needsAara = true;
                break;
            }
        }

        // Attempt to schedule the exam
        boolean scheduled = SessionHandler.scheduleExam(model, selectedExam, selectedVenue, needsAara);

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
        // Check if all exams have been scheduled
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
            DialogUtils.showWarning("Not all exams have been scheduled. Please schedule all exams before finalising.");
            return;
        }

        // Finalise will trigger save dialog through SessionHandler
        SessionHandler.finaliseExamBlock(model);
        view.updateView();
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
        if (!currentVerbose) { // Now enabling verbose
            DialogUtils.showMessage("Verbose output " + status);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

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

    @Override
    public void modelChanged(String property) {
        // Update view when model changes
        SwingUtilities.invokeLater(() -> view.updateView());
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public ExamBlockModel getModel() {
        return model;
    }
}