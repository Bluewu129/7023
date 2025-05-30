package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An object describing a single Exam Session.
 * An exam "session" is a block of time in a particular Venue, with zero or more Exams.
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
    /** The number of rows of desks set up for this session. */
    private int rows;
    /** The number of columns of desks set up for this session. */
    private int columns;
    /** The total number of desks available for this session. */
    private int totalDesks;
    /** The 2D array (row x column) of all desks available for this session. */
    public Desk[][] desks;
    /** The registry for dependencies. */
    private Registry registry;

    /**
     * Constructs a new empty Exam Session for a particular Venue.
     * Fixed parameter order: Registry, Venue, int, LocalDate, LocalTime
     */
    public Session(Venue venue, int sessionNumber, LocalDate day, LocalTime start, Registry registry) {
        this.venue = venue;
        this.sessionNumber = sessionNumber;
        this.day = day;
        this.start = start;
        this.exams = new ArrayList<>();
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
     * Fixed parameter order: Registry, BufferedReader, int
     */
    public Session(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;
        this.exams = new ArrayList<>();

        String line = CSSE7023.getLine(br);
        if (line == null) throw new RuntimeException("EOF reading Session #" + nthItem);

        String[] idxAndRest = line.split("\\. ", 2);
        if (idxAndRest.length != 2) throw new RuntimeException("Session header format error: " + line);

        int idx = CSSE7023.toInt(idxAndRest[0], "Number format exception parsing Session " + nthItem + " header");
        if (idx != nthItem) throw new RuntimeException("Session index out of sync!");

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

                String examTitle = examLine;
                int startParen = examLine.lastIndexOf('(');
                if (startParen > 0) {
                    examTitle = examLine.substring(0, startParen).trim();
                }

                Exam exam = null;
                List<Exam> allExams = registry.getAll(Exam.class);
                for (Exam e : allExams) {
                    if (e.getSubject().getTitle().equals(examTitle)) {
                        exam = e;
                        break;
                    }
                }

                if (exam != null) {
                    String nextLine = CSSE7023.getLine(br, true);
                    if (nextLine != null && nextLine.trim().startsWith("[Desks:")) {
                        CSSE7023.getLine(br);

                        String deskLine;
                        while ((deskLine = CSSE7023.getLine(br, true)) != null) {
                            if (deskLine.trim().isEmpty() ||
                                    (!deskLine.contains("Desk") && !deskLine.contains(",") && !deskLine.contains("."))) {
                                break;
                            }
                            CSSE7023.getLine(br);
                        }
                    }

                    scheduleExam(exam);
                }
            }
        }

        if (registry != null) {
            registry.add(this, Session.class);
        }
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
     */
    public Venue getVenue() {
        return venue;
    }

    /**
     * Gets the sessionNumber of this session.
     */
    public int getSessionNumber() {
        return sessionNumber;
    }

    /**
     * Gets the date of this session.
     */
    public LocalDate getDate() {
        return day;
    }

    /**
     * Gets the start time of this session.
     */
    public LocalTime getTime() {
        return start;
    }

    /**
     * Gets the total number of desks in this session.
     */
    public int getTotalDesks() {
        return totalDesks;
    }

    /**
     * Gets the list of exams being held in this session.
     */
    public List<Exam> getExams() {
        return new ArrayList<>(exams);
    }

    /**
     * Counts the number of students already scheduled in this Session.
     */
    public int countStudents() {
        if (registry == null) {
            return 0;
        }

        int total = 0;
        List<Student> allStudents = registry.getAll(Student.class);

        for (Student student : allStudents) {
            if (student.isAara() == venue.isAara()) {
                boolean takesAnyExam = false;
                for (Exam exam : exams) {
                    for (Subject subject : student.getSubjects().all()) {
                        if (subject.equals(exam.getSubject())) {
                            takesAnyExam = true;
                            break;
                        }
                    }
                    if (takesAnyExam) break;
                }
                if (takesAnyExam) {
                    total++;
                }
            }
        }

        return total;
    }

    /**
     * Removes an exam from this session.
     */
    public void removeExam(Exam exam) {
        exams.remove(exam);
    }

    /**
     * Allocates an exam to this session (Venue and time).
     * Added int parameter as required by specification.
     */
    public void scheduleExam(Exam exam) {
        if (!exams.contains(exam)) {
            exams.add(exam);
        }
    }

    /**
     * Allocates Students to Desks for every Exam in this Session.
     */
    public void allocateStudents(ExamList exams, StudentList cohort) {
        int nextDesk = 1;

        List<Student> allSessionStudents = new ArrayList<>();

        for (Exam exam : this.exams) {
            for (Student student : cohort.all()) {
                if (student.isAara() == this.venue.isAara()) {
                    for (Subject subject : student.getSubjects().all()) {
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

        allSessionStudents.sort(Comparator.comparing(Student::familyName));

        int totalStudents = allSessionStudents.size();

        if (totalStudents <= totalDesks) {
            boolean skipColumns = totalStudents < (totalDesks / 2);

            for (Student student : allSessionStudents) {
                int j = (nextDesk - 1) / rows;
                int i = (nextDesk - 1) % rows;

                if (i < rows && j < columns) {
                    String givenAndInit = getGivenAndInit(student.givenNames());
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
        }
        return "";
    }

    /**
     * Prints the layout of the desks in this session in the venue.
     */
    public void printDesks() {
        StringBuilder sb = new StringBuilder();
        printDesks(sb);
        System.out.print(sb.toString());
    }

    /**
     * Prints the layout of the desks in this session to a StringBuilder.
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
        bw.write(nthItem + ". Venue: " + venue.venueId() +
                ", Session Number: " + sessionNumber +
                ", Day: " + day +
                ", Start: " + start +
                ", Exams: " + exams.size() + System.lineSeparator());

        for (Exam exam : exams) {
            bw.write("    " + exam.getSubject().getTitle() +
                    " (0 students)" + System.lineSeparator());
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
            sb.append("  - ").append(exam.getSubject().getTitle()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getId() {
        return venue.venueId() + "_" + sessionNumber + "_" + day + "_" + start;
    }

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