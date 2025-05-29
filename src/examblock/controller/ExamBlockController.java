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

        // Initialize with some default data or load from file
        initializeDefaultData();

        view.setVisible(true);
    }

    /**
     * Initializes the application with some default data.
     */
    private void initializeDefaultData() {
        // Create some sample subjects - make sure they register themselves
        Subject math = new Subject("Mathematics Methods", "Advanced mathematics for Year 12 students.", model.getRegistry());
        Subject physics = new Subject("Physics", "Study of matter, energy, and their interactions.", model.getRegistry());
        Subject chemistry = new Subject("Chemistry", "Study of matter and chemical reactions.", model.getRegistry());

        // Now add them to the subject list (they are already registered)
        model.getSubjects().addSubject(math);
        model.getSubjects().addSubject(physics);
        model.getSubjects().addSubject(chemistry);

        // Create some sample exams - make sure they register themselves too
        Exam mathExam = new Exam(math, Exam.ExamType.EXTERNAL, 15, 11, 2025, 9, 0, model.getRegistry());
        Exam physicsExam = new Exam(physics, Exam.ExamType.EXTERNAL, 18, 11, 2025, 13, 30, model.getRegistry());

        model.getExams().add(mathExam);
        model.getExams().add(physicsExam);

        // Create some sample rooms and venues
        Room r1 = new Room("R1", model.getRegistry());
        Room r2 = new Room("R2", model.getRegistry());

        RoomList venueRooms = new RoomList(model.getRegistry());
        venueRooms.add(r1);

        Venue venue1 = new Venue("V1", 1, venueRooms, 10, 8, 80, false);
        // Register the venue manually since constructor doesn't do it
        model.getRegistry().add(venue1, Venue.class);
        model.getVenues().addVenue(venue1);

        model.notifyObservers("initialized");
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
            model.addObserver(this);
            initializeDefaultData(); 
            view.updateView();
        }
    }

    /**
     * Handles the Open menu action.
     */
    private void handleOpen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Exam Block Data");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Exam Block Data Files (*.ebd)", "ebd"));

        int result = fileChooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (model.loadFromFile(selectedFile.getAbsolutePath())) {
                view.updateView();
                DialogUtils.showMessage("File loaded successfully: " + selectedFile.getName());
            }
        }
    }

    /**
     * Handles the Save menu action.
     */
    private void handleSave() {
        double version = model.getVersionNumber();
        if (!CSSE7023.isBadVersion(version)) {
            model.setVersion(version);
            if (model.saveToFile(model.getRegistry(), null, model.getTitle(), version)) {
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
     * Handles the Schedule Exam action.
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
        boolean isAaraExam = DialogUtils.askQuestion("Is this an AARA exam?") == JOptionPane.YES_OPTION;

        if (!selectedVenue.checkVenueType(isAaraExam)) {
            return; // Message already displayed by checkVenueType
        }

        // Attempt to schedule the exam
        boolean scheduled = SessionHandler.scheduleExam(model, selectedExam, selectedVenue, isAaraExam);

        if (scheduled) {
            view.updateView();
            DialogUtils.showMessage("Exam scheduled successfully!");
        }
    }

    /**
     * Handles the Finalise action.
     */
    private void handleFinalise() {
        int result = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to finalise the exam block? This will allocate students to desks.",
                "Finalise Exam Block",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            SessionHandler.finaliseExamBlock(model);
            view.updateView();
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