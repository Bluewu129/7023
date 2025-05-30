package examblock.model;

import java.time.LocalDate;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An object describing a single Year 12 Student.
 */
public class Student implements StreamManager, ManageableListItem {

    /** The Student's 10-digit Learner Unique Identifier (LUI). */
    private Long lui;
    /** The Student's given name(s). */
    private String given;
    /** The Student's family name. */
    private String family;
    /** The Student's date of birth. */
    private LocalDate dob;
    /** The Student's house colour. */
    private String house;
    /** The Student requires Access Arrangements and Reasonable Adjustments (AARA). */
    private Boolean aara;
    /** The list of the Student's subjects. */
    private final SubjectList subjects;
    /** The list of the Student's current units. */
    private final UnitList units;
    /** The list of the Student's exams for the current exam block. */
    private final ExamList exams;
    /** The registry reference. */
    private Registry registry;

    /**
     * Constructs a new Student object with no AARA requirements by default.
     *
     * @param lui the student's 10-digit Learner Unique Identifier (LUI). The LUI
     *            must be unique to each student throughout the entire cohort.
     * @param givenNames the initial given names for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param familyName the initial family name for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param day the integer day of the date of birth for the student, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of birth for the student, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of birth for the student,
     *             which must be between 1965 and 2015.
     * @param house the initial house colour for the student, which must be one of:
     *              Blue, Green, Red, White, or Yellow.
     * @param registry the global registry.
     */
    public Student(Long lui, String givenNames, String familyName, int day,
                   int month, int year, String house, Registry registry) {
        this.lui = lui;
        given = sanitiseName(givenNames);
        family = sanitiseName(familyName);
        dob = LocalDate.of(year, month, day);
        this.house = house;
        this.aara = false;
        subjects = new SubjectList(registry);
        units = new UnitList(registry);
        exams = new ExamList(registry);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Student.class);
        }
    }

    /**
     * Constructs a new Student object with AARA requirements.
     * Overloaded constructor for a new Student requiring access arrangements
     * and reasonable adjustments.
     *
     * @param lui the student's 10-digit Learner Unique Identifier (LUI). The LUI
     *            must be unique to each student throughout the entire cohort.
     * @param givenNames the initial given names for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param familyName the initial family name for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     * @param day the integer day of the date of birth for the student, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of birth for the student, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of birth for the student,
     *             which must be between 1965 and 2015.
     * @param house the initial house colour for the student, which must be one of:
     *              Blue, Green, Red, White, or Yellow.
     * @param aara the initial aara setting for the student, true or false:
     *             true requires AARA adjustments, false does not.
     * @param registry the global registry.
     */
    public Student(Long lui, String givenNames, String familyName, int day,
                   int month, int year, String house, Boolean aara, Registry registry) {
        this.lui = lui;
        given = sanitiseName(givenNames);
        family = sanitiseName(familyName);
        dob = LocalDate.of(year, month, day);
        this.house = house;
        this.aara = aara;
        subjects = new SubjectList(registry);
        units = new UnitList(registry);
        exams = new ExamList(registry);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Student.class);
        }
    }

    /**
     * Constructs a Student by reading a description from a text stream
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic failure
     */
    public Student(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.subjects = new SubjectList(registry);
        this.units = new UnitList(registry);
        this.exams = new ExamList(registry);
        this.registry = registry;

        try {
            streamIn(br, registry, nthItem);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error reading Student data: " + e.getMessage(), e);
        }

        if (registry != null) {
            registry.add(this, Student.class);
        }
    }

    /**
     * Return a string from the input string complying with the following rules:
     * - a single string with one or more names.
     * - names must contain only alphabetic characters, hyphens, or apostrophes
     * - multiple names must be separated by one or more spaces.
     * - any leading and trailing spaces are ignored
     *
     * @param text - the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseName(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String cleaned = text.trim().replaceAll("\\s+", " ");

        if (!cleaned.matches("[a-zA-Z\\s\\-']+")) {
            cleaned = cleaned.replaceAll("[^a-zA-Z\\s\\-']", "");
        }

        return cleaned;
    }

    /**
     * Used to write data to the disk.
     *
     * The format of the text written to the stream must be matched exactly by streamIn, so it
     * is very important to format the output as described.
     *
     * @param bw writer, already opened. Your data should be written at the current
     *           file position
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // "1. LIAM ALEXANDER SMITH"
        bw.write(nthItem + ". " + (given + " " + family).toUpperCase().trim()
                + System.lineSeparator());


        bw.write("LUI: " + lui + ", Family Name: " + family + ", Given Name(s): " + given
                + ", Date of Birth: " + dob + ", House: " + house + ", AARA: "
                + aara + System.lineSeparator());

        if (subjects != null && !subjects.all().isEmpty()) {
            bw.write("Subjects: ");
            boolean first = true;
            for (Subject subject : subjects.all()) {
                if (!first) {
                    bw.write(", ");
                }
                bw.write(subject.getTitle());
                first = false;
            }
            bw.write(System.lineSeparator());
        }
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.
     *
     * For the format of the text in the input stream, refer to the streamOut documentation.
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

        // "1. LIAM ALEXANDER SMITH"
        String heading = CSSE7023.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem);
        }
        String[] bits = heading.split("\\. ", 2);
        if (bits.length != 2) {
            throw new RuntimeException("Invalid student format: " + heading);
        }

        int index = CSSE7023.toInt(bits[0],
                "Number format exception parsing Student "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Student index out of sync!");
        }

        String detailLine = CSSE7023.getLine(br);
        if (detailLine == null) {
            throw new RuntimeException("EOF reading Student #"
                    + nthItem + " details");
        }

        String[] details = detailLine.split(", ");
        for (String detail : details) {
            String[] kv = CSSE7023.keyValuePair(detail);
            if (kv == null || kv.length != 2) {
                continue;
            }
            switch (kv[0]) {
                case "LUI":
                    this.lui = CSSE7023.toLong(kv[1],
                            "Invalid LUI: " + kv[1]);
                    break;
                case "Family Name":
                    this.family = sanitiseName(kv[1]);
                    break;
                case "Given Name(s)":
                    this.given = sanitiseName(kv[1]);
                    break;
                case "Date of Birth":
                    this.dob = CSSE7023.toLocalDate(kv[1],
                            "Invalid date: " + kv[1]);
                    break;
                case "House":
                    this.house = kv[1];
                    break;
                case "AARA":
                    this.aara = CSSE7023.toBoolean(kv[1],
                            "Invalid AARA value: " + kv[1]);
                    break;
            }
        }

        String subjectsLine = CSSE7023.getLine(br);
        if (subjectsLine != null && subjectsLine.startsWith("Subjects: ")) {
            String subjectsList = subjectsLine.substring("Subjects: ".length());
            String[] subjectNames = subjectsList.split(", ");

            for (String subjectName : subjectNames) {
                if (registry != null) {
                    subjectName = subjectName.trim();

                    Subject subject = registry.find(subjectName, Subject.class);

                    if (subject == null) {
                        for (Subject s : registry.getAll(Subject.class)) {
                            if (s.getTitle().equals(subjectName)) {
                                subject = s;
                                break;
                            }
                        }
                    }
                    if (subject != null) {
                        if (!this.subjects.getItems().contains(subject)) {
                            this.subjects.getItems().add(subject);
                        }
                    } else {
                        System.err.println("WARNING: Subject '" + subjectName
                                + "' not found in registry for student " + this.lui);
                    }
                }
            }
        }
        if (this.given == null) {
            this.given = "";
        }
        if (this.family == null) {
            this.family = "";
        }
        if (this.house == null) {
            this.house = "";
        }
        if (this.aara == null) {
            this.aara = false;
        }
        if (this.dob == null) {
            this.dob = LocalDate.now();
        }
        if (this.lui == null) {
            this.lui = System.currentTimeMillis();
        }
    }

    /**
     * Creates and returns a string representation of this student's detailed state.
     *
     * @return the string representation of this student's detailed state.
     */
    public String getFullDetail() {
        final String nameLine = String.valueOf(lui) + " " + this.shortName() + "\n";
        StringBuilder studentPrint = new StringBuilder();
        studentPrint.append(nameLine);
        studentPrint.append(this.subjects.toString());
        studentPrint.append("\n");
        studentPrint.append(this.exams.toString());
        studentPrint.append("=".repeat(60));
        studentPrint.append("\n");
        return studentPrint.toString();
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return an Object[] containing class values suitable for use in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{lui, firstName(), familyName(), dob, house, aara
        };
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return String.valueOf(lui);
    }

    /**
     * Change the LUI of the student.
     *
     * @param lui the student's 10-digit Learner Unique Identifier (LUI). The LUI
     *            must be unique to each student throughout the entire cohort.
     */
    public void changeLui(Long lui) {
        this.lui = lui;
    }

    /**
     * Sets the given names of the student.
     *
     * @param givenNames the new given names for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     */
    public void setGiven(String givenNames) {
        if (givenNames != null && givenNames.length() > 0) {
            given = sanitiseName(givenNames);
        }
    }

    /**
     * Sets the family name of the student.
     *
     * @param familyName the new family name for the student, which must be a
     *                   single string with one or more names. Names must contain
     *                   only alphabetic characters, hyphens, or apostrophes; and
     *                   multiple names must be separated by one or more spaces.
     *                   Any leading and trailing spaces are ignored.
     */
    public void setFamily(String familyName) {
        if (familyName != null && familyName.length() > 0) {
            family = sanitiseName(familyName);
        }
    }

    /**
     * Gets the LUI of this student.
     *
     * @return the 10-digit LUI of this student as a Long.
     */
    public Long getLui() {
        return lui;
    }

    /**
     * Gets the given name(s) of this student.
     *
     * @return the given name(s) of this student.
     */
    public String givenNames() {
        if (given != null && given.length() > 0) {
            return given;
        }
        return "";
    }

    /**
     * Gets the first given name of this student.
     *
     * @return the first given name of this student.
     */
    public String firstName() {
        if (given != null && given.length() > 0) {
            String stripped = given.replaceAll("\\s+", " ").strip();
            String[] tokens = stripped.split(" ");
            if (tokens.length > 0) {
                return tokens[0];
            }
        }
        return "";
    }

    /**
     * Gets the family name of this student.
     *
     * @return the family name of this student.
     */
    public String familyName() {
        if (family != null && family.length() > 0) {
            return family;
        } else {
            return "";
        }
    }

    /**
     * Gets the first given name and family name of this student.
     *
     * @return the first given name and family name of this student.
     */
    public String shortName() {
        return this.firstName() + " " + family;
    }

    /**
     * Gets all the given name(s) and family name of this student.
     *
     * @return all the given name(s) and family name of this student.
     */
    public String fullName() {
        return given + " " + family;
    }

    /**
     * Gets the date of birth of this student.
     *
     * @return the date of birth of this student.
     */
    public LocalDate getDob() {
        return dob;
    }

    /**
     * Gets the house colour of this student.
     *
     * @return the house colour of this student.
     */
    public String getHouse() {
        return house;
    }

    /**
     * Returns true if this student is an AARA student.
     * (i.e. the student requires Access Arrangements and Reasonable Adjustments).
     *
     * @return true if this student is an AARA student, false otherwise.
     */
    public Boolean isAara() {
        return aara;
    }

    /**
     * Gets the {@link SubjectList} for this student.
     *
     * @return the reference to this student's {@link SubjectList}.
     */
    public SubjectList getSubjects() {
        return subjects;
    }

    /**
     * Gets the {@link ExamList} for this student.
     *
     * @return the reference to this student's {@link ExamList}.
     */
    public ExamList getExams() {
        return exams;
    }

    /**
     * Adds a subject to this student.
     *
     * @param subject the {@link Subject} being added to this student.
     */
    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    /**
     * Adds a unit to this student.
     *
     * @param unit the {@link Unit} being added to this student.
     */
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    /**
     * Adds an exam to this student.
     *
     * @param exam the {@link Exam} being added to this student.
     */
    public void addExam(Exam exam) {
        exams.add(exam);
    }

    /**
     * Removes a subject from this student.
     *
     * @param subject the {@link Subject} being removed from this student.
     */
    public void removeSubject(Subject subject) {
        this.subjects.remove(subject);
    }

    /**
     * Creates and returns a string representation of this student's basic state.
     *
     * @return the string representation of this student's basic state.
     */
    @Override
    public String toString() {
        final String nameLine = String.valueOf(lui) + " " + this.shortName() + "\n";
        StringBuilder studentPrint = new StringBuilder();
        studentPrint.append(nameLine);
        studentPrint.append(this.subjects.toString());
        studentPrint.append(this.exams.toString());
        studentPrint.append("=".repeat(60));
        studentPrint.append("\n");
        return studentPrint.toString();
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
        Student other = (Student) o;
        return other.lui.equals(this.lui);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return this.lui.hashCode();
    }
}