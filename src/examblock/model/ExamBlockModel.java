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
    private String filename;
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
        this.filename = "";
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
     * Gets the filename.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename.
     *
     * @param filename the filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
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
     * Loads data from a file - simple version.
     *
     * @return true if successful, false otherwise
     */
    public void loadFromFile() {
        if (filename != null && !filename.isEmpty()) {
            loadFromFile(filename);
        }
    }

    /**
     * Loads data from a file with registry parameter.
     *
     * @param registry the registry (ignored, uses own registry)
     * @return true if successful, false otherwise
     */
    public void loadFromFile(Registry registry) {
        loadFromFile();
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
            if (line != null && line.startsWith("Title:")) {
                String[] headerParts = CSSE7023.keyValuePair(line);
                if (headerParts != null) {
                    title = headerParts[1];
                }
            }

            line = CSSE7023.getLine(br);
            if (line != null && line.startsWith("Version:")) {
                String[] versionParts = CSSE7023.keyValuePair(line);
                if (versionParts != null) {
                    version = CSSE7023.toDouble(versionParts[1], "Invalid version format");
                }
            }

            // Skip the [Begin] marker
            line = CSSE7023.getLine(br);
            if (line == null || !line.equals("[Begin]")) {
                throw new RuntimeException("Expected [Begin] marker but found: " + line);
            }

            // Load each section
            while ((line = CSSE7023.getLine(br, true)) != null) {
                if (line.equals("[End]")) {
                    // Read the actual line to consume it
                    CSSE7023.getLine(br);
                    break;
                }

                if (line.startsWith("[Subjects:")) {
                    subjects.streamIn(br, registry, 1);
                } else if (line.startsWith("[Units:")) {
                    units.streamIn(br, registry, 1);
                } else if (line.startsWith("[Students:")) {
                    students.streamIn(br, registry, 1);
                } else if (line.startsWith("[Exams:")) {
                    exams.streamIn(br, registry, 1);
                } else if (line.startsWith("[Rooms:")) {
                    rooms.streamIn(br, registry, 1);
                } else if (line.startsWith("[Venues:")) {
                    // Important: Venues must be loaded AFTER Rooms
                    venues.streamIn(br, registry, 1);
                } else if (line.startsWith("[Sessions:")) {
                    // Sessions need special handling
                    loadSessions(br);
                } else {
                    // Unknown section, skip the line
                    CSSE7023.getLine(br);
                }
            }

            // After loading, we need to establish relationships
            establishRelationships();

            // Set the filename for future saves
            this.filename = filename;

            if (Verbose.isVerbose()) {
                System.out.println("File loaded successfully!");
                System.out.println("Loaded " + subjects.size() + " subjects");
                System.out.println("Loaded " + units.size() + " units");
                System.out.println("Loaded " + students.size() + " students");
                System.out.println("Loaded " + exams.size() + " exams");
                System.out.println("Loaded " + rooms.size() + " rooms");
                System.out.println("Loaded " + venues.size() + " venues");
                System.out.println("Loaded " + sessions.all().size() + " sessions");
            }

            notifyObservers("loaded");
            return true;

        } catch (IOException | RuntimeException e) {
            if (Verbose.isVerbose()) {
                System.err.println("Error loading file: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Loads sessions from the file.
     * Sessions have a special format that needs custom parsing.
     */
    private void loadSessions(BufferedReader br) throws IOException {
        String line = CSSE7023.getLine(br);
        if (line == null || !line.startsWith("[Sessions:")) {
            throw new RuntimeException("Expected [Sessions: N] but got: " + line);
        }

        // Parse session count
        String[] parts = line.substring(1, line.length() - 1).split(": ");
        int sessionCount = Integer.parseInt(parts[1]);

        if (Verbose.isVerbose()) {
            System.out.println("\nLoading " + sessionCount + " sessions...\n");
        }

        // Read each session
        for (int i = 1; i <= sessionCount; i++) {
            // Read session header: "1. Venue: V1+V2+V3, Session Number: 1, Day: 2025-03-10, Start: 12:30, Exams: 2"
            String sessionHeader = CSSE7023.getLine(br);
            if (sessionHeader == null) {
                throw new RuntimeException("EOF reading Session #" + i);
            }

            // Parse the session header
            String[] headerParts = sessionHeader.split("\\. ", 2);
            if (headerParts.length != 2) {
                throw new RuntimeException("Invalid session header format: " + sessionHeader);
            }

            // Parse session details
            String details = headerParts[1];
            String venueId = null;
            int sessionNumber = 0;
            String day = null;
            String start = null;
            int examCount = 0;

            // Parse key-value pairs
            String[] detailParts = details.split(", ");
            for (String part : detailParts) {
                String[] kv = part.split(": ");
                if (kv.length == 2) {
                    switch (kv[0]) {
                        case "Venue":
                            venueId = kv[1];
                            break;
                        case "Session Number":
                            sessionNumber = Integer.parseInt(kv[1]);
                            break;
                        case "Day":
                            day = kv[1];
                            break;
                        case "Start":
                            start = kv[1];
                            break;
                        case "Exams":
                            examCount = Integer.parseInt(kv[1]);
                            break;
                    }
                }
            }

            // Create the session
            Venue venue = venues.find(venueId);
            if (venue == null) {
                throw new RuntimeException("Venue not found for session: " + venueId);
            }

            Session session = new Session(venue, sessionNumber,
                    CSSE7023.toLocalDate(day, "Invalid date format"),
                    CSSE7023.toLocalTime(start, "Invalid time format"), registry);

            // Read exam allocations for this session
            for (int j = 0; j < examCount; j++) {
                String examLine = CSSE7023.getLine(br);
                if (examLine != null && !examLine.trim().isEmpty()) {
                    // Remove leading spaces if any
                    examLine = examLine.trim();

                    // Extract student count from the exam line
                    // Format: "Subject Name (N students)"
                    int studentCount = 0;
                    String examTitle = examLine;

                    int startParen = examLine.lastIndexOf('(');
                    int endParen = examLine.lastIndexOf(')');

                    if (startParen > 0 && endParen > startParen) {
                        // Extract the title without the student count
                        examTitle = examLine.substring(0, startParen).trim();

                        String countStr = examLine.substring(startParen + 1, endParen);
                        countStr = countStr.replace("students", "").replace("student", "").trim();
                        try {
                            studentCount = Integer.parseInt(countStr);
                        } catch (NumberFormatException e) {
                            if (Verbose.isVerbose()) {
                                System.out.println("Could not parse student count from: " + countStr);
                            }
                        }
                    }

                    // Find the exam by matching the subject title
                    Exam exam = null;
                    for (Exam e : exams.all()) {
                        if (e.getSubject().getTitle().equals(examTitle)) {
                            exam = e;
                            break;
                        }
                    }

                    if (exam == null) {
                        // Try matching by partial title
                        for (Exam e : exams.all()) {
                            if (examTitle.contains(e.getSubject().getTitle()) ||
                                    e.getSubject().getTitle().contains(examTitle)) {
                                exam = e;
                                break;
                            }
                        }
                    }

                    if (exam != null) {
                        // Check if there's a [Desks: N] line following
                        String nextLine = CSSE7023.getLine(br, true); // Peek

                        if (nextLine != null && nextLine.trim().startsWith("[Desks:")) {
                            // Consume the [Desks: N] line
                            CSSE7023.getLine(br);

                            // Skip all desk allocation lines
                            String deskLine;
                            while ((deskLine = CSSE7023.getLine(br, true)) != null) {
                                // Check if this is a desk line
                                if (deskLine.trim().isEmpty() ||
                                        (!deskLine.contains("Desk") && !deskLine.contains(",") && !deskLine.contains("."))) {
                                    break;
                                }
                                // Consume the line
                                CSSE7023.getLine(br);
                            }
                        }

                        session.scheduleExam(exam, studentCount);
                    } else if (Verbose.isVerbose()) {
                        System.out.println("Warning: Exam not found for title: " + examTitle);
                    }
                }
            }

            sessions.add(session);
        }

        if (Verbose.isVerbose()) {
            System.out.println("Successfully loaded " + sessions.all().size() + " sessions");
        }
    }

    /**
     * Establishes relationships between loaded entities.
     * For example, linking students to their subjects and exams.
     */
    private void establishRelationships() {
        // Link students to their exams based on subjects
        for (Student student : students.all()) {
            // Clear any existing exams
            student.clearExams();

            // For each subject the student is enrolled in
            for (Subject subject : student.getSubjectsList()) {
                // Find all exams for this subject
                for (Exam exam : exams.all()) {
                    if (exam.getSubject().equals(subject)) {
                        // Add this exam to the student's exam list
                        student.addExam(exam);
                    }
                }
            }
        }

        if (Verbose.isVerbose()) {
            System.out.println("Established relationships between entities");
            System.out.println("Linked students to their exams based on enrolled subjects");
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
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Exam Block Files (*.ebd)", "ebd"));

            int result = fileChooser.showSaveDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            filename = fileChooser.getSelectedFile().getAbsolutePath();

            // Ensure .ebd extension
            if (!filename.toLowerCase().endsWith(".ebd")) {
                filename = filename + ".ebd";
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write header
            bw.write("Title: " + this.title + System.lineSeparator());
            bw.write("Version: " + this.version + System.lineSeparator());
            bw.write(System.lineSeparator());
            bw.write("[Begin]" + System.lineSeparator());
            bw.write(System.lineSeparator());

            // Write each section
            subjects.streamOut(bw, 1);
            units.streamOut(bw, 1);
            students.streamOut(bw, 1);
            exams.streamOut(bw, 1);
            rooms.streamOut(bw, 1);
            venues.streamOut(bw, 1);

            // Write sessions (need special handling)
            bw.write("[Sessions: " + sessions.all().size() + "]" + System.lineSeparator());
            int sessionIndex = 1;
            for (Session session : sessions.all()) {
                bw.write(sessionIndex + ". Venue: " + session.getVenue().venueId() +
                        ", Session Number: " + session.getSessionNumber() +
                        ", Day: " + session.getDate() +
                        ", Start: " + session.getTime() +
                        ", Exams: " + session.getExams().size() + System.lineSeparator());

                // Write exam details for this session
                for (Exam exam : session.getExams()) {
                    bw.write("    " + exam.getSubject().getTitle() +
                            " (" + session.countStudents() + " students)" + System.lineSeparator());

                    // If finalized, write desk allocations
                    if (session.countStudents() > 0) {
                        bw.write("    [Desks: " + session.countStudents() + "]" + System.lineSeparator());
                        // Note: Actual desk allocations would be written here if we stored them
                    }
                }
                sessionIndex++;
            }

            bw.write(System.lineSeparator());
            bw.write("[End]" + System.lineSeparator());

            // Update our stored filename
            this.filename = filename;

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
}