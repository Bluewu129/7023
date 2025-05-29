package examblock.model;

import examblock.view.components.DialogUtils;
import examblock.view.components.Verbose;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The main model class for the ExamBlock application.
 * Manages all the data and business logic for exam scheduling.
 */
public class ExamBlockModel {

    private String title;
    private double version;
    private Registry registry;
    private SubjectList subjects;
    private UnitList units;
    private ExamList exams;
    private StudentList students;
    private RoomList rooms;
    private VenueList venues;
    private SessionList sessions;
    private List<ModelObserver> observers;

    /**
     * Constructs a new ExamBlockModel with default values.
     */
    public ExamBlockModel() {
        this.title = "Exam Block";
        this.version = 1.0;
        this.registry = new RegistryImpl();
        this.subjects = new SubjectList(registry);
        this.units = new UnitList(registry);
        this.exams = new ExamList(registry);
        this.students = new StudentList(registry);
        this.rooms = new RoomList(registry);
        this.venues = new VenueList(registry);
        this.sessions = new SessionList();
        this.observers = new ArrayList<>();
    }

    /**
     * Gets the title of this exam block.
     *
     * @return the title of this exam block
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this exam block.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
        notifyObservers("title");
    }

    /**
     * Gets the version of this exam block.
     *
     * @return the version number
     */
    public double getVersion() {
        return version;
    }

    /**
     * Sets the version of this exam block.
     *
     * @param version the new version number
     */
    public void setVersion(double version) {
        this.version = version;
        notifyObservers("version");
    }

    /**
     * Gets the registry for this model.
     *
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Gets the subject list.
     *
     * @return the subject list
     */
    public SubjectList getSubjects() {
        return subjects;
    }

    /**
     * Gets the unit list.
     *
     * @return the unit list
     */
    public UnitList getUnits() {
        return units;
    }

    /**
     * Gets the exam list.
     *
     * @return the exam list
     */
    public ExamList getExams() {
        return exams;
    }

    /**
     * Gets the student list.
     *
     * @return the student list
     */
    public StudentList getStudents() {
        return students;
    }

    /**
     * Gets the room list.
     *
     * @return the room list
     */
    public RoomList getRooms() {
        return rooms;
    }

    /**
     * Gets the venue list.
     *
     * @return the venue list
     */
    public VenueList getVenues() {
        return venues;
    }

    /**
     * Gets the session list.
     *
     * @return the session list
     */
    public SessionList getSessions() {
        return sessions;
    }

    /**
     * Adds an observer to this model.
     *
     * @param observer the observer to add
     */
    public void addObserver(ModelObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer from this model.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(ModelObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all observers of a change.
     *
     * @param property the property that changed
     */
    public void notifyObservers(String property) {
        for (ModelObserver observer : observers) {
            observer.modelChanged(property);
        }
    }

    /**
     * Loads data from a file.
     *
     * @param filename the file to load from
     * @return true if successful, false otherwise
     */
    public boolean loadFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Clear existing data
            registry.clear();
            subjects = new SubjectList(registry);
            units = new UnitList(registry);
            exams = new ExamList(registry);
            students = new StudentList(registry);
            rooms = new RoomList(registry);
            venues = new VenueList(registry);
            sessions = new SessionList();

            // Read header information
            String line = CSSE7023.getLine(br);
            if (line != null) {
                String[] headerParts = CSSE7023.keyValuePair(line);
                if (headerParts != null && headerParts[0].equals("Title")) {
                    title = headerParts[1];
                }
            }

            line = CSSE7023.getLine(br);
            if (line != null) {
                String[] versionParts = CSSE7023.keyValuePair(line);
                if (versionParts != null && versionParts[0].equals("Version")) {
                    version = CSSE7023.toDouble(versionParts[1], "Invalid version format");
                }
            }

            // Load each section - only call streamIn if the class actually implements it
            // For now, skip streaming for classes that don't have it implemented yet
            try {
                if (subjects instanceof StreamManager) {
                    ((StreamManager) subjects).streamIn(br, registry, 1);
                }
            } catch (Exception e) {
                System.out.println("Skipping subjects loading: " + e.getMessage());
            }

            // Skip other sections for now until streaming is properly implemented
            System.out.println("Basic file structure loaded. Full streaming implementation pending.");

            notifyObservers("loaded");
            return true;

        } catch (IOException | RuntimeException e) {
            if (Verbose.isVerbose()) {
                System.err.println("Error loading file: " + e.getMessage());
            }
            DialogUtils.showMessage("Error loading file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves data to a file.
     *
     * @param registry the registry (unused, for compatibility)
     * @param filename the filename to save to (null to prompt user)
     * @param title the title for the save dialog
     * @param version the version for the save dialog
     * @return true if successful, false otherwise
     */
    public boolean saveToFile(Registry registry, String filename, String title, double version) {
        if (filename == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Exam Block Data");
            fileChooser.setSelectedFile(new File(title + " (v" + version + ").ebd"));

            int result = fileChooser.showSaveDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            filename = fileChooser.getSelectedFile().getAbsolutePath();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write header
            bw.write("Title: " + this.title + System.lineSeparator());
            bw.write("Version: " + this.version + System.lineSeparator());
            bw.write(System.lineSeparator());

            // Write each section - only call streamOut if implemented
            try {
                if (subjects instanceof StreamManager) {
                    ((StreamManager) subjects).streamOut(bw, 1);
                }
            } catch (Exception e) {
                bw.write("# Subjects section skipped\n");
            }

            // Simple placeholder sections for now
            bw.write("# Other sections not yet implemented\n");

            if (Verbose.isVerbose()) {
                System.out.println("File saved successfully to: " + filename);
            }

            notifyObservers("saved");
            return true;

        } catch (IOException e) {
            if (Verbose.isVerbose()) {
                System.err.println("Error saving file: " + e.getMessage());
            }
            DialogUtils.showMessage("Error saving file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the version number from user input.
     *
     * @return the version number, or BAD_VERSION if cancelled
     */
    public double getVersionNumber() {
        String input = JOptionPane.showInputDialog(null,
                "Enter version number:",
                "Version",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) {
            return CSSE7023.BAD_VERSION;
        }

        try {
            return Double.parseDouble(input.trim());
        } catch (NumberFormatException e) {
            return CSSE7023.BAD_VERSION;
        }
    }
}