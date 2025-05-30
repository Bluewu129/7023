package examblock.model;

import java.time.LocalDate;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents a Year 12 student with comprehensive personal and academic information.
 * Manages student subjects, units, and exam assignments while maintaining data integrity.
 * Supports both standard students and those requiring AARA 
 * (Access Arrangements and Reasonable Adjustments).
 */
public class Student implements StreamManager, ManageableListItem {

    /**
     * Pattern for validating name fields - allows letters, spaces, hyphens, and apostrophes only
     */
    private static final String NAME_VALIDATION_PATTERN = "[a-zA-Z\\s\\-']+";

    /**
     * Student's unique 10-digit Learner Unique Identifier
     */
    private Long lui;

    /**
     * Student's given name(s) as a single space-separated string
     */
    private String given;

    /**
     * Student's family name
     */
    private String family;

    /**
     * Student's date of birth
     */
    private LocalDate dob;

    /**
     * Student's house colour assignment
     */
    private String house;

    /**
     * Whether student requires Access Arrangements and Reasonable Adjustments
     */
    private Boolean aara;

    /**
     * Collection of subjects this student is enrolled in
     */
    private final SubjectList subjects;

    /**
     * Collection of current units this student is taking
     */
    private final UnitList units;

    /**
     * Collection of exams this student is scheduled for in current exam block
     */
    private final ExamList exams;

    /**
     * Reference to global registry for object management
     */
    private Registry registry;

    /**
     * Creates a new student with standard (non-AARA) configuration.
     * Names are automatically sanitized to remove invalid characters and normalize spacing.
     *
     * @param lui        unique 10-digit Learner Unique Identifier (
     *                   must be unique across entire cohort)
     * @param givenNames student's given names (alphabetic characters, hyphens, apostrophes only)
     * @param familyName student's family name (same constraints as given names)
     * @param day        birth day (must be valid for given month and year)
     * @param month      birth month (1-12 inclusive)
     * @param year       birth year (1965-2015 inclusive for Year 12 students)
     * @param house      student's house color (Blue, Green, Red, White, or Yellow)
     * @param registry   global registry for centralized object management
     */
    public Student(Long lui, String givenNames, String familyName, int day, int month, int year,
                   String house, Registry registry) {
        this(lui, givenNames, familyName, day, month, year, house, false, registry);
    }

    /**
     * Creates a new student with specified AARA requirements.
     * Overloaded constructor allowing explicit specification of access arrangement needs.
     *
     * @param lui        unique 10-digit Learner Unique Identifier
     * @param givenNames student's given names
     * @param familyName student's family name
     * @param day        birth day
     * @param month      birth month (1-12)
     * @param year       birth year (1965-2015)
     * @param house      student's house color
     * @param aara       true if student requires Access Arrangements and Reasonable Adjustments
     * @param registry   global registry for object management
     */
    public Student(Long lui, String givenNames, String familyName, int day, int month, int year,
                   String house, Boolean aara, Registry registry) {
        this.lui = lui;
        this.given = sanitiseName(givenNames);
        this.family = sanitiseName(familyName);
        this.dob = LocalDate.of(year, month, day);
        this.house = house;
        this.aara = aara;
        this.subjects = new SubjectList(registry);
        this.units = new UnitList(registry);
        this.exams = new ExamList(registry);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Student.class);
        }
    }

    /**
     * Creates a student by deserializing from a text stream.
     * Used for loading student data from files with proper error handling.
     *
     * @param br       BufferedReader opened and positioned at student data
     * @param registry global object registry for resolving subject references
     * @param nthItem  expected index of this student in the serialization stream
     * @throws IOException      on stream reading failures
     * @throws RuntimeException on data format or logic errors
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
     * Sanitizes name input according to validation rules.
     * Removes invalid characters, normalizes whitespace, and trims boundaries.
     * Ensures names contain only alphabetic characters, spaces, hyphens, and apostrophes.
     *
     * @param text the name string to sanitize
     * @return cleaned name string conforming to validation rules
     */
    public String sanitiseName(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Normalize whitespace - replace multiple spaces with single space
        String cleaned = text.trim().replaceAll("\\s+", " ");

        // Remove invalid characters if present
        if (!cleaned.matches(NAME_VALIDATION_PATTERN)) {
            cleaned = cleaned.replaceAll("[^a-zA-Z\\s\\-']", "");
        }

        return cleaned;
    }

    /**
     * Writes student data to output stream in structured format.
     * Format includes uppercase header, detailed information line, and subject enrollments.
     *
     * @param bw      writer already opened at current file position
     * @param nthItem student's position in the serialization stream for consistency checks
     * @throws IOException on stream writing failures
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // Write header: "1. LIAM ALEXANDER SMITH"
        bw.write(nthItem + ". " + (given + " " 
                + family).toUpperCase().trim() + System.lineSeparator());

        // Write detailed student information
        bw.write("LUI: " + lui + ", Family Name: " 
                + family 
                + ", Given Name(s): " 
                + given 
                +
                ", Date of Birth: " + dob + ", House: "
                + house + ", AARA: " + aara + System.lineSeparator());

        // Write subject enrollments if any exist
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
     * Reads student data from input stream with comprehensive error handling.
     * Parses header, detailed information, and subject enrollments while resolving references.
     *
     * @param br       reader already opened at student data position
     * @param registry global object registry for resolving subject references
     * @param nthItem  expected student index for consistency validation
     * @throws IOException      on stream reading failures
     * @throws RuntimeException on data format or consistency errors
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem) 
            throws IOException, RuntimeException {
        // Parse student header: "1. LIAM ALEXANDER SMITH"
        String heading = CSSE7023.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem);
        }
        String[] bits = heading.split("\\. ", 2);
        if (bits.length != 2) {
            throw new RuntimeException("Invalid student format: " + heading);
        }

        int index = CSSE7023.toInt(bits[0], 
                "Number format exception parsing Student " + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Student index out of sync!");
        }

        // Parse detailed student information line
        String detailLine = CSSE7023.getLine(br);
        if (detailLine == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem + " details");
        }

        String[] details = detailLine.split(", ");
        for (String detail : details) {
            String[] kv = CSSE7023.keyValuePair(detail);
            if (kv == null || kv.length != 2) {
                continue;
            }
            switch (kv[0]) {
                case "LUI":
                    this.lui = CSSE7023.toLong(kv[1], "Invalid LUI: " + kv[1]);
                    break;
                case "Family Name":
                    this.family = sanitiseName(kv[1]);
                    break;
                case "Given Name(s)":
                    this.given = sanitiseName(kv[1]);
                    break;
                case "Date of Birth":
                    this.dob = CSSE7023.toLocalDate(kv[1], "Invalid date: " + kv[1]);
                    break;
                case "House":
                    this.house = kv[1];
                    break;
                case "AARA":
                    this.aara = CSSE7023.toBoolean(kv[1], "Invalid AARA value: " + kv[1]);
                    break;
            }
        }

        // Parse subject enrollments if present
        String subjectsLine = CSSE7023.getLine(br);
        if (subjectsLine != null && subjectsLine.startsWith("Subjects: ")) {
            String subjectsList = subjectsLine.substring("Subjects: ".length());
            String[] subjectNames = subjectsList.split(", ");

            for (String subjectName : subjectNames) {
                if (registry != null) {
                    subjectName = subjectName.trim();

                    // Try direct ID lookup first
                    Subject subject = registry.find(subjectName, Subject.class);

                    // Fallback to title-based search
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

        // Set default values for any missing fields
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
     * Creates detailed string representation of student's complete state.
     * Includes LUI, short name, subject list, exam list, and separator line.
     *
     * @return comprehensive string representation of this student
     */
    public String getFullDetail() {
        final String nameLine = String.valueOf(lui) + " " + this.shortName() + "\n";
        StringBuilder studentPrint = new StringBuilder();
        studentPrint.append(nameLine);
        studentPrint.append(this.subjects.toString()).append("\n");
        studentPrint.append(this.exams.toString());
        studentPrint.append("=".repeat(60)).append("\n");
        return studentPrint.toString();
    }

    /**
     * Creates array of values suitable for table display in view components.
     * Provides core student information for JTable integration.
     *
     * @return array containing LUI, first name, family name, DOB, house, and AARA status
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{lui, firstName(), familyName(), dob, house, aara};
    }

    /**
     * Provides unique string identifier for registry lookup.
     * Uses LUI as the unique identifier for this student.
     *
     * @return string representation of the LUI
     */
    @Override
    public String getId() {
        return String.valueOf(lui);
    }

    // Accessor and mutator methods

    /**
     * Updates the student's LUI.
     *
     * @param lui new 10-digit Learner Unique Identifier
     */
    public void changeLui(Long lui) {
        this.lui = lui;
    }

    /**
     * Updates the student's given names with sanitization.
     * Only updates if provided value is valid and non-empty.
     *
     * @param givenNames new given names for the student
     */
    public void setGiven(String givenNames) {
        if (givenNames != null && givenNames.length() > 0) {
            given = sanitiseName(givenNames);
        }
    }

    /**
     * Updates the student's family name with sanitization.
     * Only updates if provided value is valid and non-empty.
     *
     * @param familyName new family name for the student
     */
    public void setFamily(String familyName) {
        if (familyName != null && familyName.length() > 0) {
            family = sanitiseName(familyName);
        }
    }

    /**
     * Gets the student's LUI.
     *
     * @return the 10-digit LUI as a Long
     */
    public Long getLui() {
        return lui;
    }

    /**
     * Gets the student's complete given names.
     *
     * @return the given names string, or empty string if null
     */
    public String givenNames() {
        if (given != null && given.length() > 0) {
            return given;
        }
        return "";
    }

    /**
     * Extracts the first given name from the complete given names.
     * Handles multiple names by splitting on spaces and returning the first token.
     *
     * @return the first given name, or empty string if none available
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
     * Gets the student's family name.
     *
     * @return the family name, or empty string if null
     */
    public String familyName() {
        if (family != null && family.length() > 0) {
            return family;
        } else {
            return "";
        }
    }

    /**
     * Creates short name combining first given name and family name.
     *
     * @return formatted string with first name and family name
     */
    public String shortName() {
        return this.firstName() + " " + family;
    }

    /**
     * Creates full name combining all given names and family name.
     *
     * @return formatted string with all names
     */
    public String fullName() {
        return given + " " + family;
    }

    /**
     * Gets the student's date of birth.
     *
     * @return the birth date as LocalDate
     */
    public LocalDate getDob() {
        return dob;
    }

    /**
     * Gets the student's house color assignment.
     *
     * @return the house color string
     */
    public String getHouse() {
        return house;
    }

    /**
     * Checks if student requires Access Arrangements and Reasonable Adjustments.
     *
     * @return true if student is AARA, false otherwise
     */
    public Boolean isAara() {
        return aara;
    }

    /**
     * Gets reference to student's subject collection for enrollment management.
     *
     * @return the SubjectList containing enrolled subjects
     */
    public SubjectList getSubjects() {
        return subjects;
    }

    /**
     * Gets reference to student's exam collection for exam scheduling.
     *
     * @return the ExamList containing scheduled exams
     */
    public ExamList getExams() {
        return exams;
    }

    /**
     * Adds a subject to this student's enrollment list.
     *
     * @param subject the Subject to add to student's enrollment
     */
    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    /**
     * Adds a unit to this student's current unit list.
     *
     * @param unit the Unit to add to student's current units
     */
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    /**
     * Adds an exam to this student's scheduled exam list.
     *
     * @param exam the Exam to add to student's schedule
     */
    public void addExam(Exam exam) {
        exams.add(exam);
    }

    /**
     * Removes a subject from this student's enrollment list.
     *
     * @param subject the Subject to remove from student's enrollment
     */
    public void removeSubject(Subject subject) {
        this.subjects.remove(subject);
    }

    /**
     * Creates basic string representation of student's state.
     * Similar to getFullDetail but used for general toString() purposes.
     *
     * @return string representation including LUI, name, subjects, and exams
     */
    @Override
    public String toString() {
        final String nameLine = String.valueOf(lui) + " " + this.shortName() + "\n";
        StringBuilder studentPrint = new StringBuilder();
        studentPrint.append(nameLine);
        studentPrint.append(this.subjects.toString());
        studentPrint.append(this.exams.toString());
        studentPrint.append("=".repeat(60)).append("\n");
        return studentPrint.toString();
    }

    /**
     * Implements class-specific equality comparison based on LUI uniqueness.
     * Two students are equal if they have the same LUI, regardless of other fields.
     *
     * @param o the other object to compare
     * @return true if objects represent the same student (same LUI)
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
     * Generates hash code based on LUI for consistent hashing behavior.
     *
     * @return hash code derived from the LUI
     */
    @Override
    public int hashCode() {
        return this.lui.hashCode();
    }
}