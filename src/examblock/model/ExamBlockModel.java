package examblock.model;

import examblock.view.components.Verbose;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The M in the MVC model - this is the source of truth for all the data in the app
 * [5] Observer pattern for model-view communication.
 */
public class ExamBlockModel {

    /** The title of this exam block */
    private String title;

    /** The version number of this exam block */
    private double version;

    /** The filename currently associated with this model */
    private String filename;

    /** The global registry for all objects */
    private Registry registry;

    /** List of observers to notify when data changes */
    private List<ModelObserver> observers;

    /** The list of subjects */
    private SubjectList subjects;

    /** The list of units */
    private UnitList units;

    /** The list of students */
    private StudentList students;

    /** The list of exams */
    private ExamList exams;

    /** The list of rooms */
    private RoomList rooms;

    /** The list of venues */
    private VenueList venues;

    /** The list of sessions */
    private SessionList sessions;

    /**
     * Constructor
     * [2] Registry pattern - centralized object storage and lookup mechanism.
     */
    public ExamBlockModel() {
        this.title = "ExamBlock Data 2025";
        this.version = 1.0;
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
     * Add an observer
     *[5] Generic observer pattern for loose coupling between model and view.
     * @param observer - the new observer to be called on update
     */
    public void addObserver(ModelObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Notify observers that data has changed
     *[5] Observer pattern for notifying view of model changes.
     * @param property - a string naming the property
     */
    public void notifyObservers(String property) {
        for (ModelObserver observer : observers) {
            observer.modelChanged(property);
        }
    }

    /**
     * get the most up-to-date ExamList from the source of truth, the registry
     *
     * @return the ExamList
     */
    public ExamList getExams() {
        return exams;
    }

    /**
     * get the most up-to-date VenueList from the source of truth, the registry
     *
     * @return the VenueList
     */
    public VenueList getVenues() {
        return venues;
    }

    /**
     * get the most up-to-date RoomList from the source of truth, the registry
     *
     * @return the RoomList
     */
    public RoomList getRooms() {
        return rooms;
    }

    /**
     * get the most up-to-date StudentList from the source of truth, the registry
     *
     * @return the StudentList
     */
    public StudentList getStudents() {
        return students;
    }

    /**
     * get the most up-to-date SessionList from the source of truth, the registry
     *
     * @return the SessionList
     */
    public SessionList getSessions() {
        return sessions;
    }

    /**
     * get the most up-to-date SubjectList from the source of truth, the registry
     *
     * @return the SubjectList
     */
    public SubjectList getSubjects() {
        return subjects;
    }

    /**
     * get the most up-to-date UnitList from the source of truth, the registry
     *
     * @return the UnitList
     */
    public UnitList getUnits() {
        return units;
    }

    /**
     * return the Exam Block title
     *
     * @return the Exam Block title
     */
    public String getTitle() {
        return title;
    }

    /**
     * change the title of an Exam Block. Observers advised
     *
     * @param title - to change
     */
    public void setTitle(String title) {
        this.title = title;
        notifyObservers("title_changed");
    }

    /**
     * get the current version number
     *
     * @return the version number
     */
    public double getVersion() {
        return version;
    }

    /**
     * change the version of the Exam Block. Observers advised
     *
     * @param version - the new version. Has to be greater than the current version
     */
    public void setVersion(double version) {
        if (version > this.version) {
            this.version = version;
            notifyObservers("version_changed");
        } else {
            System.out.println("New version must be "
                    + "greater than current version " + this.version);
        }
    }

    /**
     * get the currently loaded filename
     *
     * @return filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * change the filename. Observers advised
     *
     * @param filename - new filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
        notifyObservers("filename_changed");
    }

    /**
     * returns the registry
     *
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * Save a registry to disk. This file format has to match the format expected by loadFromFile.
     * If the file cannot be successfully saved, log a message to the console and return false.
     *
     * Typically, the data file structure looks like this, although you're free to design your own.
     * • Title: ExamBlock Data 2025
     * • Version: 1.0
     * • [Begin]
     * • subjects (see SubjectList, Subject)
     * • units (see UnitList, Unit)
     * • students (see StudentList, Student)
     * • exams (see ExamList, Exam)
     * • rooms (see RoomList, Room)
     * • venues (see VenueList, Venue)
     * • sessions (see SessionList, Session)
     * • [End]
     *
     * [8] File I/O exception handling and error recovery.
     *
     * @param registry - the registry to save
     * @param filename - the filename to save to. If null, a dialog is shown to ask for a filename.
     * @param title - the Exam Block title
     * @param version - the current version number
     * @return true is file saved, otherwise false
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
     * Load a registry from disk. This version will prompt the user for a file to load.
     * For the details of the file format, see loadFromFile(Registry, String).
     */
    public void loadFromFile() {
        System.out.println("Please specify a filename to load from.");
    }

    /**
     * Load a registry from disk. This file format has to match the format written by saveToFile.
     * If the file cannot be loaded due to an IOException or RuntimeException, terminates the app.
     *
     * Typically, the data file structure looks like this, although you're free to design your own.
     * • Title: ExamBlock Data 2025
     * • Version: 1.0
     * • [Begin]
     * • subjects (see SubjectList, Subject)
     * • units (see UnitList, Unit)
     * • students (see StudentList, Student)
     * • exams (see ExamList, Exam)
     * • rooms (see RoomList, Room)
     * • venues (see VenueList, Venue)
     * • sessions (see SessionList, Session)
     * • [End]
     *
     * [8] Exception handling in file I/O operations.
     *
     * @param registry - the registry to fill
     * @param filename - the filename to load from
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
                System.out.println("Loaded " + subjects.size() + " subjects"); //test

                System.out.println("Available subjects after loading:");
                for (Subject s : registry.getAll(Subject.class)) {
                    System.out.println("  - ID: '" + s.getId() + "', Title: '"
                            + s.getTitle() + "'");
                }

                System.out.println("=== Loading Units ===");
                units = new UnitList(registry);
                units.streamIn(br, registry, 0);
                System.out.println("Loaded " + units.size() + " units"); //test

                System.out.println("=== Loading Students ===");
                students = new StudentList(registry);
                students.streamIn(br, registry, 0);
                System.out.println("Loaded " + students.size() + " students"); //test

                System.out.println("=== Loading Exams ===");
                exams = new ExamList(registry);
                exams.streamIn(br, registry, 0);
                System.out.println("Loaded " + exams.size() + " exams"); //test

                System.out.println("=== Loading Rooms ===");
                rooms = new RoomList(registry);
                rooms.streamIn(br, registry, 0);
                System.out.println("Loaded " + rooms.size() + " rooms"); //test

                System.out.println("=== Loading Venues ===");
                venues = new VenueList(registry);
                venues.streamIn(br, registry, 0);
                System.out.println("Loaded " + venues.size() + " venues"); //test

                System.out.println("=== Loading Sessions ===");
                sessions = new SessionList(registry);
                sessions.streamIn(br, registry, 0);
                System.out.println("Loaded " + sessions.size() + " sessions"); //test

                // End
                String endLine = CSSE7023.getLine(br);
                if (endLine == null || !endLine.equals("[End]")) {
                    System.out.println("Warning: missing [End] marker in file"); //test
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