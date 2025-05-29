package examblock.view;

import examblock.controller.ExamBlockController;
import examblock.model.*;
import examblock.view.components.DialogUtils;
import examblock.view.components.FileChooser;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * The V in MVC model. Drives and owns the UI
 * As per specification - requires Registry parameter
 */
public class ExamBlockView extends JFrame implements ModelObserver {

    private ExamBlockController controller;
    private Registry registry;

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
     * As per specification - requires Registry parameter
     *
     * @param registry the registry for dependencies
     */
    public ExamBlockView(Registry registry) {
        this.registry = registry;

        // Set FileChooser parent
        FileChooser.setParent(this);

        initializeComponents();
        layoutComponents();
        setupMenus();

        // Initial update - will be done when controller is set
    }

    /**
     * Sets the controller for this view.
     * Must be called after construction.
     *
     * @param controller the controller
     */
    public void setController(ExamBlockController controller) {
        this.controller = controller;

        // Add controller as action listener where needed
        if (finaliseButton != null) {
            finaliseButton.removeActionListener(null);
            finaliseButton.addActionListener(controller);
        }

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
                if (controller != null) {
                    controller.actionPerformed(new java.awt.event.ActionEvent(this,
                            java.awt.event.ActionEvent.ACTION_PERFORMED, "Exit"));
                }
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
        createBottomPanel();
        add(bottomTabbedPane, BorderLayout.SOUTH);
    }

    /**
     * Creates the bottom tabbed pane with all tables.
     * Method name as per specification
     */
    public JTabbedPane createBottomPanel() {
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

        return bottomTabbedPane;
    }

    /**
     * Creates the top panel.
     * Method name as per specification
     */
    public JPanel createTopPanel() {
        // This method is not used in the current implementation
        // but is required by the specification
        JPanel topPanel = new JPanel();
        return topPanel;
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
        if (controller != null) {
            openItem.addActionListener(controller);
        }
        fileMenu.add(openItem);

        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setActionCommand("Save");
        if (controller != null) {
            saveMenuItem.addActionListener(controller);
        }
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setActionCommand("Exit");
        if (controller != null) {
            exitItem.addActionListener(controller);
        }
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");

        verboseMenuItem = new JCheckBoxMenuItem("Verbose Output");
        verboseMenuItem.setActionCommand("Verbose");
        if (controller != null) {
            verboseMenuItem.addActionListener(controller);
        }
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
     * Display the view.
     * Method name as per specification
     */
    public void display() {
        setVisible(true);
    }

    // All other existing methods remain the same...

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

        // Update tables
        updateExamTable(model.getExams());
        updateSubjectPage(model.getSubjects());
        updateExamPage(model.getExams());
        updateUnitPage(model.getUnits());
        updateStudentPage(model.getStudents());
        updateRoomPage(model.getRooms());
        updateVenuPage(model.getVenues());

        // Update tree
        updateTree(model.getSessions(), model.getVenues());

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

    // Additional required methods as per specification

    /**
     * Sets the model for this view.
     * Method name as per specification
     */
    public void setModel(ExamBlockModel model) {
        // Model is accessed through controller
        updateView();
    }

    /**
     * Called when the model changes.
     * Method name as per specification
     */
    @Override
    public void modelChanged(String property) {
        SwingUtilities.invokeLater(() -> updateView());
    }

    /**
     * Gets the frame.
     * Method name as per specification
     */
    public JFrame getFrame() {
        return this;
    }

    /**
     * Updates the exam table.
     * Method name as per specification
     */
    public void updateExamTable(ExamList exams) {
        examTableModel.setRowCount(0);

        if (controller == null || controller.getModel() == null) {
            return;
        }

        ExamBlockModel model = controller.getModel();

        for (Exam exam : exams.all()) {
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
                    "INTERNAL",
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
     * Updates the subject page.
     * Method name as per specification
     */
    public void updateSubjectPage(SubjectList subjects) {
        DefaultTableModel subjectsModel = (DefaultTableModel) subjectsTable.getModel();
        subjectsModel.setRowCount(0);
        for (Subject subject : subjects.all()) {
            subjectsModel.addRow(subject.toTableRow());
        }
    }

    /**
     * Updates the exam page.
     * Method name as per specification
     */
    public void updateExamPage(ExamList exams) {
        DefaultTableModel examsModel = (DefaultTableModel) examsTable.getModel();
        examsModel.setRowCount(0);
        for (Exam exam : exams.all()) {
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
    }

    /**
     * Updates the unit page.
     * Method name as per specification
     */
    public void updateUnitPage(UnitList units) {
        DefaultTableModel unitsModel = (DefaultTableModel) unitsTable.getModel();
        unitsModel.setRowCount(0);
        for (Unit unit : units.all()) {
            unitsModel.addRow(unit.toTableRow());
        }
    }

    /**
     * Updates the student page.
     * Method name as per specification
     */
    public void updateStudentPage(StudentList students) {
        DefaultTableModel studentsModel = (DefaultTableModel) studentsTable.getModel();
        studentsModel.setRowCount(0);
        for (Student student : students.all()) {
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
    }

    /**
     * Updates the room page.
     * Method name as per specification
     */
    public void updateRoomPage(RoomList rooms) {
        DefaultTableModel roomsModel = (DefaultTableModel) roomsTable.getModel();
        roomsModel.setRowCount(0);
        for (Room room : rooms.all()) {
            if (!(room instanceof Venue)) { // Only show actual rooms, not venues
                roomsModel.addRow(room.toTableRow());
            }
        }
    }

    /**
     * Updates the venue page.
     * Method name as per specification
     */
    public void updateVenuPage(VenueList venues) {
        DefaultTableModel venuesModel = (DefaultTableModel) venuesTable.getModel();
        venuesModel.setRowCount(0);
        for (Venue venue : venues.all()) {
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
     * Updates the tree.
     * Method name as per specification
     */
    public void updateTree(SessionList sessions, VenueList venues) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();

        ExamBlockModel model = controller.getModel();

        // Add existing sessions if any
        if (sessions.all().size() > 0) {
            DefaultMutableTreeNode existingNode = new DefaultMutableTreeNode("Existing sessions (" +
                    sessions.all().size() + ")");

            for (Session session : sessions.all()) {
                int totalStudentsInSession = 0;

                // Count total students across all exams in this session
                for (Exam exam : session.getExams()) {
                    int studentCount = 0;
                    // Count students taking this exam in this venue type
                    for (Student student : model.getStudents().all()) {
                        if (student.isAara() == session.getVenue().isAara()) {
                            for (Subject subject : student.getSubjectsList()) {
                                if (subject.equals(exam.getSubject())) {
                                    studentCount++;
                                    break;
                                }
                            }
                        }
                    }
                    totalStudentsInSession += studentCount;
                }

                int totalDesks = session.getVenue().deskCount();
                int availableDesks = totalDesks - totalStudentsInSession;

                String sessionText = session.getDate() + " at " + session.getTime() +
                        " in " + session.getVenue().venueId() +
                        " (" + availableDesks + " of " + totalDesks + " desks available)";
                DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(sessionText);

                // Add "Exams (N)" node
                DefaultMutableTreeNode examsNode = new DefaultMutableTreeNode("Exams (" +
                        session.getExams().size() + ")");

                // Add each exam
                for (Exam exam : session.getExams()) {
                    // Count students for this specific exam
                    int studentCount = 0;
                    for (Student student : model.getStudents().all()) {
                        if (student.isAara() == session.getVenue().isAara()) {
                            for (Subject subject : student.getSubjectsList()) {
                                if (subject.equals(exam.getSubject())) {
                                    studentCount++;
                                    break;
                                }
                            }
                        }
                    }

                    String examText = exam.getSubject().getTitle() + " (" + studentCount + " students)";
                    examsNode.add(new DefaultMutableTreeNode(examText));
                }

                sessionNode.add(examsNode);
                existingNode.add(sessionNode);
            }
            root.add(existingNode);
        }

        // Add "Create a new session" node
        DefaultMutableTreeNode createNewNode = new DefaultMutableTreeNode("Create a new session");
        for (Venue venue : venues.all()) {
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

        // Add button is enabled when exam and venue are selected
        addButton.setEnabled(selectedExam != null && selectedVenue != null);

        // Clear button is enabled when an exam is selected
        clearButton.setEnabled(selectedExam != null);
    }

    /**
     * Handles the Add button action.
     */
    private void handleAdd() {
        if (controller != null) {
            controller.actionPerformed(new java.awt.event.ActionEvent(this,
                    java.awt.event.ActionEvent.ACTION_PERFORMED, "Schedule Exam"));
        }
    }

    /**
     * Handles the Clear button action.
     */
    private void handleClear() {
        // Clear the exam selection
        examTable.clearSelection();
        updateButtonStates();
    }

    /**
     * Gets the currently selected exam.
     */
    public Exam getSelectedExam() {
        if (controller == null || controller.getModel() == null) {
            return null;
        }

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
        if (sessionVenueTree.getSelectionPath() == null || controller == null) {
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
        if (sessionVenueTree.getSelectionPath() == null || controller == null) {
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

    // Additional methods required by specification

    public JButton getAddButton() {
        return addButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getFinaliseButton() {
        return finaliseButton;
    }

    public JTree getTree() {
        return sessionVenueTree;
    }

    public JTable getExamTable() {
        return examTable;
    }

    public DefaultTableModel getExamTableModel() {
        return examTableModel;
    }

    public JTabbedPane getTabbedPane() {
        return bottomTabbedPane;
    }

    public void setTitle(String title) {
        super.setTitle(title);
    }

    public void setVersion(double version) {
        // Version is displayed in title
        if (controller != null && controller.getModel() != null) {
            controller.getModel().setVersion(version);
            updateView();
        }
    }

    // Additional methods from specification

    /**
     * Add a listener for the Add button
     */
    public void addAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    /**
     * Add a listener for the Clear button
     */
    public void addClearButtonListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }

    /**
     * Add a listener for the Finalise button
     */
    public void addFinaliseButtonListener(ActionListener listener) {
        finaliseButton.addActionListener(listener);
    }

    /**
     * mapping from list to item represented
     */
    public void addExamToExamMap(int index, Exam exam) {
        // This mapping is handled by the table model index
        // No additional mapping needed as we use direct indexing
    }

    /**
     * Add Session object to the SessionNode map
     */
    public void addSessionToSessionNodeMap(DefaultMutableTreeNode sessionNode, Session session) {
        // Store mapping in node's user object
        sessionNode.setUserObject(new NodeData("session", session));
    }

    /**
     * Add Venue object to the VenueNode map
     */
    public void addVenueToVenueNodeMap(DefaultMutableTreeNode venueNode, Venue venue) {
        // Store mapping in node's user object
        venueNode.setUserObject(new NodeData("venue", venue));
    }

    /**
     * get Exam stored for index index
     */
    public Exam getExamFromExamMap(int index) {
        if (controller != null && controller.getModel() != null) {
            ExamList exams = controller.getModel().getExams();
            if (index >= 0 && index < exams.all().size()) {
                return exams.all().get(index);
            }
        }
        return null;
    }

    /**
     * Mapping from list item to the item represented
     */
    public Exam getExamFromExamNodeMap(DefaultMutableTreeNode examNode) {
        if (examNode.getUserObject() instanceof NodeData) {
            NodeData data = (NodeData) examNode.getUserObject();
            if ("exam".equals(data.type) && data.data instanceof Exam) {
                return (Exam) data.data;
            }
        }
        return null;
    }

    /**
     * return the selected exam rows
     */
    public int[] getSelectedExamRows() {
        return examTable.getSelectedRows();
    }

    /**
     * get the node from the session tree
     */
    public DefaultMutableTreeNode getSelectedTreeNode() {
        TreePath path = sessionVenueTree.getSelectionPath();
        if (path != null) {
            return (DefaultMutableTreeNode) path.getLastPathComponent();
        }
        return null;
    }

    /**
     * return Session object from the SessionNode map
     */
    public Session getSessionFromSessionNodeMap(DefaultMutableTreeNode sessionNode) {
        if (sessionNode.getUserObject() instanceof NodeData) {
            NodeData data = (NodeData) sessionNode.getUserObject();
            if ("session".equals(data.type) && data.data instanceof Session) {
                return (Session) data.data;
            }
        }
        return null;
    }

    /**
     * return the Session root node
     */
    public DefaultMutableTreeNode getSessionRoot() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        // Find the "Existing sessions" node
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            if (child.getUserObject().toString().startsWith("Existing sessions")) {
                return child;
            }
        }
        return null;
    }

    /**
     * Get Venue object from the VenueNode map
     */
    public Venue getVenueFromVenueNodeMap(DefaultMutableTreeNode venueNode) {
        if (venueNode.getUserObject() instanceof NodeData) {
            NodeData data = (NodeData) venueNode.getUserObject();
            if ("venue".equals(data.type) && data.data instanceof Venue) {
                return (Venue) data.data;
            }
        }
        return null;
    }

    /**
     * return the Venue root node
     */
    public DefaultMutableTreeNode getVenueRoot() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        // Find the "Create a new session" node
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            if (child.getUserObject().equals("Create a new session")) {
                return child;
            }
        }
        return null;
    }

    /**
     * Checks if there are any un-finalized sessions in the JTree
     */
    public boolean hasUnfinalisedSessions() {
        if (controller == null || controller.getModel() == null) {
            return false;
        }

        // Check if any sessions exist but haven't been finalized
        SessionList sessions = controller.getModel().getSessions();
        if (sessions.all().isEmpty()) {
            return false;
        }

        // Check if desk allocations have been done
        for (Session session : sessions.all()) {
            if (session.countStudents() == 0) {
                return true; // Session exists but no students allocated
            }
        }

        return false;
    }

    /**
     * clear the selection of any control, as well as some cached values
     */
    public void removeAllSelections() {
        examTable.clearSelection();
        sessionVenueTree.clearSelection();
    }

    // Inner class for storing node data
    private static class NodeData {
        final String type;
        final Object data;

        NodeData(String type, Object data) {
            this.type = type;
            this.data = data;
        }

        @Override
        public String toString() {
            if (data != null) {
                return data.toString();
            }
            return "";
        }
    }
}