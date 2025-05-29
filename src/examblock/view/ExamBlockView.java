package examblock.view;

import examblock.controller.ExamBlockController;
import examblock.model.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

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

    /**
     * Constructs a new ExamBlockView.
     *
     * @param controller the controller for this view
     */
    public ExamBlockView(ExamBlockController controller) {
        this.controller = controller;
        initializeComponents();
        layoutComponents();
        setupMenus();

        // Initial update
        updateView();

        // Make sure the frame is visible
        setVisible(true);
    }

    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setTitle("Exam Block Manager - Exam Block (v1.2)");
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

        // Center align checkbox columns
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
        String[] examColumns = {"Subject", "Type", "Paper", "Subtitle", "Date", "Time"};
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
        String[] unitColumns = {"Subject", "Unit ID", "Title"};
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
        String[] studentColumns = {"LUI", "Full Name", "House", "AARA"};
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
        String[] venueColumns = {"Venue", "Rooms", "Rows x Columns", "Desks", "AARA"};
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
        fileMenu.setMnemonic('F');

        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.setMnemonic('L');
        loadItem.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
        loadItem.setActionCommand("Open");
        loadItem.addActionListener(controller);
        fileMenu.add(loadItem);

        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveMenuItem.setActionCommand("Save");
        saveMenuItem.addActionListener(controller);
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        exitItem.setActionCommand("Exit");
        exitItem.addActionListener(controller);
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        verboseMenuItem = new JCheckBoxMenuItem("Verbose Output");
        verboseMenuItem.setMnemonic('V');
        verboseMenuItem.setActionCommand("Verbose");
        verboseMenuItem.addActionListener(controller);
        viewMenu.add(verboseMenuItem);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
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
        String title = "Exam Block Manager - " + model.getTitle() + " (v" + model.getVersion() + ")";
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
        finaliseButton.setEnabled(hasData && model.getSessions().all().size() > 0);

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
            examsModel.addRow(exam.toLongTableRow());
        }

        // Update Units tab
        DefaultTableModel unitsModel = (DefaultTableModel) unitsTable.getModel();
        unitsModel.setRowCount(0);
        for (Unit unit : model.getUnits().all()) {
            unitsModel.addRow(unit.toTableRow());
        }

        // Update Students tab
        DefaultTableModel studentsModel = (DefaultTableModel) studentsTable.getModel();
        studentsModel.setRowCount(0);
        for (Student student : model.getStudents().all()) {
            studentsModel.addRow(student.toTableRow());
        }

        // Update Rooms tab
        DefaultTableModel roomsModel = (DefaultTableModel) roomsTable.getModel();
        roomsModel.setRowCount(0);
        for (Room room : model.getRooms().all()) {
            if (!(room instanceof Venue)) { // Only show actual rooms, not venues
                roomsModel.addRow(room.toTableRow());
            }
        }

        // Update Venues tab
        DefaultTableModel venuesModel = (DefaultTableModel) venuesTable.getModel();
        venuesModel.setRowCount(0);
        for (Venue venue : model.getVenues().all()) {
            venuesModel.addRow(venue.toTableRow());
        }
    }

    /**
     * Updates the session/venue tree structure.
     */
    private void updateSessionVenueTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();

        ExamBlockModel model = controller.getModel();

        // Add existing sessions
        if (model.getSessions().all().size() > 0) {
            DefaultMutableTreeNode existingNode = new DefaultMutableTreeNode("Existing sessions (" +
                    model.getSessions().all().size() + ")");

            for (Session session : model.getSessions().all()) {
                String sessionText = session.getDate() + " at " + session.getTime() +
                        " in " + session.getVenue().venueId() +
                        " (" + calculateAvailableDesks(session) + " of " +
                        session.getVenue().deskCount() + " desks available)";
                DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(sessionText);

                // Add exams under each session
                DefaultMutableTreeNode examsNode = new DefaultMutableTreeNode("Exams (" +
                        session.getExams().size() + ")");
                for (Exam exam : session.getExams()) {
                    examsNode.add(new DefaultMutableTreeNode(exam.getSubject().getTitle() +
                            " (" + session.countStudents() + " students)"));
                }
                sessionNode.add(examsNode);

                existingNode.add(sessionNode);
            }
            root.add(existingNode);
        }

        // Add "Create a new session" node with venues
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

        // Expand all nodes
        treeModel.reload();
        for (int i = 0; i < sessionVenueTree.getRowCount(); i++) {
            sessionVenueTree.expandRow(i);
        }
    }

    /**
     * Calculates available desks for a session.
     */
    private int calculateAvailableDesks(Session session) {
        return session.getVenue().deskCount() - session.countStudents();
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

        // Clear button is enabled when a session is selected
        clearButton.setEnabled(selectedSession != null);
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
        Session selectedSession = getSelectedSession();
        if (selectedSession != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear this session?",
                    "Clear Session",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                controller.getModel().getSessions().remove(selectedSession);
                updateView();
            }
        }
    }

    /**
     * Gets the currently selected exam.
     *
     * @return the selected exam, or null if none selected
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
     *
     * @return the selected venue, or null if none selected
     */
    public Venue getSelectedVenue() {
        if (sessionVenueTree.getSelectionPath() != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    sessionVenueTree.getSelectionPath().getLastPathComponent();

            if (node != null && node.getParent() != null) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                if (parent.getUserObject().equals("Create a new session")) {
                    // Extract venue ID from the node text
                    String nodeText = node.getUserObject().toString();
                    String venueId = nodeText.substring(0, nodeText.indexOf(" "));

                    // Find the venue
                    ExamBlockModel model = controller.getModel();
                    for (Venue venue : model.getVenues().all()) {
                        if (venue.venueId().equals(venueId)) {
                            return venue;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the currently selected session from the tree.
     *
     * @return the selected session, or null if none selected
     */
    public Session getSelectedSession() {
        if (sessionVenueTree.getSelectionPath() != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    sessionVenueTree.getSelectionPath().getLastPathComponent();

            if (node != null && node.getParent() != null) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                if (parent.getUserObject().toString().startsWith("Existing sessions")) {
                    // This is a session node - need to parse and find the actual session
                    String nodeText = node.getUserObject().toString();

                    // Extract date and venue from the text
                    // Format: "2025-03-10 at 12:30 in V1+V2+V3 (X of Y desks available)"
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
            }
        }
        return null;
    }

    /**
     * Updates the verbose status in the menu.
     *
     * @param verbose true if verbose mode is enabled
     */
    public void updateVerboseStatus(boolean verbose) {
        verboseMenuItem.setSelected(verbose);
    }
}