package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;

/**
 * An object describing a single Year 12 Student.
 * Updated to implement StreamManager and ManageableListItem interfaces.
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
    /** The registry for dependencies. */
    private Registry registry;

    /**
     * Constructs a new Student object with no AARA requirements by default.
     */
    public Student(Long lui, String givenNames, String familyName, int day,
                   int month, int year, String house) {
        this(lui, givenNames, familyName, day, month, year, house, false);
    }

    /**
     * Constructs a new Student object with AARA requirements.
     */
    public Student(Long lui, String givenNames, String familyName, int day,
                   int month, int year, String house, Boolean aara) {
        this.lui = lui;
        given = cleanName(givenNames);
        family = cleanName(familyName);
        dob = LocalDate.of(year, month, day);
        this.house = house;
        this.aara = aara;
        subjects = new SubjectList();
        units = new UnitList();
        exams = new ExamList();
        this.registry = null;
    }

    /**
     * Constructs a Student with registry.
     */
    public Student(Long lui, String givenNames, String familyName, int day,
                   int month, int year, String house, Boolean aara, Registry registry) {
        this(lui, givenNames, familyName, day, month, year, house, aara);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Student.class);
        }
    }

    /**
     * Constructs a Student by reading from a stream.
     */
    public Student(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;
        subjects = new SubjectList();
        units = new UnitList();
        exams = new ExamList();

        readStudentData(br, nthItem);

        if (registry != null) {
            registry.add(this, Student.class);
        }

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Student: " + lui + " " + shortName());
        }
    }

    /**
     * Cleans a name by removing extra spaces and formatting correctly.
     */
    private String cleanName(String rawName) {
        if (rawName == null) {
            return "";
        }
        return rawName.trim().replaceAll("\\s+", " ");
    }

    /**
     * Reads student data from the stream.
     */
    private void readStudentData(BufferedReader br, int nthItem)
            throws IOException, RuntimeException {
        // Read header line: "1. 1234567890 John Smith"
        String headerLine = CSSE7023.getLine(br);
        if (headerLine == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem);
        }

        String[] headerParts = headerLine.split("\\. ", 2);
        if (headerParts.length != 2) {
            throw new RuntimeException("Invalid student header format: " + headerLine);
        }

        int index = CSSE7023.toInt(headerParts[0],
                "Number format exception parsing Student " + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Student index out of sync! Expected " + nthItem +
                    " but got " + index);
        }

        // Parse LUI and name
        String[] luiAndName = headerParts[1].split(" ", 2);
        if (luiAndName.length < 2) {
            throw new RuntimeException("Invalid student data format: " + headerParts[1]);
        }

        lui = CSSE7023.toLong(luiAndName[0], "Invalid LUI format");

        // Split given and family names (assume last word is family name)
        String[] nameParts = luiAndName[1].trim().split(" ");
        if (nameParts.length == 1) {
            given = "";
            family = nameParts[0];
        } else {
            family = nameParts[nameParts.length - 1];
            StringBuilder givenBuilder = new StringBuilder();
            for (int i = 0; i < nameParts.length - 1; i++) {
                if (i > 0) givenBuilder.append(" ");
                givenBuilder.append(nameParts[i]);
            }
            given = givenBuilder.toString();
        }

        // Read additional data (DOB, house, AARA) - simplified for now
        dob = LocalDate.of(2007, 1, 1); // Default DOB
        house = "Blue"; // Default house
        aara = false; // Default AARA
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + lui + " " + shortName() + System.lineSeparator());
        // Additional student data could be written here
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        // This method is implemented in the constructor
        throw new UnsupportedOperationException("Use constructor instead");
    }

    // All existing methods remain the same...
    public void changeLui(Long lui) {
        this.lui = lui;
    }

    public void setGiven(String givenNames) {
        if (givenNames != null && givenNames.length() > 0) {
            given = cleanName(givenNames);
        }
    }

    public void setFamily(String familyName) {
        if (familyName != null && familyName.length() > 0) {
            family = cleanName(familyName);
        }
    }

    public Long getLui() {
        return lui;
    }

    public String givenNames() {
        return given != null && given.length() > 0 ? given : "";
    }

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

    public String familyName() {
        return family != null && family.length() > 0 ? family : "";
    }

    public String shortName() {
        return firstName() + " " + family;
    }

    public String fullName() {
        return given + " " + family;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getHouse() {
        return house;
    }

    public Boolean isAara() {
        return aara;
    }

    public SubjectList getSubjects() {
        return subjects;
    }

    public ExamList getExams() {
        return exams;
    }

    public void addSubject(Subject subject) {
        subjects.addSubject(subject);
    }

    public void removeSubject(Subject subject) {
        subjects.removeSubject(subject);
    }

    @Override
    public String getFullDetail() {
        final String nameLine = String.valueOf(lui) + " " + shortName() + "\n";
        StringBuilder studentPrint = new StringBuilder();
        studentPrint.append(nameLine);
        studentPrint.append(subjects.toString());
        studentPrint.append("\n");
        studentPrint.append(exams.toString());
        studentPrint.append("=".repeat(60));
        studentPrint.append("\n");
        return studentPrint.toString();
    }

    @Override
    public Object[] toTableRow() {
        return new Object[]{lui, shortName(), house, aara ? "Yes" : "No"};
    }

    @Override
    public Object[] toLongTableRow() {
        return new Object[]{lui, fullName(), dob, house, aara ? "Yes" : "No",
                subjects.size()};
    }

    @Override
    public String getId() {
        return String.valueOf(lui);
    }

    @Override
    public String toString() {
        final String nameLine = String.valueOf(lui) + " " + shortName() + "\n";
        StringBuilder studentPrint = new StringBuilder();
        studentPrint.append(nameLine);
        studentPrint.append(subjects.toString());
        studentPrint.append(exams.toString());
        studentPrint.append("=".repeat(60));
        studentPrint.append("\n");
        return studentPrint.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return lui.equals(student.lui);
    }

    @Override
    public int hashCode() {
        return lui.hashCode();
    }
}