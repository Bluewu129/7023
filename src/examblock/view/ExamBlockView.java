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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The V in MVC model. Drives and owns the UI
 * As per specification - requires Registry parameter
 */
public class ExamBlockView extends JFrame {

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

    // Simple action listener that delegates to controller
    private ActionListener simpleActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (controller != null) {
                controller.handleAction(e.getActionCommand());
            }
        }
    };

    /**
     * Constructs a new ExamBlockView.
     * As per specification - requires Registry parameter
     *
     * @param registry the registry for dependencies
     */
    public ExamBlockView(Registry registry) {
        this.registry = registry;

        FileChooser.setParent(this);

        initializeComponents();
        layoutComponents();
        setupMenus();
    }

    /**
     * Sets the controller for this view.
     * Must be called after construction.
     *
     * @param controller the controller
     */
    public void setController(ExamBlockController controller) {
        this.controller = controller;
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
                return false;
            }
        };

        // Initialize exam table
        examTable = new JTable(examTableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examTable.setRowHeight(20);

        // Set column widths
        examTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        examTable.getColumnModel().getColumn(0).setMaxWidth(40);
        examTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        examTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        examTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        examTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        examTable.getColumnModel().getColumn(5).setPreferredWidth(40);
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
        finaliseButton.addActionListener(simpleActionListener);

        addButton = new JButton("Add");
        addButton.setEnabled(false);
        addButton.addActionListener(e -> {
            if (controller != null) {
                controller.handleAction("Schedule Exam");
            }
        });

        clearButton = new JButton("Clear");
        clearButton.setEnabled(false);
        clearButton.addActionListener(e -> handleClear());

        // Add window close listener
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (controller != null) {
                    controller.handleAction("Exit");
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
     */
    public JPanel createTopPanel() {
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
        openItem.addActionListener(simpleActionListener);
        fileMenu.add(openItem);

        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setActionCommand("Save");
        saveMenuItem.addActionListener(simpleActionListener);
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setActionCommand("Exit");
        exitItem.addActionListener(simpleActionListener);
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // View menu
        JMenu viewMenu = new JMenu("View");

        verboseMenuItem = new JCheckBoxMenuItem("Verbose Output");
        verboseMenuItem.setActionCommand("Verbose");
        verboseMenuItem.addActionListener(simpleActionListener);
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
     */
    public void display() {
        setVisible(true);
    }

    /**
     * Shows the desk allocations in a viewer.
     */
    private void showDeskAllocations() {
        if (!latestDeskAllocations.isEmpty()) {
            DialogUtils.showTextViewer(latestDeskAllocations, "Desk Allocations",
                    DialogUtils.ViewerOptions.SCROLL_WRAP, CSSE7023.FileType.TXT);
        } else {
            StringBuilder sb = new StringBuilder();
            ExamBlockModel model = getModel();

            if (model != null) {
                sb.append("Desk Allocations\n");
                sb.append("================\n\n");

                for (Session session : model.getSessions().all()) {
                    sb.append("Session: ").append(session.getVenue().venueId());
                    sb.append(" - ").append(session.getDate());
                    sb.append(" at ").append(session.getTime()).append("\n");
                    sb.append("-".repeat(50)).append("\n");

                    writeDeskLayout(sb, session);
                    sb.append("\n");
                }
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

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int deskNum = i * columns + j + 1;
                sb.append(String.format("Desk %2d  ", deskNum));
            }
            sb.append("\n");
        }
    }

    /**
     * Gets the current model from the controller.
     */
    private ExamBlockModel getModel() {
        return controller != null ? controller.getModel() : null;
    }

    /**
     * Updates the view with current model data.
     */
    public void updateView() {
        ExamBlockModel model = getModel();
        if (model == null) {
            return;
        }

        String title = "Exam Block Manager - " + model.getTitle() + " (v" + String.format("%.1f", model.getVersion()) + ")";
        setTitle(title);

        updateExamTable(model.getExams());
        updateSubjectPage(model.getSubjects());
        updateExamPage(model.getExams());
        updateUnitPage(model.getUnits());
        updateStudentPage(model.getStudents());
        updateRoomPage(model.getRooms());
        updateVenuePage(model.getVenues());
        updateTree(model.getSessions(), model.getVenues());

        boolean hasData = model.getExams().size() > 0 ||
                model.getVenues().size() > 0 ||
                model.getSessions().all().size() > 0;

        if (saveMenuItem != null) {
            saveMenuItem.setEnabled(hasData);
        }

        finaliseButton.setEnabled(model.getSessions().all().size() > 0);
        deskAllocationsMenuItem.setEnabled(model.getSessions().all().size() > 0);

        updateButtonStates();
        revalidate();
        repaint();
    }

    // All the update methods remain the same but use getModel()
    public void updateExamTable(ExamList exams) {
        examTableModel.setRowCount(0);
        ExamBlockModel model = getModel();
        if (model == null) return;

        for (Exam exam : exams.all()) {
            int aaraCount = 0;
            int nonAaraCount = 0;
            for (Student student : model.getStudents().all()) {
                boolean hasSubject = false;
                for (Subject subject : student.getSubjects().all()) {
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
                    "✓",
                    exam.getSubject().getTitle(),
                    exam.getDate().toString(),
                    exam.getTime().toString(),
                    String.valueOf(aaraCount),
                    String.valueOf(nonAaraCount)
            };
            examTableModel.addRow(rowData);
        }
    }

    public void updateSubjectPage(SubjectList subjects) {
        DefaultTableModel subjectsModel = (DefaultTableModel) subjectsTable.getModel();
        subjectsModel.setRowCount(0);
        for (Subject subject : subjects.all()) {
            subjectsModel.addRow(subject.toTableRow());
        }
    }

    public void updateExamPage(ExamList exams) {
        DefaultTableModel examsModel = (DefaultTableModel) examsTable.getModel();
        examsModel.setRowCount(0);
        for (Exam exam : exams.all()) {
            Object[] row = exam.toLongTableRow();
            Object[] fullRow = new Object[7];
            for (int i = 0; i < Math.min(row.length, 7); i++) {
                fullRow[i] = row[i];
            }
            if (fullRow[2] == null) fullRow[2] = "";
            if (fullRow[3] == null) fullRow[3] = "";
            if (fullRow[4] == null) fullRow[4] = "";
            examsModel.addRow(fullRow);
        }
    }

    public void updateUnitPage(UnitList units) {
        DefaultTableModel unitsModel = (DefaultTableModel) unitsTable.getModel();
        unitsModel.setRowCount(0);
        for (Unit unit : units.all()) {
            unitsModel.addRow(unit.toTableRow());
        }
    }

    public void updateStudentPage(StudentList students) {
        DefaultTableModel studentsModel = (DefaultTableModel) studentsTable.getModel();
        studentsModel.setRowCount(0);
        for (Student student : students.all()) {
            StringBuilder subjects = new StringBuilder();
            boolean first = true;
            for (Subject subject : student.getSubjects().all()) {
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

    public void updateRoomPage(RoomList rooms) {
        DefaultTableModel roomsModel = (DefaultTableModel) roomsTable.getModel();
        roomsModel.setRowCount(0);
        for (Room room : rooms.all()) {
            if (!(room instanceof Venue)) {
                roomsModel.addRow(room.toTableRow());
            }
        }
    }

    public void updateVenuePage(VenueList venues) {
        DefaultTableModel venuesModel = (DefaultTableModel) venuesTable.getModel();
        venuesModel.setRowCount(0);
        for (Venue venue : venues.all()) {
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

    public void updateTree(SessionList sessions, VenueList venues) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();

        ExamBlockModel model = getModel();
        if (model == null) return;

        if (sessions.all().size() > 0) {
            DefaultMutableTreeNode existingNode = new DefaultMutableTreeNode("Existing sessions (" +
                    sessions.all().size() + ")");

            for (Session session : sessions.all()) {
                int totalStudentsInSession = 0;

                for (Exam exam : session.getExams()) {
                    int studentCount = 0;
                    for (Student student : model.getStudents().all()) {
                        if (student.isAara() == session.getVenue().isAara()) {
                            for (Subject subject : student.getSubjects().all()) {
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

                DefaultMutableTreeNode examsNode = new DefaultMutableTreeNode("Exams (" +
                        session.getExams().size() + ")");

                for (Exam exam : session.getExams()) {
                    int studentCount = 0;
                    for (Student student : model.getStudents().all()) {
                        if (student.isAara() == session.getVenue().isAara()) {
                            for (Subject subject : student.getSubjects().all()) {
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

        treeModel.reload();

        if (root.getChildCount() > 0) {
            TreePath path = new TreePath(new Object[]{root, root.getChildAt(0)});
            sessionVenueTree.expandPath(path);
        }
        if (root.getChildCount() > 1) {
            TreePath path = new TreePath(new Object[]{root, root.getChildAt(1)});
            sessionVenueTree.expandPath(path);
        }
    }

    private void updateButtonStates() {
        Exam selectedExam = getSelectedExam();
        Venue selectedVenue = getSelectedVenue();

        addButton.setEnabled(selectedExam != null && selectedVenue != null);
        clearButton.setEnabled(selectedExam != null);
    }

    private void handleClear() {
        examTable.clearSelection();
        updateButtonStates();
    }

    public Exam getSelectedExam() {
        ExamBlockModel model = getModel();
        if (model == null) return null;

        int selectedRow = examTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < model.getExams().all().size()) {
            return model.getExams().all().get(selectedRow);
        }
        return null;
    }

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
            String nodeText = node.getUserObject().toString();
            int spaceIndex = nodeText.indexOf(" ");
            if (spaceIndex > 0) {
                String venueId = nodeText.substring(0, spaceIndex);

                ExamBlockModel model = getModel();
                if (model != null) {
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

    public void updateVerboseStatus(boolean verbose) {
        verboseMenuItem.setSelected(verbose);
    }

    public void setLatestDeskAllocations(String deskAllocations) {
        this.latestDeskAllocations = deskAllocations;
        deskAllocationsMenuItem.setEnabled(true);
    }

    public void setLatestFinaliseReport(String report) {
        this.latestFinaliseReport = report;
        finaliseReportsMenuItem.setEnabled(true);
    }

    // Getter methods
    public ExamBlockController getController() { return controller; }
    public Registry getRegistry() { return registry; }
    public JButton getAddButton() { return addButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getFinaliseButton() { return finaliseButton; }
    public JTree getTree() { return sessionVenueTree; }
    public JTable getExamTable() { return examTable; }
    public DefaultTableModel getExamTableModel() { return examTableModel; }
    public JTabbedPane getTabbedPane() { return bottomTabbedPane; }
}
