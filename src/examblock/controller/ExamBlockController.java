package examblock.controller;

import examblock.given.SessionHandler;
import examblock.model.*;
import examblock.view.ExamBlockView;
import examblock.view.components.DialogUtils;
import examblock.view.components.FileChooser;
import examblock.view.components.Verbose;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * Primary controller coordinating interactions between model and view components.
 * Implements the Controller layer of MVC architecture with Swing GUI integration.
 * Manages user interactions through ActionListeners and maintains button state consistency.
 */
public class ExamBlockController {

    /**
     * The model component containing all exam block data
     */
    private ExamBlockModel model;

    /**
     * The view component managing the user interface
     */
    private ExamBlockView view;

    /**
     * Initializes the controller and establishes the complete MVC architecture.
     * Creates model and view components, configures event handling, sets up menus,
     * and displays the initial file dialog as required by application workflow.
     */
    public ExamBlockController() {
        model = new ExamBlockModel();
        view = new ExamBlockView(model.getRegistry());
        view.setModel(model);

        addListeners();
        setupMenus();
        initializeView();
        view.display();

        // Display initial file dialog as required by workflow step 1
        showInitialFileDialog();
    }

    /**
     * Configures event listeners for all view components.
     * Establishes button listeners and selection change handlers for proper state management.
     */
    private void addListeners() {
        view.addFinaliseButtonListener(new FinaliseButtonListener());
        view.addAddButtonListener(new AddButtonListener());
        view.addClearButtonListener(new ClearButtonListener());

        // Enhanced button state management - synchronize with selection changes
        view.getExamTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        view.getTree().getSelectionModel().addTreeSelectionListener(e -> updateButtonStates());
    }

    /**
     * Establishes initial view state with proper button accessibility.
     * Disables action buttons until valid data is loaded according to workflow requirements.
     */
    private void initializeView() {
        // Initially disable all action buttons - only File/Load and File/Exit should be enabled
        view.getAddButton().setEnabled(false);
        view.getClearButton().setEnabled(false);
        view.getFinaliseButton().setEnabled(false);

        updateViewFromModel();
    }

    /**
     * Creates and configures the complete menu system.
     * Implements menu structure with File operations
     * and View options using proper ActionListener organization.
     */
    private void setupMenus() {
        final JFrame frame = view.getFrame();
        final JMenuBar menuBar = new JMenuBar();

        // File menu construction
        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("Load...");
        loadItem.addActionListener(e -> loadFile());
        fileMenu.add(loadItem);

        fileMenu.addSeparator();

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveFile());
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As");
        saveAsItem.addActionListener(e -> saveAsFile());
        fileMenu.add(saveAsItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // View menu construction
        JMenu viewMenu = new JMenu("View");

        JMenuItem deskAllocationsItem = new JMenuItem("Desk Allocations...");
        deskAllocationsItem.addActionListener(e -> showDeskAllocations());
        viewMenu.add(deskAllocationsItem);

        JMenuItem finaliseReportsItem = new JMenuItem("Finalise Reports...");
        finaliseReportsItem.addActionListener(e -> showFinaliseReports());
        viewMenu.add(finaliseReportsItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        frame.setJMenuBar(menuBar);
    }

    /**
     * Displays the initial file dialog as mandated by workflow step 1.
     * The application must show the File Open dialog on startup.
     */
    private void showInitialFileDialog() {
        loadFile();
    }

    /**
     * Handles file loading operation with user confirmation for data replacement.
     * Checks for existing data and prompts user before clearing current state.
     */
    private void loadFile() {
        boolean hasExistingData = model.getExams().size() > 0
                ||
                model.getStudents().size() > 0
                ||
                model.getSubjects().size() > 0;

        if (hasExistingData) {
            int result = DialogUtils.askQuestion("Loading a new file will " 
                    +
                    "clear all existing data. Continue?");
            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            // Clear all existing model data
            model.getRegistry().clear();
            model.getSubjects().clear();
            model.getUnits().clear();
            model.getStudents().clear();
            model.getExams().clear();
            model.getRooms().clear();
            model.getVenues().clear();
            model.getSessions().clear();
            model.setTitle("ExamBlock Data 2025");
            model.setVersion(1.3);
            model.setFilename("");
        }

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.open("", CSSE7023.FileType.EBD);

        if (selectedFile != null) {
            try {
                model.loadFromFile(model.getRegistry(), selectedFile.getAbsolutePath());
                updateViewFromModel();
                updateWindowTitle();
                updateButtonStates(); // Update button states after loading data
            } catch (Exception e) {
                DialogUtils.showMessage("Error loading file: " + e.getMessage());
            }
        }
    }

    /**
     * Saves the current file or prompts for Save As if no filename exists.
     * Automatically increments version number for existing files.
     */
    private void saveFile() {
        String currentFilename = model.getFilename();
        if (currentFilename == null || currentFilename.isEmpty()) {
            saveAsFile();
        } else {
            double newVersion = model.getVersion() + 0.1;
            boolean success = model.saveToFile(model.getRegistry(), 
                    currentFilename, model.getTitle(), newVersion);
            if (success) {
                updateWindowTitle();
            }
        }
    }

    /**
     * Displays Save As dialog with version management and file selection.
     * Allows user to specify new filename, title, and version number.
     */
    private void saveAsFile() {
        FileChooser fileChooser = new FileChooser(model.getTitle(), model.getVersion(), 
                model.getVersion() + 0.1);
        String selectedFile = fileChooser.save(model.getFilename(), CSSE7023.FileType.EBD);

        if (!selectedFile.isEmpty()) {
            String title = fileChooser.title();
            double version = fileChooser.version();

            boolean success = model.saveToFile(model.getRegistry(), selectedFile, title, version);
            if (success) {
                model.setFilename(selectedFile);
                model.setTitle(title);
                updateWindowTitle();
            }
        }
    }

    /**
     * Displays finalise reports in a text viewer dialog.
     * Generates comprehensive report using SessionHandler and presents in scrollable format.
     */
    private void showFinaliseReports() {
        String report = generateFinalisationReport();
        DialogUtils.showTextViewer(report, "Finalisation Report", 
                DialogUtils.ViewerOptions.WRAP, CSSE7023.FileType.EFR);
    }

    /**
     * Refreshes all view components with current model data.
     * Synchronizes view state with model across all tabs and components.
     */
    private void updateViewFromModel() {
        view.updateExamTable(model.getExams());
        view.updateTree(model.getSessions(), model.getVenues());
        view.updateSubjectPage(model.getSubjects());
        view.updateExamPage(model.getExams());
        view.updateUnitPage(model.getUnits());
        view.updateStudentPage(model.getStudents());
        view.updateRoomPage(model.getRooms());
        view.updateVenuPage(model.getVenues());

        // Populate exam mapping for quick access
        for (int i = 0; i < model.getExams().size(); i++) {
            view.addExamToExamMap(i, model.getExams().all().get(i));
        }
    }

    /**
     * Updates button availability based on current selection state.
     * Implements enhanced button state management 
     * according to workflow requirements.
     */
    private void updateButtonStates() {
        // Get current selection state
        boolean examSelected = view.getSelectedExamRows() != null 
                && view.getSelectedExamRows().length > 0;
        DefaultMutableTreeNode selectedTreeNode = view.getSelectedTreeNode();

        boolean venueSelected = selectedTreeNode != null 
                && view.getVenueFromVenueNodeMap(selectedTreeNode) != null;
        boolean sessionSelected = selectedTreeNode != null 
                && view.getSessionFromSessionNodeMap(selectedTreeNode) != null;

        // Add button: enabled when both exam and (venue or session) are selected
        view.getAddButton().setEnabled(examSelected && (venueSelected || sessionSelected));

        // Clear button: enabled when any selection exists OR when sessions exist
        boolean hasSelections = examSelected || selectedTreeNode != null;
        boolean hasSessions = model.getSessions().size() > 0;
        view.getClearButton().setEnabled(hasSelections || hasSessions);

        // Finalise button: enabled when sessions exist
        view.getFinaliseButton().setEnabled(model.getSessions().size() > 0);
    }

    /**
     * Updates the window title with current file information and version.
     * Displays exam block title and filename in the title bar.
     */
    private void updateWindowTitle() {
        String title = "Exam Block Manager";
        if (!model.getTitle().isEmpty()) {
            title += " - " + model.getTitle();
        }
        if (!model.getFilename().isEmpty()) {
            title += " (" + new File(model.getFilename()).getName() + ")";
        }
        view.setTitle(title);
        view.setVersion(model.getVersion());
    }

    /**
     * Generates comprehensive finalisation report using SessionHandler.
     * Creates detailed report containing all exam block information.
     */
    private String generateFinalisationReport() {
        return SessionHandler.printEverything(model);
    }

    /**
     * Saves finalisation report to timestamped file.
     * Automatically generates filename in format: ExamBlockReport-YYYY-MM-DD_HH-MM-SS.efr
     * Saves in same directory as data file or current directory if no data file exists.
     */
    private void saveFinaliseReport() {
        try {
            // Generate timestamp-based filename according to workflow requirements
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = "ExamBlockReport-" + timestamp + ".efr";

            String reportContent = generateFinalisationReport();

            // Determine output directory based on data file location
            String dataFileDir = ".";
            if (!model.getFilename().isEmpty()) {
                File dataFile = new File(model.getFilename());
                dataFileDir = dataFile.getParent();
                if (dataFileDir == null) {
                    dataFileDir = ".";
                }
            }

            String fullPath = new File(dataFileDir, fileName).getAbsolutePath();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath))) {
                writer.write(reportContent);
            }

            if (Verbose.isVerbose()) {
                DialogUtils.showMessage("Exam Block Finalise Report saved to file: " + fileName);
            }
            System.out.println("Finalisation report saved to: " + fullPath);

        } catch (Exception e) {
            DialogUtils.showMessage("Unable to write Finalise Report to file: " + e.getMessage());
        }
    }

    /**
     * Displays desk allocations in a text viewer with formatted layout.
     * Shows current desk assignments across all venues and sessions.
     */
    private void showDeskAllocations() {
        StringBuilder sb = new StringBuilder();
        model.getVenues().writeAllocations(sb, model.getSessions());
        DialogUtils.showTextViewer(sb.toString(), "Desk Allocations", 
                DialogUtils.ViewerOptions.WRAP, CSSE7023.FileType.TXT);
    }

    /**
     * Handles finalisation button actions with comprehensive workflow management.
     * Performs student allocation, prompts for file save, generates reports, and updates display.
     * Implements enhanced confirmation dialog and auto-save functionality.
     */
    private class FinaliseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.getSessions().size() == 0) {
                DialogUtils.showMessage("No sessions to finalise.");
                return;
            }

            // Allocate students to desks first
            model.getVenues().allocateStudents(model.getSessions(), 
                    model.getExams(), model.getStudents());

            // Show file save dialog for finalised data
            FileChooser fileChooser = new FileChooser(model.getTitle(), 
                    model.getVersion(), model.getVersion() + 0.1);
            String selectedFile = fileChooser.save("finalised.ebd", CSSE7023.FileType.EBD);

            if (!selectedFile.isEmpty()) {
                String title = fileChooser.title();
                double version = fileChooser.version();
                boolean success = model.saveToFile(model.getRegistry(), 
                        selectedFile, title, version);

                if (success) {
                    model.setFilename(selectedFile);
                    model.setTitle(title);
                    updateWindowTitle();

                    // Automatically save finalisation report with timestamp
                    saveFinaliseReport();

                    updateViewFromModel();

                    // Show report viewer after finalisation
                    String report = generateFinalisationReport();
                    DialogUtils.showTextViewer(report, "Exam Block Viewer", 
                            DialogUtils.ViewerOptions.WRAP, CSSE7023.FileType.EFR);
                }
            }
        }
    }

    /**
     * Handles add button actions for scheduling exams into sessions or venues.
     * Provides enhanced confirmation dialogs and capacity validation as per workflow requirements.
     */
    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = view.getSelectedExamRows();
            DefaultMutableTreeNode selectedNode = view.getSelectedTreeNode();

            if (selectedRows == null || selectedNode == null) {
                return;
            }

            Exam selectedExam = view.getExamFromExamMap(selectedRows[0]);
            if (selectedExam == null) {
                return;
            }

            Venue venue = view.getVenueFromVenueNodeMap(selectedNode);
            Session existingSession = view.getSessionFromSessionNodeMap(selectedNode);

            if (venue != null) {
                // Creating new session in venue
                int studentsForThisVenue;
                if (venue.isAara()) {
                    studentsForThisVenue = model.getStudents()
                            .countStudents(selectedExam.getSubject(), true);
                } else {
                    studentsForThisVenue = model.getStudents()
                            .countStudents(selectedExam.getSubject(), false);
                }

                int totalInSession = model.getSessions().getSessionNewTotal(
                       venue, selectedExam, studentsForThisVenue);

                if (!venue.willFit(totalInSession)) {
                    view.removeAllSelections();
                    updateButtonStates();
                    return;
                }

                // Enhanced confirmation dialog as per workflow requirements
                String message = "CONFIRM scheduling the " + selectedExam
                        .getSubject().getTitle() + " exam into " + venue.venueId();

                int result = DialogUtils.askQuestion(message);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        LocalDate day = selectedExam.getDate();
                        LocalTime time = selectedExam.getTime();
                        int sessionNumber = model.getSessions().getSessionNumber(venue, day, time);

                        if (sessionNumber == 0) {
                            model.getSessions()
                                    .getSessionNewTotal(venue, selectedExam, studentsForThisVenue);
                            sessionNumber = model.getSessions().getSessionNumber(venue, day, time);
                        }

                        Session session = model.getSessions().getSession(venue, sessionNumber);
                        session.scheduleExam(selectedExam);
                        updateViewFromModel();

                    } catch (Exception ex) {
                        DialogUtils.showMessage("Error scheduling exam: " + ex.getMessage());
                    }
                }

            } else if (existingSession != null) {
                // Adding to existing session
                Venue sessionVenue = existingSession.getVenue();
                int studentsForThisExam;
                if (sessionVenue.isAara()) {
                    studentsForThisExam = model.getStudents()
                            .countStudents(selectedExam.getSubject(), true);
                } else {
                    studentsForThisExam = model.getStudents()
                            .countStudents(selectedExam.getSubject(), false);
                }

                int currentStudents = existingSession.countStudents();
                int totalStudents = currentStudents + studentsForThisExam;

                if (!sessionVenue.willFit(totalStudents)) {
                    view.removeAllSelections();
                    updateButtonStates();
                    return;
                }

                // Enhanced confirmation dialog for adding to existing session
                String message = "Confirm adding  " + selectedExam.getSubject().getTitle() 
                        +
                        " exam to existing session " + existingSession 
                        +
                        " (adding " + studentsForThisExam + " students)";

                int result = DialogUtils.askQuestion(message);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        existingSession.scheduleExam(selectedExam);
                        updateViewFromModel();

                    } catch (Exception ex) {
                        DialogUtils.showMessage("Error adding exam to session: " + ex.getMessage());
                    }
                }

            } else {
                DialogUtils.showMessage("Please select a venue or existing session.");
            }

            // Clear selections and update button states after operation
            view.removeAllSelections();
            updateButtonStates();
        }
    }

    /**
     * Handles clear button actions to remove sessions or clear selections.
     * Provides options to clear selections only or remove existing sessions.
     */
    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Check what is currently selected and what exists
            boolean examSelected = view.getSelectedExamRows() != null 
                    && view.getSelectedExamRows().length > 0;
            DefaultMutableTreeNode selectedTreeNode = view.getSelectedTreeNode();
            boolean hasAnySelection = examSelected || selectedTreeNode != null;
            boolean hasSessions = model.getSessions().size() > 0;

            if (!hasAnySelection && !hasSessions) {
                // Nothing selected and no sessions exist
                DialogUtils.showMessage("Nothing to clear.");
                return;
            }

            // If sessions exist, offer to remove them
            if (hasSessions) {
                showClearOptionsDialog(hasAnySelection);
            } else {
                // Only selections exist, just clear them
                clearSelectionsOnly();
            }
        }

        /**
         * Shows dialog with options for what to clear.
         */
        private void showClearOptionsDialog(boolean hasSelections) {
            StringBuilder message = new StringBuilder();
            message.append("Choose what to clear:\n\n");

            if (hasSelections) {
                message.append("• Current selections in the interface\n");
            }

            int sessionCount = model.getSessions().size();
            message.append("• All existing sessions (")
                    .append(sessionCount).append(" session(s))\n\n");
            message.append("What would you like to do?");

            String[] options = new String[]{"Remove All Sessions", "Cancel"};

            int choice = JOptionPane.showOptionDialog(
                    view.getFrame(),
                    message.toString(),
                    "Clear Options",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[options.length - 1] // Default to Cancel
            );

            switch (choice) {
                case 0: // Remove All Sessions
                    removeAllSessions();
                    break;
                default: // Cancel or close
                    break;
            }
        }

        /**
         * Removes all sessions from the model and updates the view.
         */
        private void removeAllSessions() {
            int sessionCount = model.getSessions().size();

            // Confirm deletion
            String confirmMessage = "Are you sure you want to" 
                    +
                    " remove all " 
                    + sessionCount + " session(s)?\n\n" 
                    +
                    "This action cannot be undone.";

            int confirm = JOptionPane.showConfirmDialog(
                    view.getFrame(),
                    confirmMessage,
                    "Confirm Remove All Sessions",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // Clear all sessions from the sessions list
                    model.getSessions().clear();

                    // Clear all sessions from the registry
                    model.getRegistry().removeAll(Session.class);

                    // Clear any selections
                    view.removeAllSelections();

                    // Update the view to reflect the changes
                    updateViewFromModel();
                    updateButtonStates();

                    // Show success message
                    DialogUtils.showMessage("All " 
                            + sessionCount 
                            + " session(s) have been removed successfully.");

                    System.out.println("All sessions removed by user.");

                } catch (Exception ex) {
                    DialogUtils.showMessage("Error removing sessions: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        /**
         * Clears only the current selections without removing any sessions.
         */
        private void clearSelectionsOnly() {
            // Clear all selections
            view.removeAllSelections();

            // Update button states
            updateButtonStates();

            // Show confirmation
            DialogUtils.showMessage("Selections have been cleared.");

            System.out.println("Selections cleared by user.");
        }
    }
}