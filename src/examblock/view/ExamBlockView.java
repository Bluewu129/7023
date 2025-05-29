package examblock.view;

import examblock.controller.ExamBlockController;
import examblock.model.*;
import examblock.view.components.DialogUtils;
import examblock.view.components.FileChooser;
import examblock.view.components.Verbose;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main view class for the ExamBlock application.
 * Provides the GUI interface for exam scheduling.
 */
public class ExamBlockView extends JFrame {

    private ExamBlockController controller;
    private JTable examTable;
    private JTree sessionVenueTree;
    private DefaultTableModel examTableModel;
    private DefaultTreeModel treeModel;

    // Bottom tabbed pane and tables
    private JTabbedPane bottomTabbedPane;
    private JTable subjectsTable, examsTable, unitsTable, studentsTable, roomsTable, venuesTable;

    // Buttons that need to be enabled/disabled
    private JButton finaliseButton;
    private JButton addButton;
    private JButton clearButton;

    // Menu items that need to be enabled/disabled
    private JMenuItem saveMenuItem;
    private JCheckBoxMenuItem verboseMenuItem;
    private JMenuItem deskAllocationsMenuItem;
    private JMenuItem finaliseReportsMenuItem;

    // Store latest desk allocations and finalise report
    private String latestDeskAllocations = "";
    private String latestFinaliseReport = "";

    /**
     * Constructs a new ExamBlockView.
     *
     * @param controller the controller for this view
     */
    public ExamBlockView(ExamBlockController controller) {
        this.controller = controller;

        // Set FileChooser parent
        FileChooser.setParent(this);

        initializeComponents();
        layoutComponents();
        setupMenus();

        // Initial update
        updateView();
    }

    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setTitle("Exam Block Manager");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Initialize table model with column headers
        String[] columnNames = {"Int.", "Subject", "Date", "Time", "AARA", "Non."};
        examTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        // Initialize exam table
        examTable = new JTable(examTableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examTable.setRowHeight(20);

        // Set column widths
        examTable.getColumnModel().getColumn(0).setPreferredWidth(30);  // Int.
        examTable.getColumnModel().getColumn(0).setMaxWidth(40);
        examTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Subject
        examTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Date
        examTable.getColumnModel().getColumn(3).setPreferredWidth(50);  // Time
        examTable.getColumnModel().getColumn(4).setPreferredWidth(50);  // AARA
        examTable.getColumnModel().getColumn(5).setPreferredWidth(40);  // Non.
        examTable.getColumnModel().getColumn(5).setMaxWidth(50);

        // Center align checkbox and number columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        examTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        examTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        examTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Initialize tree
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Sessions");
        treeModel = new DefaultTreeModel(rootNode);
        sessionVenueTree = new JTree(treeModel);
        sessionVenueTree.setRootVisible(false);
        sessionVenueTree.setShowsRootHandles(true);

        // Initialize buttons
        finaliseButton = new JButton("Finalise");
        finaliseButton.setActionCommand("Finalise");
        finaliseButton.addActionListener(controller);
        finaliseButton.setEnabled(false);

        addButton = new JButton("Add");
        addButton.setEnabled(false);
        addButton.addActionListener(e -> handleAdd());

        clearButton = new JButton("Clear");
        clearButton.setEnabled(false);
        clearButton.addActionListener(e -> handleClear());

        // Add window close listener
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                controller.actionPerformed(new java.awt.event.ActionEvent(this,
                        java.awt.event.ActionEvent.ACTION_PERFORMED, "Exit"));
            }
        });

        // Add selection listeners
        examTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        sessionVenueTree.addTreeSelectionListener(e -> updateButtonStates());
    }

    /**
     * Lays out all GUI components.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));

        // Main panel with three sections
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 1. Select an Exam/Unit panel
        JPanel examPanel = new JPanel(new BorderLayout());
        examPanel.setBorder(new TitledBorder("1. Select an Exam / Unit"));

        JScrollPane examScrollPane = new JScrollPane(examTable);
        examScrollPane.setPreferredSize(new Dimension(300, 200));
        examPanel.add(examScrollPane, BorderLayout.CENTER);
        mainPanel.add(examPanel);

        // 2. Select a Session/Venue panel
        JPanel sessionVenuePanel = new JPanel(new BorderLayout());
        sessionVenuePanel.setBorder(new TitledBorder("2. Select a Session / Venue"));

        JScrollPane treeScrollPane = new JScrollPane(sessionVenueTree);
        treeScrollPane.setPreferredSize(new Dimension(300, 200));
        sessionVenuePanel.add(treeScrollPane, BorderLayout.CENTER);
        mainPanel.add(sessionVenuePanel);

        // 3. Go panel with buttons
        JPanel goPanel = new JPanel(new BorderLayout());
        goPanel.setBorder(new TitledBorder("3. Go"));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(finaliseButton);
        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);

        goPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(goPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Create bottom tabbed pane
        createBottomTabbedPane();
        add(bottomTabbedPane, BorderLayout.SOUTH);
    }

    /**
     * Creates the bottom tabbed pane with all tables.
     */
    private void createBottomTabbedPane() {
        bottomTabbedPane = new JTabbedPane();
        bottomTabbedPane.setPreferredSize(new Dimension(1200, 300));

        // Subjects tab
        String[] subjectColumns = {"Title", "Description"};
        DefaultTableModel subjectsModel = new DefaultTableModel(subjectColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        subjectsTable = new JTable(subjectsModel);
        subjectsTable.setRowHeight(20);
        JScrollPane subjectsScroll = new JScrollPane(subjectsTable);
        bottomTabbedPane.addTab("Subjects", subjectsScroll);

        // Exams tab
        String[] examColumns = {"Subject", "Type", "Paper", "Subtitle", "Unit", "Date", "Time"};
        DefaultTableModel examsModel = new DefaultTableModel(examColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        examsTable = new JTable(examsModel);
        examsTable.setRowHeight(20);
        JScrollPane examsScroll = new JScrollPane(examsTable);
        bottomTabbedPane.addTab("Exams", examsScroll);

        // Units tab
        String[] unitColumns = {"Subject", "UnitID", "Title"};
        DefaultTableModel unitsModel = new DefaultTableModel(unitColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        unitsTable = new JTable(unitsModel);
        unitsTable.setRowHeight(20);
        JScrollPane unitsScroll = new JScrollPane(unitsTable);
        bottomTabbedPane.addTab("Units", unitsScroll);

        // Students tab
        String[] studentColumns = {"Full Name", "LUI", "Date of Birth", "AARA", "Subjects"};
        DefaultTableModel studentsModel = new DefaultTableModel(studentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(studentsModel);
        studentsTable.setRowHeight(20);
        JScrollPane studentsScroll = new JScrollPane(studentsTable);
        bottomTabbedPane.addTab("Students", studentsScroll);

        // Rooms tab
        String[] roomColumns = {"Room ID"};
        DefaultTableModel roomsModel = new DefaultTableModel(roomColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomsTable = new JTable(roomsModel);
        roomsTable.setRowHeight(20);
        JScrollPane roomsScroll = new JScrollPane(roomsTable);
        bottomTabbedPane.addTab("Rooms", roomsScroll);

        // Venues tab
        String[] venueColumns = {"Venue", "Rooms", "Rows", "Col", "Desks", "AARA"};
        DefaultTableModel venuesModel = new DefaultTableModel(venueColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        venuesTable = new JTable(venuesModel);
        venuesTable.setRowHeight(20);
        JScrollPane venuesScroll = new JScrollPane(venuesTable);
        bottomTabbedPane.addTab("Venues", venuesScroll);
    }

    /**
     * Sets up the menu bar.
     */
    private void setupMenus() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem openItem = new JMenuItem("Open");
        openItem.setActionCommand("Open");
        openItem.addActionListener(controller);
        fileMenu.add(openItem);

        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setActionCommand("Save");
        saveMenuItem.addActionListener(controller);
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setActionCommand("Exit");
        exitItem.addActionListener(controller);
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");

        verboseMenuItem = new JCheckBoxMenuItem("Verbose Output");
        verboseMenuItem.setActionCommand("Verbose");
        verboseMenuItem.addActionListener(controller);
        viewMenu.add(verboseMenuItem);

        viewMenu.addSeparator();

        // Desk Allocations menu item
        deskAllocationsMenuItem = new JMenuItem("Desk Allocations");
        deskAllocationsMenuItem.setEnabled(false);
        deskAllocationsMenuItem.addActionListener(e -> showDeskAllocations());
        viewMenu.add(deskAllocationsMenuItem);

        // Finalise Reports menu item
        finaliseReportsMenuItem = new JMenuItem("Finalise Reports");
        finaliseReportsMenuItem.setEnabled(false);
        finaliseReportsMenuItem.addActionListener(e -> showFinaliseReport());
        viewMenu.add(finaliseReportsMenuItem);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Shows the desk allocations in a viewer.
     */
    private void showDeskAllocations() {
        if (!latestDeskAllocations.isEmpty()) {
            DialogUtils.showTextViewer(latestDeskAllocations, "Desk Allocations",
                    DialogUtils.ViewerOptions.SCROLL_WRAP, CSSE7023.FileType.TXT);
        } else {
            // Generate desk allocations from current sessions
            StringBuilder sb = new StringBuilder();
            ExamBlockModel model = controller.getModel();

            sb.append("Desk Allocations\n");
            sb.append("================\n\n");

            for (Session session : model.getSessions().all()) {
                sb.append("Session: ").append(session.getVenue().venueId());
                sb.append(" - ").append(session.getDate());
                sb.append(" at ").append(session.getTime()).append("\n");
                sb.append("-".repeat(50)).append("\n");

                // Add desk layout info
                writeDeskLayout(sb, session);

                sb.append("\n");
            }

            DialogUtils.showTextViewer(sb.toString(), "Desk Allocations",
                    DialogUtils.ViewerOptions.SCROLL_WRAP, CSSE7023.FileType.TXT);
        }
    }

    /**
     * Shows the finalise report in a viewer.
     */
    private void showFinaliseReport() {
        if (!latestFinaliseReport.isEmpty()) {
            DialogUtils.showTextViewer(latestFinaliseReport, "Exam Block Finalise Report",
                    DialogUtils.ViewerOptions.SCROLL_WRAP, CSSE7023.FileType.EFR);
        } else {
            DialogUtils.showMessage("No finalise report available. Please finalise the exam block first.");
        }
    }

    /**
     * Write desk layout for a session.
     */
    private void writeDeskLayout(StringBuilder sb, Session session) {
        int rows = session.getVenue().getRows();
        int columns = session.getVenue().getColumns();

        // Simple representation of desk layout
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int deskNum = i * columns + j + 1;
                sb.append(String.format("Desk %2d  ", deskNum));
            }
            sb.append("\n");
        }
    }

    /**
     * Updates the view with current model data.
     */
    public void updateView() {
        if (controller == null || controller.getModel() == null) {
            return;
        }

        ExamBlockModel model = controller.getModel();

        // Update window title
        String title = "Exam Block Manager - " + model.getTitle() + " (v" + String.format("%.1f", model.getVersion()) + ")";
        setTitle(title);

        // Update exam table
        updateExamTable(model);

        // Update session/venue tree
        updateSessionVenueTree();

        // Update bottom tabs
        updateBottomTabs();

        // Check if data is loaded
        boolean hasData = model.getExams().size() > 0 ||
                model.getVenues().size() > 0 ||
                model.getSessions().all().size() > 0;

        // Enable/disable controls based on data availability
        if (saveMenuItem != null) {
            saveMenuItem.setEnabled(hasData);
        }

        // Finalise button is enabled when there are sessions
        finaliseButton.setEnabled(model.getSessions().all().size() > 0);

        // Enable desk allocations menu if there are sessions
        deskAllocationsMenuItem.setEnabled(model.getSessions().all().size() > 0);

        // Update button states based on selections
        updateButtonStates();

        // Force repaint
        revalidate();
        repaint();
    }

    /**
     * Updates the exam table.
     */
    private void updateExamTable(ExamBlockModel model) {
        examTableModel.setRowCount(0);

        for (Exam exam : model.getExams().all()) {
            // Count AARA and non-AARA students for this exam
            int aaraCount = 0;
            int nonAaraCount = 0;
            for (Student student : model.getStudents().all()) {
                // Check if student has this subject
                boolean hasSubject = false;
                for (Subject subject : student.getSubjectsList()) {
                    if (subject.equals(exam.getSubject())) {
                        hasSubject = true;
                        break;
                    }
                }

                if (hasSubject) {
                    if (student.isAara()) {
                        aaraCount++;
                    } else {
                        nonAaraCount++;
                    }
                }
            }

            Object[] rowData = {
                    exam.getExamType() == Exam.ExamType.INTERNAL ? "✓" : "",
                    exam.getSubject().getTitle(),
                    exam.getDate().toString(),
                    exam.getTime().toString(),
                    String.valueOf(aaraCount),
                    String.valueOf(nonAaraCount)
            };

            examTableModel.addRow(rowData);
        }
    }

    /**
     * Updates the bottom tabbed pane with current data.
     */
    private void updateBottomTabs() {
        if (controller == null || controller.getModel() == null) {
            return;
        }

        ExamBlockModel model = controller.getModel();

        // Update Subjects tab
        DefaultTableModel subjectsModel = (DefaultTableModel) subjectsTable.getModel();
        subjectsModel.setRowCount(0);
        for (Subject subject : model.getSubjects().all()) {
            subjectsModel.addRow(subject.toTableRow());
        }

        // Update Exams tab
        DefaultTableModel examsModel = (DefaultTableModel) examsTable.getModel();
        examsModel.setRowCount(0);
        for (Exam exam : model.getExams().all()) {
            Object[] row = exam.toLongTableRow();
            // Ensure we have all columns
            Object[] fullRow = new Object[7];
            for (int i = 0; i < Math.min(row.length, 7); i++) {
                fullRow[i] = row[i];
            }
            // Fill any missing columns
            if (fullRow[2] == null) fullRow[2] = ""; // Paper
            if (fullRow[3] == null) fullRow[3] = ""; // Subtitle
            if (fullRow[4] == null) fullRow[4] = ""; // Unit
            examsModel.addRow(fullRow);
        }

        // Update Units tab
        DefaultTableModel unitsModel = (DefaultTableModel) unitsTable.getModel();
        unitsModel.setRowCount(0);
        for (Unit unit : model.getUnits().all()) {
            unitsModel.addRow(unit.toTableRow());
        }

        // Update Students tab - match the workflow format
        DefaultTableModel studentsModel = (DefaultTableModel) studentsTable.getModel();
        studentsModel.setRowCount(0);
        for (Student student : model.getStudents().all()) {
            // Build subjects string
            StringBuilder subjects = new StringBuilder();
            boolean first = true;
            for (Subject subject : student.getSubjectsList()) {
                if (!first) subjects.append(", ");
                subjects.append(subject.getTitle());
                first = false;
            }

            Object[] row = {
                    student.fullName(),
                    student.getLui(),
                    student.getDob().toString(),
                    student.isAara() ? "true" : "false",
                    subjects.toString()
            };
            studentsModel.addRow(row);
        }

        // Update Rooms tab
        DefaultTableModel roomsModel = (DefaultTableModel) roomsTable.getModel();
        roomsModel.setRowCount(0);
        for (Room room : model.getRooms().all()) {
            if (!(room instanceof Venue)) { // Only show actual rooms, not venues
                roomsModel.addRow(room.toTableRow());
            }
        }

        // Update Venues tab - match the workflow format
        DefaultTableModel venuesModel = (DefaultTableModel) venuesTable.getModel();
        venuesModel.setRowCount(0);
        for (Venue venue : model.getVenues().all()) {
            // Build rooms string
            StringBuilder rooms = new StringBuilder();
            boolean first = true;
            for (Room room : venue.getRooms().all()) {
                if (!first) rooms.append(" ");
                rooms.append(room.roomId());
                first = false;
            }

            Object[] row = {
                    venue.venueId(),
                    rooms.toString(),
                    String.valueOf(venue.getRows()),
                    String.valueOf(venue.getColumns()),
                    String.valueOf(venue.deskCount()),
                    venue.isAara() ? "✓" : ""
            };
            venuesModel.addRow(row);
        }
    }

    /**
     * Updates the session/venue tree structure.
     */
    private void updateSessionVenueTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();

        ExamBlockModel model = controller.getModel();

        // Add existing sessions if any
        if (model.getSessions().all().size() > 0) {
            DefaultMutableTreeNode existingNode = new DefaultMutableTreeNode("Existing sessions (" +
                    model.getSessions().all().size() + ")");

            for (Session session : model.getSessions().all()) {
                int usedDesks = session.countStudents();
                int totalDesks = session.getVenue().deskCount();
                int availableDesks = totalDesks - usedDesks;

                String sessionText = session.getDate() + " at " + session.getTime() +
                        " in " + session.getVenue().venueId() +
                        " (" + availableDesks + " of " + totalDesks + " desks available)";
                DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(sessionText);

                // Add "Exams (N)" node
                DefaultMutableTreeNode examsNode = new DefaultMutableTreeNode("Exams (" +
                        session.getExams().size() + ")");

                // Add each exam
                for (Exam exam : session.getExams()) {
                    // Get the student count for this specific exam
                    int studentCount = session.getStudentCountForExam(exam);

                    String examText = exam.getSubject().getTitle() + " (" + studentCount + " students)";
                    examsNode.add(new DefaultMutableTreeNode(examText));

                    if (Verbose.isVerbose()) {
                        System.out.println("Exam: " + exam.getSubject().getTitle() +
                                " has " + studentCount + " students in session");
                    }
                }

                sessionNode.add(examsNode);
                existingNode.add(sessionNode);
            }
            root.add(existingNode);
        }

        // Add "Create a new session" node
        DefaultMutableTreeNode createNewNode = new DefaultMutableTreeNode("Create a new session");
        for (Venue venue : model.getVenues().all()) {
            String venueText = venue.venueId() + " (" + venue.deskCount() + " ";
            if (venue.isAara()) {
                venueText += "AARA desks)";
            } else {
                venueText += "Non-AARA desks)";
            }
            createNewNode.add(new DefaultMutableTreeNode(venueText));
        }
        root.add(createNewNode);

        // Reload and expand
        treeModel.reload();

        // Expand first level only
        if (root.getChildCount() > 0) {
            TreePath path = new TreePath(new Object[]{root, root.getChildAt(0)});
            sessionVenueTree.expandPath(path);
        }
        if (root.getChildCount() > 1) {
            TreePath path = new TreePath(new Object[]{root, root.getChildAt(1)});
            sessionVenueTree.expandPath(path);
        }
    }

    /**
     * Updates button states based on current selections.
     */
    private void updateButtonStates() {
        Exam selectedExam = getSelectedExam();
        Venue selectedVenue = getSelectedVenue();
        Session selectedSession = getSelectedSession();

        // Add button is enabled when exam and venue are selected
        addButton.setEnabled(selectedExam != null && selectedVenue != null);

        // Clear button is enabled when an exam is selected (Test 5)
        clearButton.setEnabled(selectedExam != null);
    }

    /**
     * Handles the Add button action.
     */
    private void handleAdd() {
        controller.actionPerformed(new java.awt.event.ActionEvent(this,
                java.awt.event.ActionEvent.ACTION_PERFORMED, "Schedule Exam"));
    }

    /**
     * Handles the Clear button action.
     */
    private void handleClear() {
        // Clear the exam selection (Test 6)
        examTable.clearSelection();
        updateButtonStates();
    }

    /**
     * Gets the currently selected exam.
     */
    public Exam getSelectedExam() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < controller.getModel().getExams().all().size()) {
            return controller.getModel().getExams().all().get(selectedRow);
        }
        return null;
    }

    /**
     * Gets the currently selected venue from the tree.
     */
    public Venue getSelectedVenue() {
        if (sessionVenueTree.getSelectionPath() == null) {
            return null;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                sessionVenueTree.getSelectionPath().getLastPathComponent();

        if (node == null || node.getParent() == null) {
            return null;
        }

        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent.getUserObject().equals("Create a new session")) {
            // Extract venue ID from the node text
            String nodeText = node.getUserObject().toString();
            int spaceIndex = nodeText.indexOf(" ");
            if (spaceIndex > 0) {
                String venueId = nodeText.substring(0, spaceIndex);

                // Find the venue
                ExamBlockModel model = controller.getModel();
                for (Venue venue : model.getVenues().all()) {
                    if (venue.venueId().equals(venueId)) {
                        return venue;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the currently selected session from the tree.
     */
    public Session getSelectedSession() {
        if (sessionVenueTree.getSelectionPath() == null) {
            return null;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                sessionVenueTree.getSelectionPath().getLastPathComponent();

        if (node == null || node.getParent() == null) {
            return null;
        }

        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        String parentText = parent.getUserObject().toString();

        if (parentText.startsWith("Existing sessions")) {
            // This is a session node
            String nodeText = node.getUserObject().toString();

            // Parse: "2025-03-10 at 12:30 in V1+V2+V3 (X of Y desks available)"
            String[] parts = nodeText.split(" at ");
            if (parts.length >= 2) {
                String date = parts[0].trim();
                String[] timeParts = parts[1].split(" in ");
                if (timeParts.length >= 2) {
                    String time = timeParts[0].trim();
                    String venuePart = timeParts[1].split(" \\(")[0].trim();

                    // Find matching session
                    ExamBlockModel model = controller.getModel();
                    for (Session session : model.getSessions().all()) {
                        if (session.getDate().toString().equals(date) &&
                                session.getTime().toString().equals(time) &&
                                session.getVenue().venueId().equals(venuePart)) {
                            return session;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Updates the verbose status in the menu.
     */
    public void updateVerboseStatus(boolean verbose) {
        verboseMenuItem.setSelected(verbose);
    }

    /**
     * Sets the latest desk allocations data.
     */
    public void setLatestDeskAllocations(String deskAllocations) {
        this.latestDeskAllocations = deskAllocations;
        deskAllocationsMenuItem.setEnabled(true);
    }

    /**
     * Sets the latest finalise report data.
     */
    public void setLatestFinaliseReport(String report) {
        this.latestFinaliseReport = report;
        finaliseReportsMenuItem.setEnabled(true);
    }
}