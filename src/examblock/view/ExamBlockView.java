package examblock.view;

import examblock.controller.ExamBlockController;
import examblock.model.*;
import examblock.view.components.ListboxAlternatingRowRenderer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main view class for the ExamBlock application.
 * Provides the GUI interface for exam scheduling.
 */
public class ExamBlockView extends JFrame {

    private ExamBlockController controller;
    private JList<Exam> examList;
    private JList<Venue> venueList;
    private JList<Session> sessionList;
    private DefaultListModel<Exam> examListModel;
    private DefaultListModel<Venue> venueListModel;
    private DefaultListModel<Session> sessionListModel;
    private JTextArea statusArea;
    private JLabel titleLabel;
    private JLabel versionLabel;
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
        updateView();
    }

    /**
     * Initializes all GUI components.
     */
    private void initializeComponents() {
        setTitle("ExamBlock - Exam Scheduling System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Initialize list models
        examListModel = new DefaultListModel<>();
        venueListModel = new DefaultListModel<>();
        sessionListModel = new DefaultListModel<>();

        // Initialize lists
        examList = new JList<>(examListModel);
        examList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examList.setCellRenderer(new ListboxAlternatingRowRenderer());

        venueList = new JList<>(venueListModel);
        venueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        venueList.setCellRenderer(new ListboxAlternatingRowRenderer());

        sessionList = new JList<>(sessionListModel);
        sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sessionList.setCellRenderer(new ListboxAlternatingRowRenderer());

        // Initialize other components
        statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        titleLabel = new JLabel("Exam Block");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        versionLabel = new JLabel("Version: 1.0");
        versionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        // Add window close listener
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                controller.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Exit"));
            }
        });
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

        // Main panel with lists
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Exam list panel
        JPanel examPanel = new JPanel(new BorderLayout());
        examPanel.setBorder(new TitledBorder("Exams"));
        examPanel.add(new JScrollPane(examList), BorderLayout.CENTER);

        JButton scheduleButton = new JButton("Schedule Exam");
        scheduleButton.setActionCommand("Schedule Exam");
        scheduleButton.addActionListener(controller);
        examPanel.add(scheduleButton, BorderLayout.SOUTH);

        mainPanel.add(examPanel);

        // Venue list panel
        JPanel venuePanel = new JPanel(new BorderLayout());
        venuePanel.setBorder(new TitledBorder("Venues"));
        venuePanel.add(new JScrollPane(venueList), BorderLayout.CENTER);
        mainPanel.add(venuePanel);

        // Session list panel
        JPanel sessionPanel = new JPanel(new BorderLayout());
        sessionPanel.setBorder(new TitledBorder("Sessions"));
        sessionPanel.add(new JScrollPane(sessionList), BorderLayout.CENTER);

        JButton finaliseButton = new JButton("Finalise Exam Block");
        finaliseButton.setActionCommand("Finalise");
        finaliseButton.addActionListener(controller);
        sessionPanel.add(finaliseButton, BorderLayout.SOUTH);

        mainPanel.add(sessionPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new TitledBorder("Status"));
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up the menu bar.
     */
    private void setupMenus() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem newItem = new JMenuItem("New");
        newItem.setMnemonic('N');
        newItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newItem.setActionCommand("New");
        newItem.addActionListener(controller);
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("Open");
        openItem.setMnemonic('O');
        openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        openItem.setActionCommand("Open");
        openItem.addActionListener(controller);
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setMnemonic('S');
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveItem.setActionCommand("Save");
        saveItem.addActionListener(controller);
        fileMenu.add(saveItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        exitItem.setActionCommand("Exit");
        exitItem.addActionListener(controller);
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // Options menu
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic('O');

        verboseMenuItem = new JCheckBoxMenuItem("Verbose Output");
        verboseMenuItem.setMnemonic('V');
        verboseMenuItem.setActionCommand("Verbose");
        verboseMenuItem.addActionListener(controller);
        optionsMenu.add(verboseMenuItem);

        menuBar.add(optionsMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic('A');
        aboutItem.setActionCommand("About");
        aboutItem.addActionListener(controller);
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

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

        // Update venue list
        venueListModel.clear();
        for (Venue venue : model.getVenues().all()) {
            venueListModel.addElement(venue);
        }

        // Update session list
        sessionListModel.clear();
        for (Session session : model.getSessions().all()) {
            sessionListModel.addElement(session);
        }

        // Update status area
        updateStatusArea();

        repaint();
    }

    /**
     * Updates the status area with current information.
     */
    private void updateStatusArea() {
        if (controller == null || controller.getModel() == null) {
            return;
        }

        ExamBlockModel model = controller.getModel();
        StringBuilder status = new StringBuilder();

        status.append("System Status:\n");
        status.append("Subjects: ").append(model.getSubjects().all().size()).append("\n");
        status.append("Exams: ").append(model.getExams().all().size()).append("\n");
        status.append("Venues: ").append(model.getVenues().all().size()).append("\n");
        status.append("Sessions: ").append(model.getSessions().all().size()).append("\n");
        status.append("Students: ").append(model.getStudents().all().size()).append("\n");

        statusArea.setText(status.toString());
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
     * Gets the currently selected venue.
     *
     * @return the selected venue, or null if none selected
     */
    public Venue getSelectedVenue() {
        return venueList.getSelectedValue();
    }

    /**
     * Gets the currently selected session.
     *
     * @return the selected session, or null if none selected
     */
    public Session getSelectedSession() {
        return sessionList.getSelectedValue();
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