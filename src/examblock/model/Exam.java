package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * An object describing a single Year 12 Exam.
 */
public class Exam implements StreamManager, ManageableListItem {

    /**
     * An enum for the ExamType (INTERNAL or EXTERNAL).
     */
    public enum ExamType {
        /** Internal assessment, conducted by the school. */
        INTERNAL,
        /** External assessment, conducted by the QCAA. */
        EXTERNAL
    }

    /** The Subject this exam is for. */
    private Subject subject;

    /** The type of exam being conducted INTERNAL or EXTERNAL. */
    private ExamType examType;

    /** An optional paper number (null or 1 or 2) for this exam. */
    private Character paper;

    /** An optional subtitle e.g. "Technology Free" for this exam. */
    private String subtitle;

    /** An optional unit ID if only one unit is applicable for this exam. */
    private Character unit;

    /** The date of this exam. */
    private LocalDate examDate;

    /** The start time for this exam. */
    private LocalTime examTime;

    /** Store the registry passed via constructor */
    private Registry registry;

    /**
     * Constructs an Exam with minimal details.
     *
     * @param subject the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param day the integer day of the date of the exam, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of the exam, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of the exam,
     *             which must be 2025 or greater.
     * @param hour the 2-digit integer hour of the start of the exam window,
     *             in 24-hour time, which must be between 7 and 17.
     * @param minute the integer minute of the start of the exam window,
     *               which must be between 0 - 59 inclusive.
     * @param registry the global registry, where we query and register new list objects
     */
    public Exam(Subject subject, ExamType examType,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this(subject, examType, '\0', "", '\0', day, month, year, hour, minute, registry);
    }

    /**
     * Constructs an Exam with the optional unit specified.
     *
     * @param subject the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param unit an optional unit ID if only one unit is applicable.
     * @param day the integer day of the date of the exam, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of the exam, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of the exam,
     *             which must be 2025 or greater.
     * @param hour the 2-digit integer hour of the start of the exam window,
     *             in 24-hour time, which must be between 7 and 17.
     * @param minute the integer minute of the start of the exam window,
     *               which must be between 0 - 59 inclusive.
     * @param registry the global registry, where we query and register new list objects
     */
    public Exam(Subject subject, ExamType examType, Character unit,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this(subject, examType, '\0', "", unit, day, month, year, hour, minute, registry);
    }

    /**
     * Constructs an Exam with paper number and subtitle but no unit specified.
     *
     * @param subject the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param paper an optional paper number (null or 1 or 2).
     * @param subtitle an optional subtitle e.g. "Technology Free".
     * @param day the integer day of the date of the exam, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of the exam, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of the exam,
     *             which must be 2025 or greater.
     * @param hour the 2-digit integer hour of the start of the exam window,
     *             in 24-hour time, which must be between 7 and 17.
     * @param minute the integer minute of the start of the exam window,
     *               which must be between 0 - 59 inclusive.
     * @param registry the global registry, where we query and register new list objects
     */
    public Exam(Subject subject, ExamType examType, Character paper, String subtitle,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this(subject, examType, paper, subtitle, '\0', day, month, year, hour, minute, registry);
    }

    /**
     * Constructs an Exam with all optional details provided.
     *[2] Registry pattern for centralized object storage and retrieval.
     *
     * @param subject the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param paper an optional paper number (null or 1 or 2).
     * @param subtitle an optional subtitle e.g. "Technology Free".
     * @param unit an optional unit ID if only one unit is applicable.
     * @param day the integer day of the date of the exam, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of the exam, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of the exam,
     *             which must be 2025 or greater.
     * @param hour the 2-digit integer hour of the start of the exam window,
     *             in 24-hour time, which must be between 7 and 17.
     * @param minute the integer minute of the start of the exam window,
     *               which must be between 0 - 59 inclusive.
     * @param registry the global registry, where we register new exams
     */
    public Exam(Subject subject, ExamType examType, Character paper, String subtitle,
                Character unit, int day, int month, int year,
                int hour, int minute, Registry registry) {
        this.subject = subject;
        this.examType = examType;
        this.paper = paper;
        this.subtitle = subtitle;
        this.unit = unit;
        examDate = LocalDate.of(year, month, day);
        examTime = LocalTime.of(hour, minute);
        this.registry = registry;

        registry.add(this, Exam.class);
    }

    /**
     * Constructs an Exam by reading a description from a text stream
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global registry, where we query and register new list objects
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Exam(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.subject = null;
        this.examType = null;
        this.paper = '\0';
        this.subtitle = "";
        this.unit = '\0';
        this.examDate = null;
        this.examTime = null;
        this.registry = registry;

        streamIn(br, registry, nthItem);
        registry.add(this, Exam.class);
    }

    /**
     * Used to write data to the disk.
     * The format of the text written to the stream must be matched exactly by streamIn,
     * so it is very important to format the output as described.
     *
     * IF THERE IS NO PAPER AND SUBTITLE:
     * 4. Year 12 Internal Assessment General Mathematics
     * Subject: General Mathematics, Exam Type: INTERNAL, Unit: 3, Exam Date: 2025-03-11 08:30
     *
     * IF THERE IS A PAPER AND SUBTITLE:
     * 5. Year 12 Internal Assessment Mathematical Methods Paper 1
     * Subject: Mathematical Methods, Exam Type: INTERNAL, Paper: 1, Subtitle: Technology Free,
     * Unit: 3, Exam Date: 2025-03-11 12:30
     *
     * @param bw writer, already opened. Your data should be written at the current file position
     * @param nthItem a number representing this item's position in the stream.
     *                Used for sanity checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + getShortTitle() + System.lineSeparator());

        StringBuilder detail = new StringBuilder();
        detail.append("Subject: ").append(subject.getTitle());
        detail.append(", Exam Type: ").append(examType);

        if (paper != '\0') {
            detail.append(", Paper: ").append(paper);
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            detail.append(", Subtitle: ").append(subtitle);
        }
        if (unit != '\0') {
            detail.append(", Unit: ").append(unit);
        }
        detail.append(", Exam Date: ")
                .append(examDate)
                .append(" ")
                .append(examTime);

        bw.write(detail.toString() + System.lineSeparator());
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.
     *
     * For the format of the text in the input stream, refer to the StreamOut documentation.
     *
     * @param br reader, already opened
     * @param registry the global object registry
     * @param nthItem a number representing this item's position in the stream.
     *                Used for sanity checks
     * @throws IOException on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        String line1 = CSSE7023.getLine(br);
        if (line1 == null) {
            throw new RuntimeException("EOF reading Exam #" + nthItem);
        }
        String[] parts = line1.split("\\. ", 2);
        int index = CSSE7023.toInt(parts[0], "Invalid exam index");
        if (index != nthItem) {
            throw new RuntimeException("Exam index mismatch");
        }

        String line2 = CSSE7023.getLine(br);
        if (line2 == null) {
            throw new RuntimeException("EOF reading Exam details");
        }

        String[] tokens = line2.split(", ");
        for (String token : tokens) {
            String[] kv = CSSE7023.keyValuePair(token);
            if (kv == null || kv.length != 2) {
                continue;
            }

            switch (kv[0]) {
                case "Subject":
                    this.subject = registry.find(kv[1], Subject.class);
                    if (this.subject == null) {
                        for (Subject s : registry.getAll(Subject.class)) {
                            if (s.getTitle().equals(kv[1])) {
                                this.subject = s;
                                break;
                            }
                        }
                    }
                    if (this.subject == null) {
                        System.err.println("ERROR: Subject '" + kv[1] + "' not found for exam!");
                        System.out.println("Available subjects in registry:");
                        for (Subject s : registry.getAll(Subject.class)) {
                            System.out.println("  - ID: '" + s.getId()
                                    + "', Title: '" + s.getTitle() + "'");
                        }
                        this.subject = new Subject(kv[1], "Auto-generated for exam.", registry);
                    }
                    break;
                case "Exam Type":
                    this.examType = ExamType.valueOf(kv[1]);
                    break;
                case "Paper":
                    this.paper = kv[1].charAt(0);
                    break;
                case "Subtitle":
                    this.subtitle = kv[1];
                    break;
                case "Unit":
                    this.unit = kv[1].charAt(0);
                    break;
                case "Exam Date":
                    String[] dateTime = kv[1].split(" ");
                    this.examDate = CSSE7023.toLocalDate(dateTime[0], "Invalid date");
                    this.examTime = CSSE7023.toLocalTime(dateTime[1], "Invalid time");
                    break;
            }
        }
        if (this.subtitle == null) {
            this.subtitle = "";
        }
    }

    /**
     * Returns a detailed string representation of this exam.
     * The output needs to include the subject, exam type, paper (if applicable),
     * subtitle (if applicable), the unit (if applicable), and the exam date and time.
     *
     * @return a detailed string representation of this exam.
     */
    public String getFullDetail() {
        return this.getSubject().toString().toUpperCase() + "\n" + this.getTitle();
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return array of objects representing one row of data
     */
    public Object[] toTableRow() {
        return new Object[]{getShortTitle(), getDate(), getTime()};
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return an array of objects suitable for insertion into a JTable
     */
    public Object[] toLongTableRow() {
        return new Object[]{getShortTitle(), subject.toString(),
                examType.toString(), getDate(), getTime()};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    public String getId() {
        return this.toString();
    }

    /**
     * Gets the subject of the exam.
     *
     * @return subject of the exam
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Gets the full title of the exam.
     * Provides the exam type,
     * and then on a new line, the exam subject, and any paper identifier (if more than one),
     * and then on a new line, any subtitle (only if present).
     *
     * @return the full text title of the exam (type\n, subject, paper\n, subtitle)
     */
    public String getTitle() {
        StringBuilder title = new StringBuilder();
        title.append("Year 12 ");
        if (examType == ExamType.EXTERNAL) {
            title.append("External ");
        } else {
            title.append("Internal ");
        }
        title.append("Assessment\n");
        title.append(subject.getTitle());
        if (paper != '\0') {
            title.append(" Paper " + paper);
        }
        if (subtitle != "") {
            title.append("\n" + subtitle);
        }
        title.append("\n");
        return title.toString();
    }

    /**
     * Gets the short title of the exam with no subtitle.
     * Provides type, subject, and any paper identifier (if more than one), all on one line.
     *
     * @return text title of the exam (type, subject, paper)
     */
    public String getShortTitle() {
        StringBuilder title = new StringBuilder();
        title.append("Year 12 ");
        if (examType == ExamType.EXTERNAL) {
            title.append("External ");
        } else {
            title.append("Internal ");
        }
        title.append("Assessment ");
        title.append(subject.getTitle());
        if (paper != '\0') {
            title.append(" Paper " + paper);
        }
        return title.toString();
    }

    /**
     * Gets the date of this exam.
     *
     * @return the date of this exam.
     */
    public LocalDate getDate() {
        return examDate;
    }

    /**
     * Gets the start time of this exam's window.
     *
     * @return the start time of this exam's window.
     */
    public LocalTime getTime() {
        return examTime;
    }

    /**
     * Returns an exam title of just the title plus paper if applicable
     *
     * @return the title plus paper if applicable, like "Mathematical Methods Paper 1"
     */
    public String abbrevShortTitle() {
        StringBuilder title = new StringBuilder();
        title.append(subject.getTitle());
        if (paper != '\0') {
            title.append(" Paper " + paper);
        }
        return title.toString();
    }

    /**
     * Returns a brief string representation of the exam.
     *
     * @return a brief string representation of the exam.
     */
    @Override
    public String toString() {
        return this.getShortTitle();
    }

    /**
     * class specific equals method
     *[1] Consistent equals() and hashCode() methods for object comparison.
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
        Exam other = (Exam) o;
        return subject.equals(other.subject)
                && examType == other.examType
                && ((paper == null && other.paper == null)
                || (paper != null && paper.equals(other.paper)))
                && subtitle.equals(other.subtitle)
                && ((unit == null && other.unit == null)
                || (unit != null && unit.equals(other.unit)))
                && examDate.equals(other.examDate)
                && examTime.equals(other.examTime);
    }

    /**
     * return the hash value of this object
     *[1] Overriding equals() and hashCode() following best practices.
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + examType.hashCode();
        result = 31 * result + (paper != null ? paper.hashCode() : 0);
        result = 31 * result + subtitle.hashCode();
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + examDate.hashCode();
        result = 31 * result + examTime.hashCode();
        return result;
    }
}