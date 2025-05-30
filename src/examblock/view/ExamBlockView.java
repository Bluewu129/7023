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
 * The V in MVC model. Drives and owns the UI
 * [7] Basic GUI setup and component organization.
 * [10] Tabbed pane for organizing multiple views.
 * [11] Tree component for hierarchical data display.
 * [12] Menu system implementation.
 */
public class ExamBlockView implements ModelObserver {

    /**
     * The main application window frame that contains the entire exam block view.
     */
    private JFrame frame;

    /**
     * The main tabbed pane component that organizes different data views into tabs.
     * Provides easy navigation between different data management interfaces.
     * [10] Tabbed interface organization.
     */
    private JTabbedPane tabbedPane;

    /**
     * The table component specifically for displaying and managing exam data.
     */
    private JTable examTable;

    /**
     * The table model that manages data for the exam table.
     */
    private DefaultTableModel examTableModel;

    /**
     * The tree component for displaying hierarchical session and venue data.
     * [11] Hierarchical session display.
     */
    private JTree tree;

    /**
     * The root node for session data in the tree structure.
     */
    private DefaultMutableTreeNode sessionRoot;

    /**
     * The root node for venue data in the tree structure.
     */
    private DefaultMutableTreeNode venueRoot;

    /**
     * The "Add" button for adding new records to the currently selected tab.
     */
    private JButton addButton;

    /**
     * The "Clear" button for clearing selections and resetting form fields.
     */
    private JButton clearButton;

    /**
     * The "Finalise" button for completing and finalizing exam configurations.
     */
    private JButton finaliseButton;

    /**
     * The main data model containing all exam block information.
     */
    private ExamBlockModel model;

    /**
     * Mapping from tree nodes to their corresponding session objects.
     */
    private Map<DefaultMutableTreeNode, Session> sessionNodeMap;

    /**
     * Mapping from tree nodes to their corresponding venue objects.
     */
    private Map<DefaultMutableTreeNode, Venue> venueNodeMap;

    /**
     * Mapping from integer indices to exam objects for quick access.
     */
    private Map<Integer, Exam> examMap;

    /**
     * Mapping from tree nodes to their corresponding exam objects.
     */
    private Map<DefaultMutableTreeNode, Exam> examNodeMap;

    /**
     * Document listener implementation
     */
    public abstract static class SimpleDocumentListener extends Object
            implements javax.swing.event.DocumentListener {
        /**
         * Constructor
         */
        public SimpleDocumentListener() {
            super();
        }

        /**
         * Specified by: insertUpdate in interface DocumentListener
         * @param e - DocumentEvent
         */
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        /**
         * Specified by: removeUpdate in interface DocumentListener
         * @param e - DocumentEvent
         */
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        /**
         * Specified by: changedUpdate in interface DocumentListener
         * @param e - DocumentEvent
         */
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        /**
         * Called when data changes
         * @param e - the particular event that happened
         */
        public abstract void update(javax.swing.event.DocumentEvent e);
    }

    /**
     * Constructor
     * [7] Swing GUI creation and layout setup.
     * [15] Root pane and frame structure.
     * @param registry - registry
     */
    public ExamBlockView(Registry registry) {
        // maps
        sessionNodeMap = new HashMap<>();
        venueNodeMap = new HashMap<>();
        examMap = new HashMap<>();
        examNodeMap = new HashMap<>();

        // frame
        frame = new JFrame("Exam Block Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        // buttons
        addButton = new JButton("Add");
        clearButton = new JButton("Clear");
        finaliseButton = new JButton("Finalise");

        // exam table
        String[] examColumns = {"Subject", "Date", "Time", "AARA", "Non"};
        examTableModel = new DefaultTableModel(examColumns, 0);
        examTable = new JTable(examTableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // tree nodes
        sessionRoot = new DefaultMutableTreeNode("Existing sessions");
        venueRoot = new DefaultMutableTreeNode("Create a new session");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sessions");
        root.add(sessionRoot);
        root.add(venueRoot);

        // tree
        tree = new JTree(root);
        tree.setRootVisible(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // tabbed pane
        tabbedPane = new JTabbedPane();

        // dialog parents
        DialogUtils.setParent(frame);
        FileChooser.setParent(frame);

        // UI
        frame.setLayout(new BorderLayout());
        frame.add(createTopPanel(), BorderLayout.NORTH);
        frame.add(createBottomPanel(), BorderLayout.CENTER);
    }

    /**
     * create the top panel
     * @return the panel object
     */
    public JPanel createTopPanel() {
        final JPanel topPanel = new JPanel(new BorderLayout());

        // Left - exam selection
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("1. Select an Exam / Unit"));
        JScrollPane examScrollPane = new JScrollPane(examTable);
        examScrollPane.setPreferredSize(new Dimension(400, 200));
        leftPanel.add(examScrollPane, BorderLayout.CENTER);

        // Center - session/venue tree
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("2. Select a Session / Venue"));
        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setPreferredSize(new Dimension(300, 200));
        centerPanel.add(treeScrollPane, BorderLayout.CENTER);

        // Right - buttons
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("3. Go"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
     * create the bottom panel
     * [13] Scroll pane for large data table display.
     * @return the panel
     */
    public JPanel createBottomPanel() {
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
     * Show the window after construction
     */
    public void display() {
        frame.setVisible(true);
    }

    /**
     * remove old content and load new content
     * @param exams - new list of exams
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
                        exam.getTime().toString(), aaraCount, nonAaraCount
                };
                examTableModel.addRow(rowData);
                examMap.put(i, exam);
            }
        }
    }

    /**
     * update the session tree
     * @param sessions - sessions
     * @param venues - venues
     */
    public void updateTree(SessionList sessions, VenueList venues) {

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

                String sessionDisplay = session.getDate().toString() + " at "
                        + session.getTime().toString() + " in "
                        + session.getVenue().venueId()
                        + " (" + availableDesks + " of " + totalDesks + " desks available)";

                DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(sessionDisplay);
                sessionRoot.add(sessionNode);
                addSessionToSessionNodeMap(
                        sessionNode, session);

                java.util.List<Exam> sessionExams = session.getExams();
                if (sessionExams != null
                        && sessionExams.size() > 0) {

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
                                            && session.desks[row][col].deskFamilyName() != null
                                            && !session.desks[row][col].deskFamilyName()
                                            .trim().isEmpty()) {

                                        String deskExam = session.desks[row][col].deskExam();
                                        if (deskExam != null && deskExam.equals(
                                                exam.getShortTitle())) {
                                            examDesks.add(session.desks[row][col]);
                                            isSessionFinalised = true;
                                        }
                                    }
                                }
                            }
                            examDesks.sort((d1, d2) ->
                                            Integer.compare(d1.deskNumber(), d2.deskNumber()));
                        }
                        if (isSessionFinalised) {
                            for (Desk desk : examDesks) {
                                String deskDisplay = "Desk " + desk.deskNumber() + " "
                                        + desk.deskLui() + " "
                                        + desk.deskFamilyName() + ", "
                                        + desk.deskGivenAndInit();
                                DefaultMutableTreeNode deskNode
                                        = new DefaultMutableTreeNode(deskDisplay);
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
                DefaultMutableTreeNode venueNode = new DefaultMutableTreeNode(venue.toString());
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
     * new data for the Subjects page of the tabbed view
     * @param subjects - new SubjectList
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
     * new data for this page of the tabbed view
     * [9] Reflection usage for accessing private fields in Exam objects.
     * @param exams - new List of data
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
                        java.lang.reflect.Field examTypeField
                                = exam.getClass().getDeclaredField("examType");
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
                    java.lang.reflect.Field subtitleField
                            = exam.getClass().getDeclaredField("subtitle");
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

                model.addRow(
                        new Object[]{exam.getSubject().getTitle(),
                                     examType, paper, subtitle,
                                     unit, exam.getDate().toString(),
                                     exam.getTime().toString()}
                );
            }
        }
    }

    /**
     * new data for this page of the tabbed view
     * [9] Reflection for accessing private Unit fields.
     * @param units - new List of data
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
                    java.lang.reflect.Field descField
                            = unit.getClass().getDeclaredField("description");
                    descField.setAccessible(true);
                    Object descObj = descField.get(unit);
                    if (descObj != null) {
                        description = descObj.toString();
                    }
                } catch (Exception e) {
                    description = "";
                }

                model.addRow(
                        new Object[]{unit.getSubject()
                                     .getTitle(), unit.id().toString(),
                                     unitTitle,
                                     description}
                );
            }
        }
    }

    /**
     * new data for this page of the tabbed view
     * @param students - new List of data
     */
    public void updateStudentPage(StudentList students) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(3);
        JTable table = (JTable) scrollPane.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setColumnIdentifiers(
                new String[]{"LUI", "Full Name", "AARA", "Date of Birth", "Subjects"});

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

                model.addRow(new Object[]{student.getLui(),
                                          displayName,
                                          aaraDisplay,
                                          student.getDob().toString(),
                                          subjectsStr.toString()}
                );
            }
        }
    }

    /**
     * new data for this page of the tabbed view
     * @param venues - new List of data
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
     * new data for this page of the tabbed view
     * @param rooms - new List of data
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
     * Add a listener for the Finalise button
     * @param listener - listener
     */
    public void addFinaliseButtonListener(ActionListener listener) {
        finaliseButton.addActionListener(listener);
    }

    /**
     * Add a listener for the Add button
     * @param listener - listener
     */
    public void addAddButtonListener(ActionListener listener) {
        addButton.addActionListener(listener);
    }

    /**
     * Add a listener for the Clear button
     * @param listener - listener
     */
    public void addClearButtonListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }

    /**
     * set the new title
     * @param title - title
     */
    public void setTitle(String title) {
        frame.setTitle(title);
    }

    /**
     * set the version to something new
     * @param version - new version
     */
    public void setVersion(double version) {
        String currentTitle = frame.getTitle();
        if (currentTitle.contains("(v")) {
            currentTitle = currentTitle.substring(0, currentTitle.indexOf("(v"));
        }
        frame.setTitle(currentTitle + " (v" + version + ")");
    }

    /**
     * return the selected exam rows. Since this is a single-select list ctrl, this is redundant
     * @return the array of one object, or null is failure
     */
    public int[] getSelectedExamRows() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow != -1) {
            return new int[]{selectedRow};
        }
        return null;
    }

    /**
     * get the node from the session tree
     * @return the node
     */
    public DefaultMutableTreeNode getSelectedTreeNode() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            return (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        }
        return null;
    }

    /**
     * clear the selection of any control, as well as some cached values
     */
    public void removeAllSelections() {
        examTable.clearSelection();
        tree.clearSelection();
    }

    /**
     * Checks if there are any un-finalized sessions in the JTree.
     * A session is un-finalized if any Exam node has no Desk children.
     * @return true if un-finalized sessions exist, false otherwise
     */
    public boolean hasUnfinalisedSessions() {
        for (int i = 0; i < sessionRoot.getChildCount(); i++) {
            DefaultMutableTreeNode sessionNode = (DefaultMutableTreeNode) sessionRoot.getChildAt(i);
            for (int j = 0; j < sessionNode.getChildCount(); j++) {
                DefaultMutableTreeNode examNode
                        = (DefaultMutableTreeNode) sessionNode.getChildAt(j);
                if (examNode.getChildCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * here we receive the notifications that model sent us
     * @param property - whatever it is
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

                break;
        }
    }

    /**
     * model initialisation
     * @param model - rewference to the model
     */
    public void setModel(ExamBlockModel model) {
        this.model = model;
        if (model != null) {
            model.addObserver(this);
        }
    }

    /**
     * get the top level window
     * @return the frame handle
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * get the exam table
     * @return the exam table
     */
    public JTable getExamTable() {
        return examTable;
    }

    /**
     * get thee session tree
     * @return get thee session tree
     */
    public JTree getTree() {
        return tree;
    }

    /**
     * get the notebook like tabbed window
     * @return the window
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * the actual button
     * @return the actual button
     */
    public JButton getFinaliseButton() {
        return finaliseButton;
    }

    /**
     * the actual button
     * @return the actual button
     */
    public JButton getAddButton() {
        return addButton;
    }

    /**
     * the actual button
     * @return the actual button
     */
    public JButton getClearButton() {
        return clearButton;
    }

    /**
     * return the Session root node
     * @return the Session root node
     */
    public DefaultMutableTreeNode getSessionRoot() {
        return sessionRoot;
    }

    /**
     * return the Venue root node
     * @return the Venue root node
     */
    public DefaultMutableTreeNode getVenueRoot() {
        return venueRoot;
    }

    /**
     * return the table model
     * @return the table model
     */
    public DefaultTableModel getExamTableModel() {
        return examTableModel;
    }

    /**
     * Add Session object to the SessionNode map
     * @param sessionNode - tree node
     * @param session - Session object
     */
    public void addSessionToSessionNodeMap(DefaultMutableTreeNode sessionNode, Session session) {
        sessionNodeMap.put(sessionNode, session);
    }

    /**
     * return Session object from the SessionNode map
     * @param sessionNode - tree node
     * @return session Session object
     */
    public Session getSessionFromSessionNodeMap(DefaultMutableTreeNode sessionNode) {
        return sessionNodeMap.get(sessionNode);
    }

    /**
     * Add Venue object to the VenueNode map
     * @param venueNode - tree node
     * @param venue - Venue object
     */
    public void addVenueToVenueNodeMap(DefaultMutableTreeNode venueNode, Venue venue) {
        venueNodeMap.put(venueNode, venue);
    }

    /**
     * Get Venue object from the VenueNode map
     * @param venueNode - tree node
     * @return venue Venue object
     */
    public Venue getVenueFromVenueNodeMap(DefaultMutableTreeNode venueNode) {
        return venueNodeMap.get(venueNode);
    }

    /**
     * Mapping from list item to the item represented
     * @param examNode - the object being requested
     * @return the item or null
     */
    public Exam getExamFromExamNodeMap(DefaultMutableTreeNode examNode) {
        return examNodeMap.get(examNode);
    }

    /**
     * mapping from list to item represented
     * @param index - in the list
     * @param exam - what to place there
     */
    public void addExamToExamMap(int index, Exam exam) {
        examMap.put(index, exam);
    }

    /**
     * get Exam stored for index index
     * @param index - index
     * @return item
     */
    public Exam getExamFromExamMap(int index) {
        return examMap.get(index);
    }
}
