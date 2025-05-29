package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * An object describing a single {@link Exam} {@code Session}.
 * An exam "session" is a block of time in a particular {@link Venue}, with zero or more {@link Exam}s.
 * Sessions are numbered from 1 and unique in each venue, but not across venues.
 * Session number can be, but do not have to be, in chronological order of session start times.
 * That is, a new session may be inserted earlier into an existing schedule.
 * Session numbers do not have to necessarily be sequential.
 */
public class Session implements StreamManager, ManageableListItem {

    /** The venue for this session. */
    private final Venue venue;
    /** The session number of this session in this venue. */
    private final int sessionNumber;
    /** The day this session is occurring. */
    private final LocalDate day;
    /** The start time of this session. */
    private final LocalTime start;
    /** The list of exams in this session in this venue. */
    private final List<Exam> exams;
    /** Map of exam to student count for that exam */
    private final Map<Exam, Integer> examStudentCounts;
    /** The number of rows of desks set up for this session. */
    private int rows;
    /** The number of columns of desks set up for this session. */
    private int columns;
    /** The total number of desks available for this session. */
    private int totalDesks;
    /** The 2D array (row x column) of all desks available for this session. */
    private Desk[][] desks;
    /** The registry for dependencies. */
    private Registry registry;

    /**
     * Constructs a new empty {@link Exam} {@code Session} for a particular {@link Venue}.
     * The calling process must check that the supplied session number is unique for this venue.
     * Session numbers do not have to be sequential, only unique.
     * The constructor must also prepare the empty (unassigned as yet) desks that will be
     * used in this session. (The session has the same rows and columns of desks as the venue.)
     * As per specification - Registry is last parameter
     *
     * @param venue the exam venue for the new session.
     * @param sessionNumber the number (unique by venue) of the new session.
     * @param day the session date.
     * @param start the start time of the exam window.
     * @param registry the registry
     */
    public Session(Venue venue, int sessionNumber, LocalDate day, LocalTime start, Registry registry) {
        this.venue = venue;
        this.sessionNumber = sessionNumber;
        this.day = day;
        this.start = start;
        this.exams = new ArrayList<>();
        this.examStudentCounts = new HashMap<>();
        this.registry = registry;
        rows = venue.getRows();
        columns = venue.getColumns();
        totalDesks = venue.deskCount();
        desks = new Desk[rows][columns];
        initializeDesks();

        if (registry != null) {
            registry.add(this, Session.class);
        }
    }

    /**
     * Constructs a Session by reading from a BufferedReader.
     * As per specification
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve references
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic failure
     */
    public Session(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;

        String line = CSSE7023.getLine(br);
        if (line == null) throw new RuntimeException("EOF reading Session #" + nthItem);

        String[] idxAndRest = line.split("\\. ", 2);
        if (idxAndRest.length != 2) throw new RuntimeException("Session header format error: " + line);

        int idx = CSSE7023.toInt(idxAndRest[0], "Number format exception parsing Session " + nthItem + " header");
        if (idx != nthItem) throw new RuntimeException("Session index out of sync!");

        // Parse session data
        String[] parts = idxAndRest[1].split(", ");
        if (parts.length < 5) throw new RuntimeException("Session data format error: " + idxAndRest[1]);

        String venueId = null;
        int sessionNum = 0;
        LocalDate sessionDay = null;
        LocalTime sessionStart = null;
        int examCount = 0;

        for (String part : parts) {
            String[] kv = part.split(": ");
            if (kv.length == 2) {
                switch (kv[0]) {
                    case "Venue":
                        venueId = kv[1];
                        break;
                    case "Session Number":
                        sessionNum = Integer.parseInt(kv[1]);
                        break;
                    case "Day":
                        sessionDay = LocalDate.parse(kv[1]);
                        break;
                    case "Start":
                        sessionStart = LocalTime.parse(kv[1]);
                        break;
                    case "Exams":
                        examCount = Integer.parseInt(kv[1]);
                        break;
                }
            }
        }

        Venue sessionVenue = registry.find(venueId, Venue.class);
        if (sessionVenue == null) throw new RuntimeException("Venue not found: " + venueId);

        this.venue = sessionVenue;
        this.sessionNumber = sessionNum;
        this.day = sessionDay;
        this.start = sessionStart;
        this.exams = new ArrayList<>();
        this.examStudentCounts = new HashMap<>();
        rows = venue.getRows();
        columns = venue.getColumns();
        totalDesks = venue.deskCount();
        desks = new Desk[rows][columns];
        initializeDesks();

        // Read exam data
        for (int i = 0; i < examCount; i++) {
            String examLine = CSSE7023.getLine(br);
            if (examLine != null && !examLine.trim().isEmpty()) {
                examLine = examLine.trim();

                // Parse exam info and student count
                int studentCount = 0;
                String examTitle = examLine;

                int startParen = examLine.lastIndexOf('(');
                int endParen = examLine.lastIndexOf(')');

                if (startParen > 0 && endParen > startParen) {
                    examTitle = examLine.substring(0, startParen).trim();
                    String countStr = examLine.substring(startParen + 1, endParen);
                    countStr = countStr.replace("students", "").replace("student", "").trim();
                    try {
                        studentCount = Integer.parseInt(countStr);
                    } catch (NumberFormatException e) {
                        // Ignore parsing errors
                    }
                }

                // Find the exam
                Exam exam = null;
                List<Exam> allExams = registry.getAll(Exam.class);
                for (Exam e : allExams) {
                    if (e.getSubject().getTitle().equals(examTitle)) {
                        exam = e;
                        break;
                    }
                }

                if (exam != null) {
                    // Skip desk allocation data if present
                    String nextLine = CSSE7023.getLine(br, true);
                    if (nextLine != null && nextLine.trim().startsWith("[Desks:")) {
                        CSSE7023.getLine(br); // Consume [Desks: N] line

                        // Skip desk lines
                        String deskLine;
                        while ((deskLine = CSSE7023.getLine(br, true)) != null) {
                            if (deskLine.trim().isEmpty() ||
                                    (!deskLine.contains("Desk") && !deskLine.contains(",") && !deskLine.contains("."))) {
                                break;
                            }
                            CSSE7023.getLine(br);
                        }
                    }

                    scheduleExam(exam, studentCount);
                }
            }
        }

        if (registry != null) {
            registry.add(this, Session.class);
        }
    }

    // Backward compatibility constructor (delegates to new one with null registry)
    @Deprecated
    public Session(Venue venue, int sessionNumber, LocalDate day, LocalTime start) {
        this(venue, sessionNumber, day, start, null);
    }

    private void initializeDesks() {
        int deskId = 1;
        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < rows; i++) {
                desks[i][j] = new Desk(deskId++);
            }
        }
    }

    /**
     * Gets the venue of this session.
     *
     * @return The venue of this session.
     */
    public Venue getVenue() {
        return venue;
    }

    /**
     * Gets the sessionNumber of this session.
     *
     * @return The sessionNumber of this session.
     */
    public int getSessionNumber() {
        return sessionNumber;
    }

    /**
     * Gets the date of this session.
     *
     * @return The date of this session.
     */
    public LocalDate getDate() {
        return day;
    }

    /**
     * Gets the start time of this session.
     *
     * @return The start time of this session.
     */
    public LocalTime getTime() {
        return start;
    }

    /**
     * Gets the total number of desks in this session.
     *
     * @return The total number of desks.
     */
    public int getTotalDesks() {
        return totalDesks;
    }

    /**
     * Gets the list of exams being held in this session.
     *
     * @return The list of exams being held in this session.
     */
    public List<Exam> getExams() {
        return new ArrayList<>(exams);
    }

    /**
     * Counts the number of students already scheduled in this {@code Session}.
     * This counts unique students across all exams (a student taking multiple exams
     * in the same session only counts once).
     *
     * @return The number of students already scheduled in this session.
     */
    public int countStudents() {
        int total = 0;
        for (Integer count : examStudentCounts.values()) {
            total += count;
        }
        return total;
    }

    /**
     * Removes an exam from this session.
     *
     * @param exam the exam to remove
     */
    public void removeExam(Exam exam) {
        exams.remove(exam);
        examStudentCounts.remove(exam);
    }

    /**
     * Allocates an exam to this session (Venue and time).
     *
     * @param exam the exam to be allocated to this venue.
     */
    public void scheduleExam(Exam exam) {
        scheduleExam(exam, 0);
    }

    /**
     * Allocates an exam to this session (Venue and time).
     *
     * @param exam the exam to be allocated to this venue.
     * @param numberStudents the number of students being added with this allocation.
     */
    public void scheduleExam(Exam exam, int numberStudents) {
        if (!exams.contains(exam)) {
            exams.add(exam);
        }
        examStudentCounts.put(exam, numberStudents);
    }

    /**
     * Get the number of students for a specific exam in this session.
     */
    public int getStudentCountForExam(Exam exam) {
        return examStudentCounts.getOrDefault(exam, 0);
    }

    /**
     * Allocates {@link Student}s to {@link Desk}s for every {@link Exam} in this {@link Session}.
     *
     * @param exams the current set of Year 12 Exams.
     * @param cohort all the Year 12 students.
     */
    public void allocateStudents(ExamList exams, StudentList cohort) {
        int nextDesk = 1;

        // Get all students for this session, sorted by family name
        List<Student> allSessionStudents = new ArrayList<>();

        for (Exam exam : this.exams) {
            for (Student student : cohort.all()) {
                if (student.isAara() == this.venue.isAara()) {
                    for (Subject subject : student.getSubjectsList()) {
                        if (subject.equals(exam.getSubject())) {
                            if (!allSessionStudents.contains(student)) {
                                allSessionStudents.add(student);
                            }
                            break;
                        }
                    }
                }
            }
        }

        // Sort students alphabetically by surname
        allSessionStudents.sort(Comparator.comparing(Student::familyName));

        int totalStudents = allSessionStudents.size();

        if (totalStudents <= totalDesks) {
            // Calculate gaps and skip columns if needed
            int gaps = 0;
            boolean skipColumns = false;
            if (totalStudents < (totalDesks / 2)) {
                skipColumns = true;
                gaps = (totalDesks / 2) - totalStudents;
            } else {
                skipColumns = false;
                gaps = totalDesks - totalStudents;
            }

            // Allocate each student to a desk
            for (Student student : allSessionStudents) {
                // Calculate desk position
                int j = (nextDesk - 1) / rows;
                int i = (nextDesk - 1) % rows;

                if (i < rows && j < columns) {
                    String givenAndInit = getGivenAndInit(student.givenNames());
                    desks[i][j].setFamilyName(student.familyName());
                    desks[i][j].setGivenAndInit(givenAndInit);
                }

                if (skipColumns && nextDesk % rows == 0) {
                    nextDesk += rows;
                }
                nextDesk++;
            }
        }
    }

    private String getGivenAndInit(String given) {
        if (given != null && !given.isEmpty()) {
            String[] names = given.split(" ");
            if (names.length > 1) {
                return names[0] + " " + names[1].substring(0, 1) + ".";
            } else {
                return names[0];
            }
        } else {
            return "";
        }
    }

    /**
     * Prints the layout of the desks in this session in the venue.
     * Prints a grid of the deskNumber, family name, and given name and initial for each desk.
     */
    public void printDesks() {
        StringBuilder sb = new StringBuilder();
        printDesks(sb);
        System.out.print(sb.toString());
    }

    /**
     * Prints the layout of the desks in this session to a StringBuilder.
     *
     * @param sb the StringBuilder to append to
     */
    public void printDesks(StringBuilder sb) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                sb.append(String.format("%-15s", "Desk " + desks[i][j].deskNumber()));
            }
            sb.append("\n");
            for (int j = 0; j < columns; j++) {
                if (desks[i][j].deskFamilyName() == null) {
                    sb.append(String.format("%-15s", ""));
                } else {
                    sb.append(String.format("%-15s", desks[i][j].deskFamilyName()));
                }
            }
            sb.append("\n");
            for (int j = 0; j < columns; j++) {
                if (desks[i][j].deskGivenAndInit() == null) {
                    sb.append(String.format("%-15s", ""));
                } else {
                    sb.append(String.format("%-15s", desks[i][j].deskGivenAndInit()));
                }
            }
            sb.append("\n\n");
        }
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // Write session header
        bw.write(nthItem + ". Venue: " + venue.venueId() +
                ", Session Number: " + sessionNumber +
                ", Day: " + day +
                ", Start: " + start +
                ", Exams: " + exams.size() + System.lineSeparator());

        // Write exam details
        for (Exam exam : exams) {
            int studentCount = examStudentCounts.getOrDefault(exam, 0);
            bw.write("    " + exam.getSubject().getTitle() +
                    " (" + studentCount + " students)" + System.lineSeparator());

            // If finalized, write desk allocations
            if (studentCount > 0) {
                bw.write("    [Desks: " + studentCount + "]" + System.lineSeparator());
                // TODO: Write actual desk allocations if needed
            }
        }
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        throw new UnsupportedOperationException("Use constructor instead");
    }

    @Override
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString()).append("\n");
        sb.append("Exams: ").append(exams.size()).append("\n");
        for (Exam exam : exams) {
            sb.append("  - ").append(exam.getSubject().getTitle());
            int count = examStudentCounts.getOrDefault(exam, 0);
            if (count > 0) {
                sb.append(" (").append(count).append(" students)");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getId() {
        return venue.venueId() + "_" + sessionNumber + "_" + day + "_" + start;
    }

    /**
     * Returns a string representation of the session's state
     *
     * @return a string representation of the session's state
     */
    @Override
    public String toString() {
        return this.venue.venueId()
                + ": "
                + this.sessionNumber
                + ": "
                + this.day.toString()
                + " "
                + this.start.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return sessionNumber == session.sessionNumber &&
                venue.equals(session.venue) &&
                day.equals(session.day) &&
                start.equals(session.start);
    }

    @Override
    public int hashCode() {
        int result = venue.hashCode();
        result = 31 * result + sessionNumber;
        result = 31 * result + day.hashCode();
        result = 31 * result + start.hashCode();
        return result;
    }
}