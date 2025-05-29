package examblock.model;

import java.io.BufferedReader;
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
 * An exam "session" is a block of time in a particular {@link Venue}.
 * Sessions are numbered from 1 and unique in each venue, but not across venues.
 * Session number can be, but do not have to be, in chronological order of session start times.
 * That is, a new session may be inserted earlier into an existing schedule.
 * Session numbers do not have to necessarily be sequential.
 */
public class Session {

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
    private int rows; // the number of rows of Desks, running across the room left to right.
    /** The number of columns of desks set up for this session. */
    private int columns; // an optional third (maximum) room object involved in the venue.
    /** The total number of desks available for this session. */
    private int totalDesks; // the total available Desks (may be less than rows x columns).
    /** The 2D array (row x column) of all desks available for this session. */
    private Desk[][] desks;  // 2D array for desk matrix

    /**
     * Constructs a new empty {@link Exam} {@code Session} for a particular {@link Venue}.
     * The calling process must check that the supplied session number is unique for this venue.
     * Session numbers do not have to be sequential, only unique.
     * The constructor must also prepare the empty (unassigned as yet) desks that will be
     * used in this session. (The session has the same rows and columns of desks as the venue.)
     *
     * @param venue the exam venue for the new session.
     * @param sessionNumber the number (unique by venue) of the new session.
     * @param day the session date.
     * @param start the start time of the exam window.
     */
    public Session(Venue venue, int sessionNumber, LocalDate day, LocalTime start) {
        this.venue = venue;
        this.sessionNumber = sessionNumber;
        this.day = day;
        this.start = start;
        this.exams = new ArrayList<>();
        this.examStudentCounts = new HashMap<>();
        rows = venue.getRows();
        columns = venue.getColumns();
        totalDesks = venue.deskCount();
        desks = new Desk[rows][columns]; // Initialize 2D array
        initializeDesks();  // Fill matrix with Desk objects
    }

    public Session(BufferedReader br, Registry registry, int nthItem) throws IOException {
        String line = CSSE7023.getLine(br);
        if (line == null) throw new RuntimeException("EOF reading Session #" + nthItem);
        String[] idxAndRest = line.split("\\. ", 2);
        if (idxAndRest.length != 2) throw new RuntimeException("Session header format error: " + line);
        int idx = CSSE7023.toInt(idxAndRest[0], "Number format exception parsing Session " + nthItem + " header");
        if (idx != nthItem) throw new RuntimeException("Session index out of sync!");

        String[] parts = idxAndRest[1].split(": ");
        if (parts.length < 4) throw new RuntimeException("Session data format error: " + idxAndRest[1]);
        String venueId = parts[0];
        int sessionNumber = Integer.parseInt(parts[1]);
        LocalDate day = LocalDate.parse(parts[2]);
        LocalTime start = LocalTime.parse(parts[3]);
        Venue venue = registry.find(venueId, Venue.class);
        if (venue == null) throw new RuntimeException("Venue not found: " + venueId);

        this.venue = venue;
        this.sessionNumber = sessionNumber;
        this.day = day;
        this.start = start;
        this.exams = new ArrayList<>();
        this.examStudentCounts = new HashMap<>();
        rows = venue.getRows();
        columns = venue.getColumns();
        totalDesks = venue.deskCount();
        desks = new Desk[rows][columns];
        initializeDesks();
    }


    private void initializeDesks() {
        int deskId = 1;  // Unique ID for each desk
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
     * Allocates an exam to this session (Venue and time).
     *
     * @param exam the exam to be allocated to this venue.
     * @param numberStudents the number of students being added with this allocation.
     */
    public void scheduleExam(Exam exam, int numberStudents) {
        // Add exam if not already present
        if (!exams.contains(exam)) {
            exams.add(exam);
        }

        // Store or update the student count for this exam
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
                            // Check if student is already in the list (taking multiple exams)
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

    /**
     * Allocate students to the provided desk array.
     * This method is used by the report generation.
     */
    public void allocateToDesks(Desk[][] providedDesks) {
        // Copy the allocations from our internal desks to the provided desks
        for (int i = 0; i < rows && i < providedDesks.length; i++) {
            for (int j = 0; j < columns && j < providedDesks[i].length; j++) {
                if (desks[i][j].deskFamilyName() != null) {
                    providedDesks[i][j].setFamilyName(desks[i][j].deskFamilyName());
                    providedDesks[i][j].setGivenAndInit(desks[i][j].deskGivenAndInit());
                }
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
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.printf("%-15s", "Desk " + desks[i][j].deskNumber());
            }
            System.out.println();
            for (int j = 0; j < columns; j++) {
                // print any nulls as empty strings, not a null
                if (desks[i][j].deskFamilyName() == null) {
                    System.out.printf("%-15s", "");
                } else {
                    System.out.printf("%-15s", desks[i][j].deskFamilyName());
                }
            }
            System.out.println();
            for (int j = 0; j < columns; j++) {
                // print any nulls as empty strings, not a null
                if (desks[i][j].deskGivenAndInit() == null) {
                    System.out.printf("%-15s", "");
                } else {
                    System.out.printf("%-15s", desks[i][j].deskGivenAndInit());
                }
            }
            System.out.println();
            System.out.println();
        }
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
}