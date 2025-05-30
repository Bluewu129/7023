package examblock.view;

import examblock.model.*;
import examblock.view.components.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Primary view component implementing the View layer of MVC architecture.
 * Manages complete GUI interface including tables, trees, and tabbed data views.
 * Provides observer pattern integration for automatic model synchronization.
 */
public class ExamBlockView implements ModelObserver {

    /**
     * Main application window containing the entire exam block interface
     */
    private JFrame frame;

    /**
     * Tabbed pane organizing different data views for easy navigation between interfaces
     */
    private JTabbedPane tabbedPane;

    /**
     * Table component for displaying and selecting exam data
     */
    private JTable examTable;

    /**
     * Table model managing data structure for the exam table
     */
    private DefaultTableModel examTableModel;

    /**
     * Tree component displaying hierarchical session and venue data for navigation
     */
    private JTree tree;

    /**
     * Root node for organizing existing session data in tree structure
     */
    private DefaultMutableTreeNode sessionRoot;

    /**
     * Root node for organizing venue data for new session creation
     */
    private DefaultMutableTreeNode venueRoot;

    /**
     * Action button for adding exams to sessions or venues
     */
    private JButton addButton;

    /**
     * Action button for clearing current selections and resetting interface state
     */
    private JButton clearButton;

    /**
     * Action button for finalizing exam configurations and generating reports
     */
    private JButton finaliseButton;

    /**
     * Reference to the main data model for observer pattern integration
     */
    private ExamBlockModel model;

    /**
     * Mapping from tree nodes to their corresponding session objects for quick access
     */
    private Map<DefaultMutableTreeNode, Session> sessionNodeMap;

    /**
     * Mapping from tree nodes to their corresponding venue objects for quick access
     */
    private Map<DefaultMutableTreeNode, Venue> venueNodeMap;

    /**
     * Mapping from integer indices to exam objects for efficient exam table access
     */
    private Map<Integer, Exam> examMap;

    /**
     * Mapping from tree nodes to their corresponding exam objects for tree navigation
     */
    private Map<DefaultMutableTreeNode, Exam> examNodeMap;

    /**
     * Initializes the complete view interface with all components and data mappings.
     * Creates main window, configures layout, sets up UI components, 
     * and establishes parent dialogs.
     *
     * @param registry the global registry for object management integration
     */
    public ExamBlockView(Registry registry) {
        // Initialize all node-to-object mapping collections
        sessionNodeMap = new HashMap<>();
        venueNodeMap = new HashMap<>();
        examMap = new HashMap<>();
        examNodeMap = new HashMap<>();

        // Create and configure main application frame
        frame = new JFrame("Exam Block Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 700);
        frame.setLocationRelativeTo(null);

        // Initialize primary action buttons
        addButton = new JButton("Add");
        clearButton = new JButton("Clear");
        finaliseButton = new JButton("Finalise");

        // Set up exam table with appropriate column structure and selection model
        String[] examColumns = {"Subject", "Date", "Time", "AARA", "Non"};
        examTableModel = new DefaultTableModel(examColumns, 0);
        examTable = new JTable(examTableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create hierarchical tree structure for session and venue navigation
        sessionRoot = new DefaultMutableTreeNode("Existing sessions");
        venueRoot = new DefaultMutableTreeNode("Create a new session");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sessions");
        root.add(sessionRoot);
        root.add(venueRoot);

        tree = new JTree(root);
        tree.setRootVisible(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Initialize tabbed interface for detailed data views
        tabbedPane = new JTabbedPane();

        // Configure dialog utility classes with appropriate parent components
        DialogUtils.setParent(frame);
        FileChooser.setParent(frame);

        // Assemble complete layout using BorderLayout organization
        frame.setLayout(new BorderLayout());
        frame.add(createTopPanel(), BorderLayout.NORTH);
        frame.add(createBottomPanel(), BorderLayout.CENTER);
    }

    /**
     * Creates the top control panel with exam selection, session navigation, and action buttons.
     * Implements three-column layout for workflow steps: Select Exam, 
     * Select Session/Venue, Take Action.
     *
     * @return configured top panel with all control components
     */
    public JPanel createTopPanel() {
        final JPanel topPanel = new JPanel(new BorderLayout());

        // Left panel: Exam selection interface with scrollable table
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("1. Select an Exam / Unit"));
        JScrollPane examScrollPane = new JScrollPane(examTable);
        examScrollPane.setPreferredSize(new Dimension(400, 200));
        leftPanel.add(examScrollPane, BorderLayout.CENTER);

        // Center panel: Session/venue navigation with hierarchical tree
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("2. Select a Session / Venue"));
        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setPreferredSize(new Dimension(300, 200));
        centerPanel.add(treeScrollPane, BorderLayout.CENTER);

        // Right panel: Action buttons with proper spacing and layout
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("3. Go"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        rightPanel.add(finaliseButton, gbc);
        gbc.gridy++;
        rightPanel.add(addButton, gbc);
        gbc.gridy++;
        rightPanel.add(clearButton, gbc);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * Creates the bottom data panel with tabbed interface for detailed data views.
     * Implements scroll pane for large data table display across multiple entity types.
     *
     * @return configured bottom panel with tabbed data views
     */
    public JPanel createBottomPanel() {
        // Create tables for each data type with appropriate column structures
        JTable subjectsTable = new JTable(new DefaultTableModel(
            new String[]{"Title", "Description"}, 0));
        JTable examsTable = new JTable(new DefaultTableModel(
            new String[]{"Subject", "Type", "Paper", "Subtitle", "Units", "Date", "Time"}, 0));
        JTable unitsTable = new JTable(new DefaultTableModel(
            new String[]{"Subject", "UnitID", "Title"}, 0));
        JTable studentsTable = new JTable(new DefaultTableModel(
            new String[]{"Full Name", "AARA", "Date of Birth", "Subjects"}, 0));
        JTable roomsTable = new JTable(new DefaultTableModel(
            new String[]{"Room ID"}, 0));
        JTable venuesTable = new JTable(new DefaultTableModel(
            new String[]{"Venue", "Rooms", "Rows", "Col", "Desks", "AARA"}, 0));

        // Add all tabs to the tabbed pane with descriptive labels
        tabbedPane.addTab("Subjects", new JScrollPane(subjectsTable));
        tabbedPane.addTab("Exams", new JScrollPane(examsTable));
        tabbedPane.addTab("Units", new JScrollPane(unitsTable));
        tabbedPane.addTab("Students", new JScrollPane(studentsTable));
        tabbedPane.addTab("Rooms", new JScrollPane(roomsTable));
        tabbedPane.addTab("Venues", new JScrollPane(venuesTable));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(tabbedPane, BorderLayout.CENTER);
        return bottomPanel;
    }

    /**
     * Makes the main application window visible to the user.
     * Called after complete construction and configuration.
     */
    public void display() {
        frame.setVisible(true);
    }

    /**
     * Refreshes the exam table with current exam data including student counts.
     * Clears existing content and repopulates with updated information.
     * Calculates AARA and non-AARA student counts for each exam subject.
     *
     * @param exams the current list of exams to display
     */
    public void updateExamTable(ExamList exams) {
        examTableModel.setRowCount(0);
        examMap.clear();

        if (exams != null) {
            for (int i = 0; i < exams.size(); i++) {
                Exam exam = exams.all().get(i);
                int aaraCount = 0;
                int nonAaraCount = 0;

                if (model != null && model.getStudents() != null) {
                    aaraCount = model.getStudents().countStudents(exam.getSubject(), true);
                    nonAaraCount = model.getStudents().countStudents(exam.getSubject(), false);
                }

                Object[] rowData = {
                    exam.getSubject().getTitle(),
                    exam.getDate().toString(),
                    exam.getTime().toString(),
                    aaraCount,
                    nonAaraCount
                };
                examTableModel.addRow(rowData);
                examMap.put(i, exam);
            }
        }
    }

    /**
     * Updates the session tree with current session and venue data.
     * Rebuilds complete tree structure with detailed session information 
     * including desk availability.
     * Organizes existing sessions and available venues for new session creation.
     *
     * @param sessions current list of scheduled sessions
     * @param venues   current list of available venues
     */
    public void updateTree(SessionList sessions, 
                           VenueList venues) {
        sessionRoot.removeAllChildren();
        venueRoot.removeAllChildren();
        sessionNodeMap.clear();
        venueNodeMap.clear();
        examNodeMap.clear();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        root.removeAllChildren();
        if (sessions != null && sessions.size() > 0) {
            root.add(sessionRoot);
        }
        root.add(venueRoot);
        if (sessions != null && sessions.size() > 0) {
            sessionRoot.setUserObject("Existing sessions (" + sessions.size() + ")");

            for (int i = 0; i < sessions.size(); i++) {
                Session session = sessions.all().get(i);
                int usedDesks = session.countStudents();
                int totalDesks = session.getVenue().deskCount();
                int availableDesks = totalDesks - usedDesks;

                String sessionDisplay = session.getDate().toString() 
                        + " at " 
                        + session.getTime().toString() 
                        + " in " 
                        + session.getVenue().venueId() 
                        + " (" + availableDesks + " of " 
                        + totalDesks + " desks available)";
                DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(sessionDisplay);
                sessionRoot.add(sessionNode);
                addSessionToSessionNodeMap(sessionNode, session);
                java.util.List<Exam> sessionExams = session.getExams();
                if (sessionExams != null && sessionExams.size() > 0) {
                    DefaultMutableTreeNode examsNode = new DefaultMutableTreeNode(
                            "Exams (" + sessionExams.size() + ")");
                    sessionNode.add(examsNode);
                    for (Exam exam : sessionExams) {
                        int examStudents = 0;
                        if (model != null && model.getStudents() != null) {
                            boolean isAara = session.getVenue().isAara();
                            examStudents = model.getStudents().countStudents(
                                    exam.getSubject(), isAara);
                        }
                        String examDisplay = exam.getSubject().getTitle() 
                                + " (" + examStudents + " students)";
                        DefaultMutableTreeNode examNode = new DefaultMutableTreeNode(examDisplay);
                        examsNode.add(examNode);
                        examNodeMap.put(examNode, exam);
                        boolean isSessionFinalised = false;
                        java.util.List<Desk> examDesks = new java.util.ArrayList<>();
                        if (session.desks != null && session.getVenue() != null) {
                            int rows = session.getVenue().getRows();
                            int columns = session.getVenue().getColumns();

                            for (int row = 0; row < rows; row++) {
                                for (int col = 0; col < columns; col++) {
                                    if (session.desks[row][col] != null
                                            &&
                                            session.desks[row][col].deskFamilyName() != null 
                                            &&
                                            !session.desks[row][col]
                                                    .deskFamilyName().trim().isEmpty()) {
                                        String deskExam = session.desks[row][col].deskExam();
                                        if (deskExam != null && deskExam.equals(
                                                exam.getShortTitle())) {
                                            examDesks.add(session.desks[row][col]);
                                            isSessionFinalised = true;
                                        }
                                    }
                                }
                            }
                            examDesks.sort((d1, d2) -> {
                                return Integer.compare(d1.deskNumber(), d2.deskNumber());
                            });
                        }
                        if (isSessionFinalised) {
                            for (Desk desk : examDesks) {
                                String deskDisplay = "Desk " + desk.deskNumber() 
                                        + " " 
                                        + desk.deskLui() 
                                        + " " 
                                        + desk.deskFamilyName() + ", " 
                                        + desk.deskGivenAndInit();
                                DefaultMutableTreeNode deskNode = new 
                                        DefaultMutableTreeNode(deskDisplay);
                                examNode.add(deskNode);
                            }
                        }
                    }
                }
            }
        }
        if (venues != null) {
            for (int i = 0; i < venues.size(); i++) {
                Venue venue = venues.all().get(i);

                String venueDisplay = venue.venueId() + " (" + venue.deskCount() 
                        + (venue.isAara() ? " AARA" : " Non-AARA") + " desks)";
                DefaultMutableTreeNode venueNode = new DefaultMutableTreeNode(venueDisplay);
                venueRoot.add(venueNode);
                addVenueToVenueNodeMap(venueNode, venue);
            }
        }
        ((DefaultTreeModel) tree.getModel()).reload();
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /**
     * Updates the subjects tab with current subject data.
     * Refreshes table content with subject titles and descriptions.
     *
     * @param subjects new SubjectList to display
     */
    public void updateSubjectPage(SubjectList subjects) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(0);
        JTable table = (JTable) scrollPane.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.setRowCount(0);
        if (subjects != null) {
            for (int i = 0; i < subjects.size(); i++) {
                Subject subject = subjects.all().get(i);
                model.addRow(new Object[]{subject.getTitle(), subject.getDescription()});
            }
        }
    }

    /**
     * Updates the exams tab with comprehensive exam information.
     * Uses reflection to access private exam fields for complete data display.
     *
     * @param exams new ExamList to display
     */
    public void updateExamPage(ExamList exams) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(1);
        JTable table = (JTable) scrollPane.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.setRowCount(0);
        if (exams != null) {
            for (int i = 0; i < exams.size(); i++) {
                Exam exam = exams.all().get(i);

                String examType = "INTERNAL";
                if (exam.getClass().getDeclaredFields().length > 0) {
                    try {
                        java.lang.reflect.Field examTypeField = exam.getClass()
                                .getDeclaredField("examType");
                        examTypeField.setAccessible(true);
                        Object examTypeObj = examTypeField.get(exam);
                        if (examTypeObj != null) {
                            examType = examTypeObj.toString();
                        }
                    } catch (Exception e) {
                        examType = "INTERNAL";
                    }
                }

                String paper = "";
                try {
                    java.lang.reflect.Field paperField = exam.getClass().getDeclaredField("paper");
                    paperField.setAccessible(true);
                    Object paperObj = paperField.get(exam);
                    if (paperObj != null && !paperObj.equals('\0')) {
                        paper = paperObj.toString();
                    }
                } catch (Exception e) {
                    paper = "";
                }

                String subtitle = "";
                try {
                    java.lang.reflect.Field subtitleField = exam.getClass()
                            .getDeclaredField("subtitle");
                    subtitleField.setAccessible(true);
                    Object subtitleObj = subtitleField.get(exam);
                    if (subtitleObj != null && !subtitleObj.toString().trim().isEmpty()) {
                        subtitle = subtitleObj.toString();
                    }
                } catch (Exception e) {
                    subtitle = "";
                }

                String unit = "";
                try {
                    java.lang.reflect.Field unitField = exam.getClass().getDeclaredField("unit");
                    unitField.setAccessible(true);
                    Object unitObj = unitField.get(exam);
                    if (unitObj != null && !unitObj.equals('\0')) {
                        unit = unitObj.toString();
                    }
                } catch (Exception e) {
                    unit = "";
                }

                model.addRow(new Object[]{
                    exam.getSubject().getTitle(),
                    examType,
                    paper,
                    subtitle,
                    unit,
                    exam.getDate().toString(),
                    exam.getTime().toString()
                });
            }
        }
    }

    /**
     * Updates the units tab with unit information.
     * Uses reflection to access private unit fields for detailed display.
     *
     * @param units new UnitList to display
     */
    public void updateUnitPage(UnitList units) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(2);
        JTable table = (JTable) scrollPane.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setColumnIdentifiers(new String[]{"Subject", "UnitID", "Title", "Description"});

        model.setRowCount(0);
        if (units != null) {
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.all().get(i);
                String unitTitle = "";
                try {
                    java.lang.reflect.Field titleField = unit.getClass().getDeclaredField("title");
                    titleField.setAccessible(true);
                    Object titleObj = titleField.get(unit);
                    if (titleObj != null) {
                        unitTitle = titleObj.toString();
                    }
                } catch (Exception e) {
                    unitTitle = "";
                }

                String description = "";
                try {
                    java.lang.reflect.Field descField = unit.getClass()
                            .getDeclaredField("description");
                    descField.setAccessible(true);
                    Object descObj = descField.get(unit);
                    if (descObj != null) {
                        description = descObj.toString();
                    }
                } catch (Exception e) {
                    description = "";
                }

                model.addRow(new Object[]{
                    unit.getSubject().getTitle(),
                    unit.id().toString(),
                    unitTitle,
                    description
                });
            }
        }
    }

    /**
     * Updates the students tab with student information and subject enrollments.
     * Displays formatted student names, AARA status, and enrolled subjects.
     *
     * @param students new StudentList to display
     */
    public void updateStudentPage(StudentList students) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(3);
        JTable table = (JTable) scrollPane.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setColumnIdentifiers(new String[]{"LUI", "Full Name", 
            "AARA", "Date of Birth", "Subjects"});

        model.setRowCount(0);
        if (students != null) {
            for (int i = 0; i < students.size(); i++) {
                Student student = students.all().get(i);

                StringBuilder subjectsStr = new StringBuilder();
                SubjectList subjectList = student.getSubjects();
                if (subjectList != null) {
                    for (int j = 0; j < subjectList.size(); j++) {
                        if (j > 0) {
                            subjectsStr.append(", ");
                        }
                        subjectsStr.append(subjectList.all().get(j).getTitle());
                    }
                }

                String displayName = student.familyName().toUpperCase() 
                        + ", " + student.givenNames();
                String aaraDisplay = student.isAara() ? "Yes" : "";

                model.addRow(new Object[]{
                    student.getLui(),
                    displayName,
                    aaraDisplay,
                    student.getDob().toString(),
                    subjectsStr.toString()
                });
            }
        }
    }

    /**
     * Updates the venues tab with venue configuration and room details.
     * Shows venue capacity, room assignments, and AARA suitability.
     *
     * @param venues new VenueList to display
     */
    public void updateVenuPage(VenueList venues) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(5);
        JTable table = (JTable) scrollPane.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.setRowCount(0);
        if (venues != null) {
            for (int i = 0; i < venues.size(); i++) {
                Venue venue = venues.all().get(i);
                StringBuilder roomsStr = new StringBuilder();
                RoomList rooms = venue.getRooms();
                if (rooms != null) {
                    for (int j = 0; j < rooms.size(); j++) {
                        if (j > 0) {
                            roomsStr.append(" ");
                        }
                        roomsStr.append(rooms.all().get(j).roomId());
                    }
                }

                model.addRow(new Object[]{
                        venue.venueId(),
                        roomsStr.toString(),
                        venue.getRows(),
                        venue.getColumns(),
                        venue.deskCount(),
                        venue.isAara() ? "Yes" : "No"
                });
            }
        }
    }

    /**
     * Updates the rooms tab with room identifier information.
     * Displays simple room ID listing for reference.
     *
     * @param rooms new RoomList to display
     */
    public void updateRoomPage(RoomList rooms) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(4);
        JTable table = (JTable) scrollPane.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.setRowCount(0);
        if (rooms != null) {
            for (int i = 0; i < rooms.size(); i++) {
                Room room = rooms.all().get(i);
                model.addRow(new Object[]{room.getId()});
            }
        }
    }

    /**
     * Registers an ActionListener for the finalise button.
     *
     * @param listener the ActionListener to handle finalise button clicks
     */
    public void addFinaliseButtonListener(ActionListener listener) {
        finaliseButton.addActionListener(listener);
    }

    /**
     * Registers an ActionListener for the add button.
     *
     * @param listener the ActionListener to handle add button clicks
     */
    public void addAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    /**
     * Registers an ActionListener for the clear button.
     *
     * @param listener the ActionListener to handle clear button clicks
     */
    public void addClearButtonListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }

    /**
     * Updates the main window title.
     *
     * @param title the new window title
     */
    public void setTitle(String title) {
        frame.setTitle(title);
    }

    /**
     * Updates the window title to include version information.
     *
     * @param version the current version number to display
     */
    public void setVersion(double version) {
        String currentTitle = frame.getTitle();
        if (currentTitle.contains("(v")) {
            currentTitle = currentTitle.substring(0, currentTitle.indexOf("(v"));
        }
        frame.setTitle(currentTitle + " (v" + version + ")");
    }

    /**
     * Gets the currently selected exam table rows.
     * Since this uses single selection mode, returns array with one element or null.
     *
     * @return array containing selected row index, or null if no selection
     */
    public int[] getSelectedExamRows() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow != -1) {
            return new int[]{selectedRow};
        }
        return null;
    }

    /**
     * Gets the currently selected tree node.
     *
     * @return the selected DefaultMutableTreeNode, or null if no selection
     */
    public DefaultMutableTreeNode getSelectedTreeNode() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            return (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        }
        return null;
    }

    /**
     * Clears all current selections in exam table and tree.
     * Resets interface to unselected state.
     */
    public void removeAllSelections() {
        // Clear exam table selection
        if (examTable != null) {
            examTable.clearSelection();
        }

        // Clear tree selection  
        if (tree != null) {
            tree.clearSelection();
        }
    }

    /**
     * Checks for unfinalized sessions by examining tree structure.
     * A session is unfinalized if any exam node has no desk assignment children.
     *
     * @return true if unfinalized sessions exist, false otherwise
     */
    public boolean hasUnfinalisedSessions() {
        for (int i = 0; i < sessionRoot.getChildCount(); i++) {
            DefaultMutableTreeNode sessionNode = (DefaultMutableTreeNode) 
                    sessionRoot.getChildAt(i);
            for (int j = 0; j < sessionNode.getChildCount(); j++) {
                DefaultMutableTreeNode examNode = (DefaultMutableTreeNode) 
                        sessionNode.getChildAt(j);
                if (examNode.getChildCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Receives model change notifications and updates appropriate view components.
     * Implements observer pattern for automatic view synchronization with model updates.
     *
     * @param property the name of the changed model property
     */
    public void modelChanged(String property) {
        if (model == null) {
            return;
        }

        switch (property) {
            case "exams":
                updateExamTable(model.getExams());
                break;
            case "subjects":
                updateSubjectPage(model.getSubjects());
                break;
            case "units":
                updateUnitPage(model.getUnits());
                break;
            case "students":
                updateStudentPage(model.getStudents());
                break;
            case "venues":
                updateVenuPage(model.getVenues());
                break;
            case "rooms":
                updateRoomPage(model.getRooms());
                break;
            case "sessions":
                updateTree(model.getSessions(), model.getVenues());
                break;
            case "finalised":
                // Handled by controller - no direct view update needed
                break;
        }
    }

    /**
     * Sets the model reference and registers this view as an observer.
     * Establishes bidirectional communication between model and view.
     *
     * @param model the ExamBlockModel to observe
     */
    public void setModel(ExamBlockModel model) {
        this.model = model;
        if (model != null) {
            model.addObserver(this);
        }
    }

    // Accessor methods for controller integration

    /**
     * Provides access to the main application frame.
     *
     * @return the main JFrame window
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Provides access to the exam selection table.
     *
     * @return the exam JTable component
     */
    public JTable getExamTable() {
        return examTable;
    }

    /**
     * Provides access to the session/venue navigation tree.
     *
     * @return the session JTree component
     */
    public JTree getTree() {
        return tree;
    }

    /**
     * Provides access to the tabbed data interface.
     *
     * @return the main JTabbedPane component
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * Provides access to the finalise action button.
     *
     * @return the finalise JButton
     */
    public JButton getFinaliseButton() {
        return finaliseButton;
    }

    /**
     * Provides access to the add action button.
     *
     * @return the add JButton
     */
    public JButton getAddButton() {
        return addButton;
    }

    /**
     * Provides access to the clear action button.
     *
     * @return the clear JButton
     */
    public JButton getClearButton() {
        return clearButton;
    }

    /**
     * Provides access to the session root tree node.
     *
     * @return the session root DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode getSessionRoot() {
        return sessionRoot;
    }

    /**
     * Provides access to the venue root tree node.
     *
     * @return the venue root DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode getVenueRoot() {
        return venueRoot;
    }

    /**
     * Provides access to the exam table model for direct manipulation.
     *
     * @return the exam table's DefaultTableModel
     */
    public DefaultTableModel getExamTableModel() {
        return examTableModel;
    }

    // Mapping management methods for node-to-object associations

    /**
     * Associates a tree node with its corresponding session object.
     *
     * @param sessionNode the tree node representing the session
     * @param session     the Session object to associate
     */
    public void addSessionToSessionNodeMap(DefaultMutableTreeNode sessionNode, Session session) {
        sessionNodeMap.put(sessionNode, session);
    }

    /**
     * Retrieves the session object associated with a tree node.
     *
     * @param sessionNode the tree node to look up
     * @return the associated Session object, or null if not found
     */
    public Session getSessionFromSessionNodeMap(DefaultMutableTreeNode sessionNode) {
        return sessionNodeMap.get(sessionNode);
    }

    /**
     * Associates a tree node with its corresponding venue object.
     *
     * @param venueNode the tree node representing the venue
     * @param venue     the Venue object to associate
     */
    public void addVenueToVenueNodeMap(DefaultMutableTreeNode venueNode, Venue venue) {
        venueNodeMap.put(venueNode, venue);
    }

    /**
     * Retrieves the venue object associated with a tree node.
     *
     * @param venueNode the tree node to look up
     * @return the associated Venue object, or null if not found
     */
    public Venue getVenueFromVenueNodeMap(DefaultMutableTreeNode venueNode) {
        return venueNodeMap.get(venueNode);
    }

    /**
     * Retrieves the exam object associated with a tree node.
     *
     * @param examNode the tree node to look up
     * @return the associated Exam object, or null if not found
     */
    public Exam getExamFromExamNodeMap(DefaultMutableTreeNode examNode) {
        return examNodeMap.get(examNode);
    }

    /**
     * Associates a table index with its corresponding exam object.
     *
     * @param index the table row index
     * @param exam  the Exam object to associate
     */
    public void addExamToExamMap(int index, Exam exam) {
        examMap.put(index, exam);
    }

    /**
     * Retrieves the exam object associated with a table index.
     *
     * @param index the table row index to look up
     * @return the associated Exam object, or null if not found
     */
    public Exam getExamFromExamMap(int index) {
        return examMap.get(index);
    }

    /**
     * Document listener implementation for form field change detection.
     * Provides simplified interface for monitoring text field modifications.
     */
    public abstract static class SimpleDocumentListener 
            implements javax.swing.event.DocumentListener {

        /**
         * Called when text is inserted into the document.
         *
         * @param e the DocumentEvent describing the change
         */
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        /**
         * Called when text is removed from the document.
         *
         * @param e the DocumentEvent describing the change
         */
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        /**
         * Called when document attributes are changed.
         *
         * @param e the DocumentEvent describing the change
         */
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        /**
         * Abstract method for handling any type of document change.
         * Subclasses implement this to respond to text modifications.
         *
         * @param e the DocumentEvent describing the change
         */
        public abstract void update(javax.swing.event.DocumentEvent e);
    }
}