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
 * An exam "session" is a block of time in a particular Venue.
 * Sessions are numbered from 1 and unique in each venue, but not across venues.
 * Session number can be, but do not have to be, in chronological order of session start times.
 * That is, a new session may be inserted earlier into an existing schedule.
 * Session numbers do not have to necessarily be sequential.
 */
public class Session implements StreamManager, ManageableListItem {

    /** The venue for this session. */
    private Venue venue;

    /** The session number of this session in this venue. */
    private int sessionNumber;

    /** The day this session is occurring. */
    private LocalDate day;

    /** The start time of this session. */
    private LocalTime start;

    /** The list of exams in this session in this venue. */
    private ExamList exams;

    /** The number of rows of desks set up for this session. */
    private int rows;

    /** The number of columns of desks set up for this session. */
    private int columns;

    /** The total number of desks available for this session. */
    private int totalDesks;

    /** The 2D array (row x column) of all desks available for this session. */
    public Desk[][] desks;

    /** The registry reference. */
    private Registry registry;

    /**
     * Constructs a new empty Exam Session for a particular Venue with registry.
     *
     * @param venue the exam venue for the new session.
     * @param sessionNumber the number (unique by venue) of the new session.
     * @param day the session date.
     * @param start the start time of the exam window.
     * @param registry the global registry.
     */
    public Session(Venue venue, int sessionNumber, LocalDate day,
                   LocalTime start, Registry registry) {

        this.venue = venue;
        this.sessionNumber = sessionNumber;
        this.day = day;
        this.start = start;
        this.exams = new ExamList(registry);
        this.registry = registry;

        if (venue != null) {
            rows = venue.getRows();
            columns = venue.getColumns();
            totalDesks = venue.deskCount();
            desks = new Desk[rows][columns];

            if (desks != null && rows > 0 && columns > 0) {
                int deskId = 1;
                for (int j = 0; j < columns; j++) {
                    for (int i = 0; i < rows; i++) {
                        desks[i][j] = new Desk(deskId++);
                    }
                }
            }
        } else {
            rows = 0;
            columns = 0;
            totalDesks = 0;
            desks = null;
        }

        if (registry != null) {
            registry.add(this, Session.class);
        }
    }

    /**
     * Constructs a Session by reading a description from a text stream
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual references
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Session(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {

        this.exams = new ExamList(registry);
        this.registry = registry;

        streamIn(br, registry, nthItem);

        if (registry != null) {
            registry.add(this, Session.class);
        }
    }

    /**
     * Used to write data to the disk.
     *
     * @param bw writer, already opened. Your data should be written at the current
     *           file position
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". Venue: " + (venue != null ? venue.getId() : "null")
                + ", Session Number: " + sessionNumber
                + ", Day: " + day
                + ", Start: " + start
                + ", Exams: " + (exams != null ? exams.size() : 0)
                + System.lineSeparator());

        if (exams != null && exams.size() > 0) {
            for (Exam exam : exams.all()) {
                bw.write(exam.getShortTitle() + System.lineSeparator());

                // check status and assigned desks
                boolean hasAssignedStudents = false;
                int assignedDeskCount = 0;
                List<Desk> assignedDesks = new ArrayList<>();

                if (desks != null && rows > 0 && columns > 0) {
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < columns; j++) {
                            if (desks[i][j] != null
                                    && desks[i][j].deskFamilyName() != null
                                    && !desks[i][j].deskFamilyName().trim().isEmpty()) {

                                String deskExam = desks[i][j].deskExam();
                                if (deskExam != null && deskExam.equals(exam.getShortTitle())) {
                                    assignedDesks.add(desks[i][j]);
                                    assignedDeskCount++;
                                    hasAssignedStudents = true;
                                }
                            }
                        }
                    }
                }

                if (hasAssignedStudents && assignedDeskCount > 0) {
                    bw.write("[Desks: " + assignedDeskCount + "]" + System.lineSeparator());
                    assignedDesks.sort((d1, d2) ->
                            Integer.compare(d1.deskNumber(), d2.deskNumber()));

                    for (Desk desk : assignedDesks) {
                        StringBuilder deskLine = new StringBuilder();
                        deskLine.append("Desk: ").append(desk.deskNumber());
                        deskLine.append(", LUI: ").append(desk.deskLui());
                        deskLine.append(", Name: ").append(desk.deskFamilyName());

                        if (desk.deskGivenAndInit() != null
                                && !desk.deskGivenAndInit().trim().isEmpty()) {
                            deskLine.append(", ").append(desk.deskGivenAndInit());
                        }

                        bw.write(deskLine.toString() + System.lineSeparator());
                    }
                } else {
                    bw.write("[Desks: 0, Session not finalised]" + System.lineSeparator());
                }
            }
        }
    }

    /**
     * Used to read data from the disk.
     *
     * @param br reader, already opened.
     * @param registry the global object registry
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        String heading = CSSE7023.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Session #" + nthItem);
        }

        String[] bits = heading.split("\\. ", 2);
        if (bits.length != 2) {
            throw new RuntimeException("Invalid session format: " + heading);
        }

        int index = CSSE7023.toInt(bits[0], "Number format exception parsing Session "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Session index out of sync!");
        }

        this.sessionNumber = index;
        if (this.exams == null) {
            this.exams = new ExamList(registry);
        }
        this.venue = null;

        String sessionInfo = bits[1];
        String[] parts = sessionInfo.split(", ");
        int expectedExams = 0;

        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("Venue: ")) {
                String venueId = part.substring("Venue: ".length()).trim();
                if (registry != null) {
                    this.venue = registry.find(venueId, Venue.class);
                }
            } else if (part.startsWith("Session Number: ")) {
                String numberStr = part.substring("Session Number: ".length()).trim();
                this.sessionNumber = Integer.parseInt(numberStr);
            } else if (part.startsWith("Day: ")) {
                String dayStr = part.substring("Day: ".length()).trim();
                this.day = LocalDate.parse(dayStr);
            } else if (part.startsWith("Start: ")) {
                String timeStr = part.substring("Start: ".length()).trim();
                this.start = LocalTime.parse(timeStr);
            } else if (part.startsWith("Exams: ")) {
                String examStr = part.substring("Exams: ".length()).trim();
                expectedExams = Integer.parseInt(examStr);
            }
        }

        if (venue != null) {
            rows = venue.getRows();
            columns = venue.getColumns();
            totalDesks = venue.deskCount();
            desks = new Desk[rows][columns];

            if (rows > 0 && columns > 0) {
                int deskId = 1;
                for (int j = 0; j < columns; j++) {
                    for (int i = 0; i < rows; i++) {
                        desks[i][j] = new Desk(deskId++);
                    }
                }
            }
        } else {
            this.rows = 0;
            this.columns = 0;
            this.totalDesks = 0;
            this.desks = null;
        }

        for (int examIndex = 0; examIndex < expectedExams; examIndex++) {
            String examTitle = CSSE7023.getLine(br);
            if (examTitle != null) {
                examTitle = examTitle.trim();
                Exam currentExam = null;
                if (registry != null) {
                    for (Exam e : registry.getAll(Exam.class)) {
                        if (e.getShortTitle().equals(examTitle)) {
                            currentExam = e;
                            break;
                        }
                    }

                    if (currentExam != null && !this.exams.getItems().contains(currentExam)) {
                        this.exams.getItems().add(currentExam);
                    }
                }

                String deskHeader = CSSE7023.getLine(br);
                if (deskHeader != null && deskHeader.startsWith("[Desks: ")
                        && deskHeader.endsWith("]")) {
                    String deskInfo = deskHeader.substring(8, deskHeader.length() - 1);

                    if (deskInfo.equals("0, Session not finalised")) {
                        // Sessions not finalised
                    } else {
                        try {
                            int deskCount = Integer.parseInt(deskInfo);
                            for (int i = 0; i < deskCount; i++) {
                                String deskLine = CSSE7023.getLine(br);
                                if (deskLine != null && deskLine.startsWith("Desk: ")) {
                                    String[] deskParts = deskLine.split(", ");
                                    if (deskParts.length >= 3) {
                                        try {
                                            int deskNumber = Integer.parseInt(deskParts[0]
                                                    .substring("Desk: ".length()));
                                            long lui = 0;
                                            String familyName = "";
                                            String givenAndInit = "";

                                            for (String part : deskParts) {
                                                if (part.startsWith("LUI: ")) {
                                                    lui = Long.parseLong(part
                                                            .substring("LUI: ".length()));
                                                } else if (part.startsWith("Name: ")) {
                                                    String nameInfo = part.substring("Name: "
                                                            .length());
                                                    if (nameInfo.contains(", ")) {
                                                        String[] nameParts = nameInfo
                                                                .split(", ", 2);
                                                        familyName = nameParts[0].trim();
                                                        if (nameParts.length > 1) {
                                                            givenAndInit = nameParts[1].trim();
                                                        }
                                                    } else {
                                                        familyName = nameInfo.trim();
                                                    }
                                                }
                                            }
                                            if (desks != null && deskNumber > 0
                                                    && deskNumber <= totalDesks) {
                                                int targetRow = -1;
                                                int targetCol = -1;
                                                int currentDeskId = 1;
                                                for (int j = 0; j < columns
                                                        && targetRow == -1; j++) {
                                                    for (int row = 0; row < rows; row++) {
                                                        if (currentDeskId == deskNumber) {
                                                            targetRow = row;
                                                            targetCol = j;
                                                            break;
                                                        }
                                                        currentDeskId++;
                                                    }
                                                }
                                                if (targetRow != -1 && targetCol != -1
                                                        && desks[targetRow][targetCol] != null) {
                                                    Student student = null;
                                                    if (registry != null) {
                                                        for (Student s : registry
                                                                .getAll(Student.class)) {
                                                            if (s.getLui().equals(lui)) {
                                                                student = s;
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    if (student != null) {
                                                        desks[targetRow][targetCol]
                                                                .setStudent(student);
                                                    } else {
                                                        Desk desk = desks[targetRow][targetCol];
                                                        try {
                                                            java.lang.reflect.Field familyField
                                                                    = Desk.class
                                                                    .getDeclaredField("familyName");
                                                            familyField.setAccessible(true);
                                                            familyField.set(desk, familyName);

                                                            java.lang.reflect.Field givenField
                                                                    = Desk.class
                                                                    .getDeclaredField(
                                                                            "givenAndInit");
                                                            givenField.setAccessible(true);
                                                            givenField.set(desk, givenAndInit);

                                                            java.lang.reflect.Field luiField
                                                                    = Desk.class
                                                                    .getDeclaredField("lui");
                                                            luiField.setAccessible(true);
                                                            luiField.set(desk, lui);
                                                        } catch (Exception e) {
                                                            System.out.println("Error setting desk fields: " 
                                                                    + e.getMessage());
                                                        }
                                                    }

                                                    if (currentExam != null) {
                                                        desks[targetRow][targetCol]
                                                                .setExam(currentExam);
                                                    }
                                                }
                                            }
                                        } catch (NumberFormatException e) {
                                            System.out.println("Invalid number");
                                        }
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number");
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns detailed string representations of this session.
     *
     * @return Detailed string representations of this session.
     */
    @Override
    public String getFullDetail() {
        StringBuilder detail = new StringBuilder();
        detail.append("Session: ").append(getId()).append("\n");
        detail.append("Venue: ").append(venue != null ? venue.getId() : "None").append("\n");
        detail.append("Session Number: ").append(sessionNumber).append("\n");
        detail.append("Date: ").append(day).append("\n");
        detail.append("Time: ").append(start).append("\n");
        detail.append("Students: ").append(countStudents()).append("\n");
        detail.append("Exams: ").append(exams != null ? exams.size() : 0).append("\n");
        return detail.toString();
    }

    /**
     * Return a unique string identifying this session
     *
     * @return a unique string identifying this session
     */
    @Override
    public String getId() {
        return (venue != null ? venue.getId() : "null") + "_" + sessionNumber;
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
        return new ArrayList<>(exams.all());
    }

    /**
     * Counts the number of student-exam pairs where
     * the student's AARA status matches the venue's AARA status
     * and the student is enrolled in the subject of an exam.
     * A student may be counted multiple times
     * if they match multiple exams or subjects.
     *
     * @return The number of students already scheduled in this session.
     */
    public int countStudents() {
        if (desks != null && rows > 0 && columns > 0) {
            int assignedCount = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    if (desks[i][j] != null
                            && desks[i][j].deskFamilyName() != null
                            && !desks[i][j].deskFamilyName().trim().isEmpty()) {
                        assignedCount++;
                    }
                }
            }
            if (assignedCount > 0) {
                return assignedCount;
            }
        }

        if (registry == null) {
            return 0;
        }

        int totalStudents = 0;
        List<Exam> sessionExams = this.getExams();
        List<Student> allStudents = registry.getAll(Student.class);

        for (Exam exam : sessionExams) {
            for (Student student : allStudents) {
                if (student.isAara() == this.venue.isAara()) {
                    List<Subject> subjects = student.getSubjects().all();
                    for (Subject subject : subjects) {
                        if (subject.equals(exam.getSubject())) {
                            totalStudents++;
                            break;
                        }
                    }
                }
            }
        }
        return totalStudents;
    }

    /**
     * Return the total number of desks available her
     *
     * @return the total number of desks available here
     */
    public int getTotalDesks() {
        return totalDesks;
    }

    /**
     * Allocates an exam to this session (Venue and time).
     *
     * @param exam the exam to be allocated to this venue.
     */
    public void scheduleExam(Exam exam) {
        if (!exams.getItems().contains(exam)) {
            exams.getItems().add(exam);
            System.out.println("Added exam " + exam.getSubject().getTitle() + " to session");
        }
    }

    /**
     * Removes an exam from this session.
     *
     * @param exam the exam to be removed from this venue.
     */
    public void removeExam(Exam exam) {
        exams.remove(exam);
    }

    /**
     * Allocates Students to Desks for every Exam in this Session.
     *
     * @param exams the current set of Year 12 Exams.
     * @param cohort all the Year 12 students.
     */
    public void allocateStudents(ExamList exams, StudentList cohort) {
        int i;
        int j;
        int nextDesk = 1;
        int startDesk = 1;
        int finishDesk = 1;
        int totalStudents = this.countStudents();
        if (totalStudents <= totalDesks) {
            int gaps = 0;
            boolean skipColumns = false;
            if (totalStudents < (totalDesks / 2)) {
                skipColumns = true;
                gaps = (totalDesks / 2) - totalStudents;
            } else {
                skipColumns = false;
                gaps = totalDesks - totalStudents;
            }
            List<Exam> sessionExams = this.getExams();
            List<Student> students = cohort.all();
            students.sort(Comparator.comparing(Student::familyName));
            int countExams = 0;
            for (Exam exam : sessionExams) {
                countExams++;
            }
            int interGaps = 0;
            if (countExams > 1) {
                interGaps = gaps / (countExams - 1);
            }
            Subject subject;
            for (Exam exam : sessionExams) {
                startDesk = nextDesk;
                finishDesk = nextDesk;
                subject = exam.getSubject();
                for (Student student : students) {
                    if (student.isAara() == this.venue.isAara()) {
                        List<Subject> subjects = student.getSubjects().all();
                        for (Subject check : subjects) {
                            if (check == subject) {
                                j = (nextDesk - 1) / rows;
                                i = (nextDesk - 1) % rows;
                                desks[i][j].setStudent(student);
                                desks[i][j].setExam(exam);
                                finishDesk = nextDesk;
                                if (skipColumns) {
                                    if (nextDesk % rows == 0) {
                                        nextDesk += rows;
                                    }
                                }
                                nextDesk++;
                            }
                        }
                    }
                }
                nextDesk += interGaps;
            }
        }
    }

    /**
     * Prints the layout of the desks in this session in the venue.
     * Prints a grid of the deskNumber, family name, and given name and initial for each desk.
     */
    public void printDesks() {
        if (desks != null) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    System.out.printf("%-15s", "Desk " + desks[i][j].deskNumber());
                }
                System.out.println();
                for (int j = 0; j < columns; j++) {
                    if (desks[i][j].deskFamilyName() == null) {
                        System.out.printf("%-15s", "");
                    } else {
                        System.out.printf("%-15s", desks[i][j].deskFamilyName());
                    }
                }
                System.out.println();
                for (int j = 0; j < columns; j++) {
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
    }

    /**
     * Appends the desk allocation to the provided StringBuilder.
     * Displays desks in a grid format optimized for scrolling viewers.
     * @param sb - the StringBuilder to append to
     */
    public void printDesks(StringBuilder sb) {
        if (desks != null && sb != null) {
            final int deskWidth = 12;

            for (int i = 0; i < rows; i++) {
                StringBuilder deskLine = new StringBuilder();
                StringBuilder familyLine = new StringBuilder();
                StringBuilder givenLine = new StringBuilder();

                for (int j = 0; j < columns; j++) {
                    String deskNum = "";
                    String familyName = "";
                    String givenName = "";

                    if (desks[i][j] != null) {
                        deskNum = "Desk " + desks[i][j].deskNumber();

                        if (desks[i][j].deskFamilyName() != null
                                && !desks[i][j].deskFamilyName().trim().isEmpty()) {
                            familyName = desks[i][j].deskFamilyName();
                        }

                        if (desks[i][j].deskGivenAndInit() != null
                                && !desks[i][j].deskGivenAndInit().trim().isEmpty()) {
                            givenName = desks[i][j].deskGivenAndInit();
                        }
                    }

                    deskLine.append(String.format("%-" + deskWidth + "s", deskNum));
                    familyLine.append(String.format("%-" + deskWidth + "s", familyName));
                    givenLine.append(String.format("%-" + deskWidth + "s", givenName));

                    if (j < columns - 1) {
                        deskLine.append("\t");
                        familyLine.append("\t");
                        givenLine.append("\t");
                    }
                }

                sb.append(deskLine.toString()).append("\n");
                sb.append(familyLine.toString()).append("\n");
                sb.append(givenLine.toString()).append("\n");
                sb.append("\n");
            }
        }
    }

    /**
     * Returns a string representation of the session's state
     *
     * @return a string representation of the session's state
     */
    @Override
    public String toString() {
        return (venue != null ? venue.venueId() : "null")
                + ": "
                + this.sessionNumber
                + ": "
                + this.day.toString()
                + " "
                + this.start.toString();
    }

    /**
     * class specific equals method
     *
     * @param o the other object
     * @return true if they match, field for field, otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Session other = (Session) o;
        return sessionNumber == other.sessionNumber
                && venue != null && venue.equals(other.venue)
                && day != null && day.equals(other.day)
                && start != null && start.equals(other.start);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        int result = sessionNumber;
        result = 31 * result + (venue != null ? venue.hashCode() : 0);
        result = 31 * result + (day != null ? day.hashCode() : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        return result;
    }
}