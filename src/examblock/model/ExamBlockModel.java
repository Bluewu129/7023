package examblock.model;

import examblock.view.components.Verbose;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Core data model representing the complete state of an exam block system.
 * Implements the Model component of MVC architecture with observer pattern support.
 * Manages all entity collections and provides comprehensive file persistence capabilities.
 */
public class ExamBlockModel {

    /** Default title for new exam block instances */
    private static final String DEFAULT_TITLE = "ExamBlock Data 2025";

    /** Default version number for new exam block instances */
    private static final double DEFAULT_VERSION = 1.0;

    /** Title of this exam block */
    private String title;

    /** Version number of this exam block */
    private double version;

    /** Currently associated filename for this model */
    private String filename;

    /** Central registry for all managed objects using registry pattern */
    private Registry registry;

    /** Collection of observers for model change notifications */
    private List<ModelObserver> observers;

    /** Managed collection of academic subjects */
    private SubjectList subjects;

    /** Managed collection of subject units */
    private UnitList units;

    /** Managed collection of students */
    private StudentList students;

    /** Managed collection of exams */
    private ExamList exams;

    /** Managed collection of rooms */
    private RoomList rooms;

    /** Managed collection of exam venues */
    private VenueList venues;

    /** Managed collection of exam sessions */
    private SessionList sessions;

    /**
     * Initializes a new exam block model with default values and empty collections.
     * Sets up registry pattern for centralized object 
     * management and observer list for notifications.
     */
    public ExamBlockModel() {
        this.title = DEFAULT_TITLE;
        this.version = DEFAULT_VERSION;
        this.filename = "";
        this.registry = new RegistryImpl();
        this.observers = new ArrayList<>();
        this.subjects = new SubjectList(registry);
        this.units = new UnitList(registry);
        this.students = new StudentList(registry);
        this.exams = new ExamList(registry);
        this.rooms = new RoomList(registry);
        this.venues = new VenueList(registry);
        this.sessions = new SessionList(registry);
    }

    /**
     * Registers an observer for model change notifications.
     * Implements generic observer pattern for loose coupling between model and view.
     * Prevents duplicate registrations of the same observer instance.
     *
     * @param observer the observer to register for model change notifications
     */
    public void addObserver(ModelObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Notifies all registered observers of model state changes.
     * Used to maintain view synchronization when model data is modified.
     *
     * @param property descriptive name of the changed property
     */
    public void notifyObservers(String property) {
        for (ModelObserver observer : observers) {
            observer.modelChanged(property);
        }
    }

    /**
     * Retrieves the most current exam list from the registry source of truth.
     *
     * @return the managed exam collection
     */
    public ExamList getExams() {
        return exams;
    }

    /**
     * Retrieves the most current venue list from the registry source of truth.
     *
     * @return the managed venue collection
     */
    public VenueList getVenues() {
        return venues;
    }

    /**
     * Retrieves the most current room list from the registry source of truth.
     *
     * @return the managed room collection
     */
    public RoomList getRooms() {
        return rooms;
    }

    /**
     * Retrieves the most current student list from the registry source of truth.
     *
     * @return the managed student collection
     */
    public StudentList getStudents() {
        return students;
    }

    /**
     * Retrieves the most current session list from the registry source of truth.
     *
     * @return the managed session collection
     */
    public SessionList getSessions() {
        return sessions;
    }

    /**
     * Retrieves the most current subject list from the registry source of truth.
     *
     * @return the managed subject collection
     */
    public SubjectList getSubjects() {
        return subjects;
    }

    /**
     * Retrieves the most current unit list from the registry source of truth.
     *
     * @return the managed unit collection
     */
    public UnitList getUnits() {
        return units;
    }

    /**
     * Gets the current exam block title.
     *
     * @return the exam block title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the exam block title and notifies observers of the change.
     *
     * @param title the new title for this exam block
     */
    public void setTitle(String title) {
        this.title = title;
        notifyObservers("title_changed");
    }

    /**
     * Gets the current version number.
     *
     * @return the current version number
     */
    public double getVersion() {
        return version;
    }

    /**
     * Updates the version number with validation to ensure version progression.
     * Only accepts versions greater than the current version to maintain version integrity.
     *
     * @param version the new version number (must be greater than current version)
     */
    public void setVersion(double version) {
        if (version > this.version) {
            this.version = version;
            notifyObservers("version_changed");
        } else {
            System.out.println("New version must be greater than current version " + this.version);
        }
    }

    /**
     * Gets the currently associated filename.
     *
     * @return the current filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Updates the associated filename and notifies observers of the change.
     *
     * @param filename the new filename for this exam block
     */
    public void setFilename(String filename) {
        this.filename = filename;
        notifyObservers("filename_changed");
    }

    /**
     * Provides access to the central registry for object management.
     *
     * @return the registry instance
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Persists registry data to disk using structured file format.
     * Creates comprehensive data file containing title, version, and all entity collections.
     * Handles file I/O exceptions gracefully and provides user feedback.
     *
     * File structure format:
     * - Title: [exam block title]
     * - Version: [version number]
     * - [Begin]
     * - [Entity collections in defined order]
     * - [End]
     *
     * @param registry the registry containing all entities to save
     * @param filename target filename for saving, null prompts user dialog
     * @param title the exam block title for the file
     * @param version the version number for this save operation
     * @return true if save operation successful, false otherwise
     */
    public boolean saveToFile(Registry registry, String filename, String title, double version) {
        try {
            if (filename == null) {
                System.out.println("Please specify a filename to save to.");
                return false;
            }

            this.filename = filename;
            this.title = title;
            this.version = version;

            if (Verbose.isVerbose()) {
                System.out.println("Saving to file: " + filename);
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
                bw.write("Title: " + title + System.lineSeparator());
                bw.write("Version: " + version + System.lineSeparator());
                bw.write("[Begin]" + System.lineSeparator());

                subjects.streamOut(bw, 0);
                units.streamOut(bw, 0);
                students.streamOut(bw, 0);
                exams.streamOut(bw, 0);
                rooms.streamOut(bw, 0);
                venues.streamOut(bw, 0);
                sessions.streamOut(bw, 0);

                bw.write("[End]" + System.lineSeparator());

                System.out.println("Successfully saved data to " + filename);
                notifyObservers("file_saved");
                return true;
            }

        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error saving file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Prompts user for filename when no specific file is provided.
     * Used when load operation is initiated without a target file.
     */
    public void loadFromFile() {
        System.out.println("Please specify a filename to load from.");
    }

    /**
     * Loads complete registry data from a structured data file.
     * Reconstructs all entity collections and updates registry accordingly.
     * Terminates application on IOException or RuntimeException as per specification.
     *
     * Expected file structure:
     * - Title: [exam block title]
     * - Version: [version number]
     * - [Begin]
     * - subjects (see SubjectList, Subject)
     * - units (see UnitList, Unit)
     * - students (see StudentList, Student)
     * - exams (see ExamList, Exam)
     * - rooms (see RoomList, Room)
     * - venues (see VenueList, Venue)
     * - sessions (see SessionList, Session)
     * - [End]
     *
     * @param registry the registry to populate with loaded entities
     * @param filename the source filename to load from
     */
    public void loadFromFile(Registry registry, String filename) {
        try {
            this.filename = filename;
            this.registry = registry;

            System.out.println("Loading from file: " + filename);

            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String titleLine = CSSE7023.getLine(br);
                if (titleLine != null && titleLine.startsWith("Title: ")) {
                    this.title = titleLine.substring("Title: ".length());
                }

                String versionLine = CSSE7023.getLine(br);
                if (versionLine != null && versionLine.startsWith("Version: ")) {
                    this.version = Double.parseDouble(versionLine.substring("Version: ".length()));
                }

                String beginLine = CSSE7023.getLine(br);
                if (beginLine == null || !beginLine.equals("[Begin]")) {
                    throw new RuntimeException("Invalid file format - missing [Begin] marker");
                }

                registry.clear();

                System.out.println("=== Loading Subjects ===");
                subjects = new SubjectList(registry);
                subjects.streamIn(br, registry, 0);
                System.out.println("Loaded " + subjects.size() + " subjects");

                System.out.println("Available subjects after loading:");
                for (Subject s : registry.getAll(Subject.class)) {
                    System.out.println("  - ID: '" + s.getId() 
                            + "', Title: '" + s.getTitle() + "'");
                }

                System.out.println("=== Loading Units ===");
                units = new UnitList(registry);
                units.streamIn(br, registry, 0);
                System.out.println("Loaded " + units.size() + " units");

                System.out.println("=== Loading Students ===");
                students = new StudentList(registry);
                students.streamIn(br, registry, 0);
                System.out.println("Loaded " + students.size() + " students");

                System.out.println("=== Loading Exams ===");
                exams = new ExamList(registry);
                exams.streamIn(br, registry, 0);
                System.out.println("Loaded " + exams.size() + " exams");

                System.out.println("=== Loading Rooms ===");
                rooms = new RoomList(registry);
                rooms.streamIn(br, registry, 0);
                System.out.println("Loaded " + rooms.size() + " rooms");

                System.out.println("=== Loading Venues ===");
                venues = new VenueList(registry);
                venues.streamIn(br, registry, 0);
                System.out.println("Loaded " + venues.size() + " venues");

                System.out.println("=== Loading Sessions ===");
                sessions = new SessionList(registry);
                sessions.streamIn(br, registry, 0);
                System.out.println("Loaded " + sessions.size() + " sessions");

                // Validate end marker with warning if missing
                String endLine = CSSE7023.getLine(br);
                if (endLine == null || !endLine.equals("[End]")) {
                    System.out.println("Warning: missing [End] marker in file");
                }

                System.out.println("Successfully loaded all data from " + filename);
                notifyObservers("file_loaded");
            }

        } catch (IOException e) {
            System.err.println("IOException loading file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println("RuntimeException loading file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error loading file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}