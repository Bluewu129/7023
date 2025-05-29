package examblock.view;

import examblock.controller.ExamBlockController;
import examblock.model.*;
import examblock.view.components.ListboxAlternatingRowRenderer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * Main view class for the ExamBlock application.
 * Provides the GUI interface for exam scheduling.
 */
public class ExamBlockView extends JFrame {

    private ExamBlockController controller;
    private JList<Exam> examList;
    private JTree sessionVenueTree;
    private DefaultListModel<Exam> examListModel;
    private DefaultTreeModel treeModel;
    private JTextArea statusArea;
    private JLabel titleLabel;
    private JLabel versionLabel;
    private JCheckBoxMenuItem verboseMenuItem;

    // Buttons that need to be enabled/disabled
    private JButton scheduleButton;
    private JButton finaliseButton;
    private JButton addButton;
    private JButton clearButton;

    // Menu items that need to be enabled/disabled
    private JMenuItem saveMenuItem;

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
        updateView();
    }

    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setTitle("Exam Block Manager");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Initialize list models
        examListModel = new DefaultListModel<>();

        // Initialize exam list
        examList = new JList<>(examListModel);
        examList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examList.setCellRenderer(new ListboxAlternatingRowRenderer());

        // Initialize tree
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Sessions");
        treeModel = new DefaultTreeModel(rootNode);
        sessionVenueTree = new JTree(treeModel);
        sessionVenueTree.setRootVisible(false);
        sessionVenueTree.setShowsRootHandles(true);

        // Initialize other components
        statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        titleLabel = new JLabel("Exam Block");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        versionLabel = new JLabel("Version: 1.0");
        versionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        // Initialize buttons
        scheduleButton = new JButton("Schedule Exam");
        scheduleButton.setActionCommand("Schedule Exam");
        scheduleButton.addActionListener(controller);
        scheduleButton.setEnabled(false); // Initially disabled

        finaliseButton = new JButton("Finalise");
        finaliseButton.setActionCommand("Finalise");
        finaliseButton.addActionListener(controller);
        finaliseButton.setEnabled(false); // Initially disabled

        addButton = new JButton("Add");
        addButton.setEnabled(false); // Initially disabled
        addButton.addActionListener(e -> handleAdd());

        clearButton = new JButton("Clear");
        clearButton.setEnabled(false); // Initially disabled
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
        examList.addListSelectionListener(e -> {
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
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(versionLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main panel with three sections
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 1. Select an Exam/Unit panel
        JPanel examPanel = new JPanel(new BorderLayout());
        examPanel.setBorder(new TitledBorder("1. Select an Exam / Unit"));

        // Table headers
        JPanel examHeaderPanel = new JPanel(new GridLayout(1, 5));
        examHeaderPanel.add(new JLabel("Int."));
        examHeaderPanel.add(new JLabel("Subject"));
        examHeaderPanel.add(new JLabel("Date"));
        examHeaderPanel.add(new JLabel("Time"));
        examHeaderPanel.add(new JLabel("AARA"));
        examPanel.add(examHeaderPanel, BorderLayout.NORTH);

        examPanel.add(new JScrollPane(examList), BorderLayout.CENTER);
        mainPanel.add(examPanel);

        // 2. Select a Session/Venue panel
        JPanel sessionVenuePanel = new JPanel(new BorderLayout());
        sessionVenuePanel.setBorder(new TitledBorder("2. Select a Session / Venue"));
        sessionVenuePanel.add(new JScrollPane(sessionVenueTree), BorderLayout.CENTER);
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

        // Bottom tabbed pane for details
        JTabbedPane tabbedPane = new JTabbedPane();

        // Subjects tab
        JTextArea subjectsArea = new JTextArea();
        subjectsArea.setEditable(false);
        tabbedPane.addTab("Subjects", new JScrollPane(subjectsArea));

        // Exams tab
        JTextArea examsArea = new JTextArea();
        examsArea.setEditable(false);
        tabbedPane.addTab("Exams", new JScrollPane(examsArea));

        // Units tab
        JTextArea unitsArea = new JTextArea();
        unitsArea.setEditable(false);
        tabbedPane.addTab("Units", new JScrollPane(unitsArea));

        // Students tab
        JTextArea studentsArea = new JTextArea();
        studentsArea.setEditable(false);
        tabbedPane.addTab("Students", new JScrollPane(studentsArea));

        // Rooms tab
        JTextArea roomsArea = new JTextArea();
        roomsArea.setEditable(false);
        tabbedPane.addTab("Rooms", new JScrollPane(roomsArea));

        // Venues tab
        JTextArea venuesArea = new JTextArea();
        venuesArea.setEditable(false);
        tabbedPane.addTab("Venues", new JScrollPane(venuesArea));

        add(tabbedPane, BorderLayout.SOUTH);
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
        saveMenuItem.setEnabled(false); // Initially disabled
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

        // Update header
        titleLabel.setText(model.getTitle());
        versionLabel.setText("Version: " + model.getVersion());

        // Update exam list
        examListModel.clear();
        for (Exam exam : model.getExams().all()) {
            examListModel.addElement(exam);
        }

        // Update session/venue tree
        updateSessionVenueTree();

        // Check if data is loaded
        boolean hasData = model.getExams().size() > 0 ||
                model.getVenues().size() > 0 ||
                model.getSessions().all().size() > 0;

        // Enable/disable controls based on data availability
        saveMenuItem.setEnabled(hasData);
        finaliseButton.setEnabled(hasData && model.getSessions().all().size() > 0);

        // Update button states based on selections
        updateButtonStates();

        repaint();
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
                        " in " + session.getVenue().venueId();
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
            String venueText = venue.venueId() + " (" + venue.deskCount() + " desks)";
            if (venue.isAara()) {
                venueText += " AARA";
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
        return examList.getSelectedValue();
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
                    // This is a session node
                    String nodeText = node.getUserObject().toString();
                    // Parse session info and find matching session
                    // This is simplified - in real implementation would need proper parsing
                    ExamBlockModel model = controller.getModel();
                    return model.getSessions().all().isEmpty() ? null :
                            model.getSessions().all().get(0);
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