package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * An object describing a single Year 12 Exam.
 * Updated to implement StreamManager and ManageableListItem interfaces.
 */
public class Exam implements StreamManager, ManageableListItem {

    /**
     * An enum for the ExamType (INTERNAL or EXTERNAL).
     */
    public enum ExamType {
        /**
         * Internal assessment, conducted by the school.
         */
        INTERNAL,
        /**
         * External assessment, conducted by the QCAA.
         */
        EXTERNAL;
    }

    /**
     * The Subject this exam is for.
     */
    private Subject subject;
    /**
     * The type of exam being conducted INTERNAL or EXTERNAL.
     */
    private ExamType examType;
    /**
     * An optional paper number (null or 1 or 2) for this exam.
     */
    private Character paper;
    /**
     * An optional subtitle e.g. "Technology Free" for this exam.
     */
    private String subtitle;
    /**
     * An optional unit ID if only one unit is applicable for this exam.
     */
    private Character unit;
    /**
     * The date of this exam.
     */
    private LocalDate examDate;
    /**
     * The start time for this exam.
     */
    private LocalTime examTime;
    /**
     * The registry for dependencies.
     */
    private Registry registry;

    /**
     * Constructs an {@code Exam} with the subject and exam type specified.
     * As per specification - Registry is last parameter
     *
     * @param subject  the subject of the exam
     * @param examType the type of the exam (INTERNAL or EXTERNAL)
     * @param day      the day of the exam
     * @param month    the month of the exam
     * @param year     the year of the exam
     * @param hour     the starting hour of the exam
     * @param minute   the starting minute of the exam
     * @param registry the registry to add this exam to
     */
    public Exam(Subject subject, ExamType examType,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this.subject = subject;
        this.examType = examType;
        this.paper = '\0';
        this.subtitle = "";
        this.unit = '\0';
        this.examDate = LocalDate.of(year, month, day);
        this.examTime = LocalTime.of(hour, minute);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Exam.class);
        }
    }

    /**
     * Constructs an {@code Exam} with unit specified.
     * As per specification - Registry is last parameter
     *
     * @param subject  the subject of the exam
     * @param examType the type of the exam (INTERNAL or EXTERNAL)
     * @param unit     the unit ID if applicable
     * @param day      the day of the exam
     * @param month    the month of the exam
     * @param year     the year of the exam
     * @param hour     the starting hour of the exam
     * @param minute   the starting minute of the exam
     * @param registry the registry to add this exam to
     */
    public Exam(Subject subject, ExamType examType, Character unit,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this.subject = subject;
        this.examType = examType;
        this.paper = '\0';
        this.subtitle = "";
        this.unit = unit;
        this.examDate = LocalDate.of(year, month, day);
        this.examTime = LocalTime.of(hour, minute);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Exam.class);
        }
    }

    /**
     * Constructs an {@code Exam} with unit specified.
     * As per specification - Registry is last parameter
     *
     * @param subject  the subject of the exam
     * @param examType the type of the exam (INTERNAL or EXTERNAL)
     * @param day      the day of the exam
     * @param month    the month of the exam
     * @param year     the year of the exam
     * @param hour     the starting hour of the exam
     * @param minute   the starting minute of the exam
     * @param registry the registry to add this exam to
     */
    public Exam(Registry registry ,ExamType examType, Subject subject,
                int day, int month, int year, int hour, int minute ) {
        this.subject = subject;
        this.examType = examType;
        this.paper = '\0';
        this.subtitle = "";
        this.examDate = LocalDate.of(year, month, day);
        this.examTime = LocalTime.of(hour, minute);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Exam.class);
        }
    }

    /**
     * Constructs an {@code Exam} with paper number and subtitle but no unit specified.
     * As per specification - Registry is last parameter
     *
     * @param subject  the subject of the exam
     * @param examType the type of the exam (INTERNAL or EXTERNAL)
     * @param paper    the paper number (null or 1 or 2) for this exam
     * @param subtitle an optional subtitle e.g. "Technology Free" for this exam
     * @param day      the day of the exam
     * @param month    the month of the exam
     * @param year     the year of the exam
     * @param hour     the starting hour of the exam
     * @param minute   the starting minute of the exam
     * @param registry the registry to add this exam to
     */
    public Exam(Subject subject, ExamType examType, Character paper, String subtitle,
                int day, int month, int year, int hour, int minute, Registry registry) {
        this.subject = subject;
        this.examType = examType;
        this.paper = paper;
        this.subtitle = subtitle;
        this.unit = '\0';
        this.examDate = LocalDate.of(year, month, day);
        this.examTime = LocalTime.of(hour, minute);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Exam.class);
        }
    }

    /**
     * Constructs an {@code Exam} with all optional details provided.
     * As per specification - Registry is last parameter
     *
     * @param subject  the subject of the exam
     * @param examType the type of the exam (INTERNAL or EXTERNAL)
     * @param paper    the paper number (null or 1 or 2) for this exam
     * @param subtitle an optional subtitle e.g. "Technology Free" for this exam
     * @param unit     the unit ID if only one unit is applicable for this exam
     * @param day      the day of the exam
     * @param month    the month of the exam
     * @param year     the year of the exam
     * @param hour     the starting hour of the exam
     * @param minute   the starting minute of the exam
     * @param registry the registry to add this exam to
     */
    public Exam(Subject subject, ExamType examType, Character paper, String subtitle,
                Character unit, int day, int month, int year, int hour,
                int minute, Registry registry) {
        this.subject = subject;
        this.examType = examType;
        this.paper = paper;
        this.subtitle = subtitle;
        this.unit = unit;
        this.examDate = LocalDate.of(year, month, day);
        this.examTime = LocalTime.of(hour, minute);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Exam.class);
        }
    }

    /**
     * Constructs an {@code Exam} by reading from a stream.
     * As per specification
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the registry to add this exam to
     * @param nthItem    the index number of this serialized object
     * @throws IOException
     * @throws RuntimeException
     */
    public Exam(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;

        readExamData(br, nthItem);

        if (registry != null) {
            registry.add(this, Exam.class);
        }

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Exam: " + getShortTitle());
        }
    }

    /**
     * Reads the exam data from the provided BufferedReader.
     */
    private void readExamData(BufferedReader br, int nthItem)
            throws IOException, RuntimeException {
        // Read header line: "1. Year 12 Internal Assessment English"
        String headerLine = CSSE7023.getLine(br);
        if (headerLine == null) {
            throw new RuntimeException("EOF reading Exam #" + nthItem);
        }

        String[] headerParts = headerLine.split("\\. ", 2);
        if (headerParts.length != 2) {
            throw new RuntimeException("Invalid exam header format: " + headerLine);
        }

        int index = CSSE7023.toInt(headerParts[0],
                "Number format exception parsing Exam " + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Exam index out of sync!");
        }

        // Parse the exam details
        String examDetails = headerParts[1];

        // Extract exam type
        if (examDetails.contains("External")) {
            this.examType = ExamType.EXTERNAL;
        } else {
            this.examType = ExamType.INTERNAL;
        }

        // Extract subject name (it's the last part after "Assessment")
        String subjectName = "";
        if (examDetails.contains("Assessment")) {
            int idx = examDetails.indexOf("Assessment") + "Assessment".length();
            subjectName = examDetails.substring(idx).trim();
        }

        // Look for paper info
        this.paper = '\0';
        this.subtitle = "";
        if (subjectName.contains("Paper")) {
            String[] parts = subjectName.split("Paper");
            subjectName = parts[0].trim();
            String paperInfo = parts[1].trim();
            if (paperInfo.length() > 0) {
                this.paper = paperInfo.charAt(0);
                // Look for subtitle in remaining text
                if (paperInfo.length() > 2) {
                    this.subtitle = paperInfo.substring(2).trim();
                }
            }
        }

        // Find the subject
        this.subject = registry.find(subjectName, Subject.class);
        if (this.subject == null) {
            throw new RuntimeException("Subject not found: " + subjectName);
        }

        // Read additional exam info (date, time, etc.)
        String examLine2 = CSSE7023.getLine(br);
        if (examLine2 != null && !examLine2.isEmpty()) {
            // Parse key-value pairs from line like: "Subject: English, Exam Type: INTERNAL, Unit: 3, Exam Date: 2025-03-10, Exam Time: 08:30"
            String[] details = examLine2.split(", ");
            for (String detail : details) {
                String[] kv = detail.split(": ", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String value = kv[1].trim();

                    switch (key) {
                        case "Paper":
                            if (!value.isEmpty() && !value.equals("null")) {
                                this.paper = value.charAt(0);
                            }
                            break;
                        case "Subtitle":
                            this.subtitle = value;
                            break;
                        case "Unit":
                            if (!value.isEmpty() && !value.equals("null")) {
                                this.unit = value.charAt(0);
                            }
                            break;
                        case "Exam Date":
                            // Date and time are together: "2025-03-10 08:30"
                            String[] dateTimeParts = value.split(" ");
                            if (dateTimeParts.length >= 2) {
                                this.examDate = CSSE7023.toLocalDate(dateTimeParts[0], "Invalid date format");
                                this.examTime = CSSE7023.toLocalTime(dateTimeParts[1], "Invalid time format");
                            } else {
                                // Only date provided
                                this.examDate = CSSE7023.toLocalDate(value, "Invalid date format");
                                this.examTime = LocalTime.of(9, 0); // Default time
                            }
                            break;
                    }
                }
            }
        } else {
            // Default date/time if not specified
            this.examDate = LocalDate.of(2025, 3, 10);
            this.examTime = LocalTime.of(9, 0);
        }

        // Initialize unit if not set
        if (this.unit == null) {
            this.unit = '\0';
        }
    }


    /**
     * Writes the exam data to the provided BufferedWriter.
     *
     * @param bw
     * @param nthItem
     * @throws IOException
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // Write header line
        bw.write(nthItem + ". Year 12 ");
        if (examType == ExamType.EXTERNAL) {
            bw.write("External ");
        } else {
            bw.write("Internal ");
        }
        bw.write("Assessment ");
        bw.write(subject.getTitle());

        if (paper != '\0') {
            bw.write(" Paper " + paper);
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            bw.write(" " + subtitle);
        }
        bw.write(System.lineSeparator());

        // Write details line
        bw.write("Subject: " + subject.getTitle());
        bw.write(", Exam Type: " + examType.toString());
        if (paper != '\0') {
            bw.write(", Paper: " + paper);
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            bw.write(", Subtitle: " + subtitle);
        }
        if (unit != '\0') {
            bw.write(", Unit: " + unit);
        }
        bw.write(", Exam Date: " + examDate.toString());
        bw.write(", Exam Time: " + examTime.toString());
        bw.write(System.lineSeparator());
    }

    /**
     * Reads the exam data from the provided BufferedReader.
     *
     * @param br
     * @param registry the registry to add this exam to
     * @param nthItem
     * @throws IOException
     * @throws RuntimeException
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        throw new UnsupportedOperationException("Use constructor instead");
    }

    /**
     * Gets the subject of the exam.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Gets the full title of the exam.
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
        if (subtitle != null && !subtitle.isEmpty()) {
            title.append("\n" + subtitle);
        }
        title.append("\n");
        return title.toString();
    }

    /**
     * Gets the short title of the exam with no subtitle.
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
     * Gets the abbreviated short title of the exam.
     */
    public String abbrevShortTitle() {
        return subject.getTitle() + " " + examType.toString().substring(0, 3);
    }

    /**
     * Gets the date of this exam.
     */
    public LocalDate getDate() {
        return examDate;
    }

    /**
     * Gets the start time of this exam's window.
     */
    public LocalTime getTime() {
        return examTime;
    }

    /**
     * Gets the full details of this exam.
     */
    @Override
    public String getFullDetail() {
        return this.getSubject().toString().toUpperCase() + "\n" + this.getTitle();
    }

    /**
     * Returns an array of objects representing the exam details for table display.
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{
                examType == ExamType.INTERNAL ? "✓" : "",
                subject.getTitle(),
                examDate,
                examTime,
                "" // AARA column - would need student info to populate
        };
    }

    /**
     * Returns a long table row with all exam details.
     */
    @Override
    public Object[] toLongTableRow() {
        return new Object[]{subject.getTitle(), examType.toString(),
                paper != '\0' ? paper.toString() : "", subtitle,
                examDate, examTime};
    }

    @Override
    public String getId() {
        return subject.getTitle() + "_" + examType + "_" + examDate + "_" + examTime;
    }

    @Override
    public String toString() {
        return this.getShortTitle();
    }

    /**
     * Checks if this exam is equal to another object.
     *
     * @param o the object to compare to.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return subject.equals(exam.subject) &&
                examType == exam.examType &&
                examDate.equals(exam.examDate) &&
                examTime.equals(exam.examTime);
    }

    /**
     * Returns the hash code of this exam.
     *
     * @return the hash code of this exam.
     */
    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + examType.hashCode();
        result = 31 * result + examDate.hashCode();
        result = 31 * result + examTime.hashCode();
        return result;
    }
}