package examblock.controller;

import examblock.model.*;
import examblock.view.ExamBlockView;
import examblock.view.components.*;
import examblock.given.SessionHandler;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;
import examblock.view.components.Verbose;

/**
 * Main controller to coordinate between model and view
 * [3][4] MVC architecture with Swing GUI components.
 * [6] ActionListener placement following MVC design principles.
 */
public class ExamBlockController {

    /**
     * The model component of the MVC architecture.
     */
    private ExamBlockModel model;
    /**
     * The view component of the MVC architecture.
     */
    private ExamBlockView view;

    /**
     * The C in MVC.
     */
    public ExamBlockController() {

        model = new ExamBlockModel();
        view = new ExamBlockView(model.getRegistry());
        view.setModel(model);

        addListeners();
        setupMenus();
        initializeView();
        view.display();
        showInitialFileDialog();
    }

    /**
     * Add all the listeners to the view
     */
    private void addListeners() {
        view.addFinaliseButtonListener(new FinaliseButtonListener());
        view.addAddButtonListener(new AddButtonListener());
        view.addClearButtonListener(new ClearButtonListener());
        view.getExamTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        view.getTree().getSelectionModel().addTreeSelectionListener(e -> {
            updateButtonStates();
        });
    }

    /**
     * Initialize the view with current model data
     */
    private void initializeView() {
        view.getAddButton().setEnabled(false);
        view.getClearButton().setEnabled(false);
        view.getFinaliseButton().setEnabled(false);

        updateViewFromModel();
    }

    /**
     * Construct and install the menu items
     * [12] Menu system implementation.
     * [6] ActionListener organization in MVC pattern.
     */
    private void setupMenus() {
        final JFrame frame = view.getFrame();
        final JMenuBar menuBar = new JMenuBar();

        // File
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

        // View
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
     * Show initial file dialog on startup
     */
    private void showInitialFileDialog() {
        loadFile();
    }

    /**
     * Load file dialog and processing
     */
    private void loadFile() {
        boolean hasExistingData = model.getExams().size() > 0
                || model.getStudents().size() > 0
                || model.getSubjects().size() > 0;
        if (hasExistingData) {
            int result = DialogUtils.askQuestion(
                    "Loading a new file will clear all existing data. Continue?");
            if (result != JOptionPane.YES_OPTION) {
                return;
            }

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
                updateButtonStates();
            } catch (Exception e) {
                DialogUtils.showMessage("Error loading file: " + e.getMessage());
            }
        }
    }

    /**
     * Save current file
     */
    private void saveFile() {
        String currentFilename = model.getFilename();
        if (currentFilename == null || currentFilename.isEmpty()) {
            saveAsFile();
        } else {
            double newVersion = model.getVersion() + 0.1;
            boolean success = model.saveToFile(model.getRegistry(), currentFilename,
                    model.getTitle(), newVersion);
            if (success) {
                updateWindowTitle();
            }
        }
    }

    /**
     * Save As dialog and processing
     */
    private void saveAsFile() {
        FileChooser fileChooser = new FileChooser(model.getTitle(),
                model.getVersion(),
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
     * Show finalise reports viewer
     */
    private void showFinaliseReports() {
        String report = generateFinalisationReport();
        DialogUtils.showTextViewer(report,
                "Finalisation Report",
                DialogUtils.ViewerOptions.WRAP,
                CSSE7023.FileType.EFR);
    }

    /**
     * Update view from model data
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

        for (int i = 0; i < model.getExams().size(); i++) {
            view.addExamToExamMap(i, model.getExams().all().get(i));
        }
    }

    /**
     * Update button enabled states based on selections
     */
    private void updateButtonStates() {
        boolean examSelected = view.getSelectedExamRows() != null;
        DefaultMutableTreeNode selectedTreeNode = view.getSelectedTreeNode();

        boolean venueSelected = selectedTreeNode != null
                && view.getVenueFromVenueNodeMap(selectedTreeNode) != null;
        boolean sessionSelected = selectedTreeNode != null
                && view.getSessionFromSessionNodeMap(selectedTreeNode) != null;

        view.getAddButton().setEnabled(examSelected && (venueSelected || sessionSelected));
        view.getClearButton().setEnabled(examSelected || selectedTreeNode != null);
        view.getFinaliseButton().setEnabled(model.getSessions().size() > 0);
    }

    /**
     * Update window title with current file info
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
     * Generate finalisation report
     */
    private String generateFinalisationReport() {
        return SessionHandler.printEverything(model);
    }

    /**
     * Save finalisation report to file
     */
    private void saveFinaliseReport() {
        try {
            String dateNow = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss")
                    .format(new Date());
            String fileName = model.getTitle()
                    + " (v" + model.getVersion() + ") "
                    + dateNow
                    + "." + CSSE7023.FileType.EFR.getExtension();

            String reportContent = generateFinalisationReport();
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


    private void showDeskAllocations() {
        StringBuilder sb = new StringBuilder();
        model.getVenues().writeAllocations(sb, model.getSessions());
        DialogUtils.showTextViewer(sb.toString(),
                "Desk Allocations",
                DialogUtils.ViewerOptions.WRAP,
                CSSE7023.FileType.TXT);
    }

    /**
     * Finalise button listener
     * [6] ActionListener organization in MVC pattern.
     */
    private class FinaliseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.getSessions().size() == 0) {
                DialogUtils.showMessage("No sessions to finalise.");
                return;
            }

            model.getVenues().allocateStudents(model.getSessions(),
                    model.getExams(),
                    model.getStudents());

            FileChooser fileChooser = new FileChooser(model.getTitle(),
                    model.getVersion(),
                    model.getVersion() + 0.1);
            String selectedFile = fileChooser.save("finalised.ebd",
                    CSSE7023.FileType.EBD);

            if (!selectedFile.isEmpty()) {
                String title = fileChooser.title();
                double version = fileChooser.version();
                boolean success = model.saveToFile(model.getRegistry(),
                        selectedFile, title, version);
                if (success) {
                    model.setFilename(selectedFile);
                    model.setTitle(title);
                    updateWindowTitle();
                    saveFinaliseReport();
                    updateViewFromModel();

                    String report = generateFinalisationReport();
                    DialogUtils.showTextViewer(report,
                            "Exam Block Viewer",
                            DialogUtils.ViewerOptions.WRAP,
                            CSSE7023.FileType.EFR);
                }
            }
        }
    }

    /**
     * Add button listener
     * [6] Event handling in controller layer.
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
                int studentsForThisVenue;
                if (venue.isAara()) {
                    studentsForThisVenue = model.getStudents()
                                    .countStudents(selectedExam.getSubject(), true);
                } else {
                    studentsForThisVenue = model.getStudents()
                                    .countStudents(selectedExam.getSubject(), false);
                }
                int totalInSession = model.getSessions().getSessionNewTotal(venue,
                        selectedExam, studentsForThisVenue);
                if (!venue.willFit(totalInSession)) {
                    view.removeAllSelections();
                    updateButtonStates();
                    return;
                }

                String message = "CONFIRM scheduling the " + selectedExam
                        .getSubject().getTitle() + " exam into " + venue.venueId();

                int result = DialogUtils.askQuestion(message);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        LocalDate day = selectedExam.getDate();
                        LocalTime time = selectedExam.getTime();
                        int sessionNumber = model.getSessions()
                                        .getSessionNumber(venue, day, time);

                        if (sessionNumber == 0) {
                            model.getSessions().getSessionNewTotal(venue,
                                    selectedExam, studentsForThisVenue);
                            sessionNumber = model.getSessions()
                                            .getSessionNumber(venue, day, time);
                        }

                        Session session = model.getSessions()
                                        .getSession(venue, sessionNumber);
                        session.scheduleExam(selectedExam);
                        updateViewFromModel();

                    } catch (Exception ex) {
                        DialogUtils.showMessage("Error scheduling exam: "
                                + ex.getMessage());
                    }
                }

            } else if (existingSession != null) {
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

                String message = "CONFIRM adding the "
                        + selectedExam.getSubject().getTitle()
                        + " exam to existing session "
                        + existingSession.toString()
                        + " (adding " + studentsForThisExam + " students)";

                int result = DialogUtils.askQuestion(message);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        existingSession.scheduleExam(selectedExam);
                        updateViewFromModel();

                    } catch (Exception ex) {
                        DialogUtils.showMessage("Error adding exam to session: "
                                + ex.getMessage());
                    }
                }

            } else {
                DialogUtils.showMessage("Please select a venue or existing session.");
            }

            view.removeAllSelections();
            updateButtonStates();
        }
    }

    /**
     * Clear button listener
     */
    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.removeAllSelections();
            updateButtonStates();
        }
    }
}