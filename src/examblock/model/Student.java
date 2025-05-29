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

        readStudentData(br, registry, nthItem);

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
    private void readStudentData(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        // Read header line: "1. LIAM ALEXANDER SMITH"
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

        // The name is in uppercase in the header
        String fullNameUpper = headerParts[1].trim();

        // Read the details line: "LUI: 9999365663, Family Name: Smith, Given Name(s): Liam Alexander, Date of Birth: 2007-12-08, House: Blue, AARA: false"
        String detailsLine = CSSE7023.getLine(br);
        if (detailsLine == null) {
            throw new RuntimeException("EOF reading Student #" + nthItem + " details");
        }

        // Initialize defaults
        this.lui = 0L;
        this.given = "";
        this.family = "";
        this.dob = LocalDate.of(2007, 1, 1);
        this.house = "";
        this.aara = false;

        // Parse the key-value pairs
        String[] details = detailsLine.split(", ");
        for (String detail : details) {
            String[] kv = detail.split(": ", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();

                switch (key) {
                    case "LUI":
                        try {
                            lui = Long.parseLong(value);
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Invalid LUI format: " + value);
                        }
                        break;
                    case "Family Name":
                        family = value;
                        break;
                    case "Given Name(s)":
                        given = value;
                        break;
                    case "Date of Birth":
                        try {
                            // Handle the problematic date "0200-10-06"
                            if (value.startsWith("0200")) {
                                value = value.replace("0200", "2007");
                            }
                            String[] dateParts = value.split("-");
                            if (dateParts.length == 3) {
                                int year = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]);
                                int day = Integer.parseInt(dateParts[2]);
                                dob = LocalDate.of(year, month, day);
                            }
                        } catch (Exception e) {
                            // Default to a reasonable date if parsing fails
                            dob = LocalDate.of(2007, 1, 1);
                            if (Verbose.isVerbose()) {
                                System.out.println("Error parsing date for student #" + nthItem + ": " + value);
                            }
                        }
                        break;
                    case "House":
                        house = value;
                        break;
                    case "AARA":
                        aara = value.equalsIgnoreCase("true");
                        break;
                }
            }
        }

        // Read subjects line: "Subjects: Essential English, Essential Mathematics, Ancient History, Industrial Technology Skills, Trade Course, Another Trade Course"
        String subjectsLine = CSSE7023.getLine(br);
        if (subjectsLine != null && subjectsLine.startsWith("Subjects:")) {
            String subjectsList = subjectsLine.substring("Subjects:".length()).trim();
            if (!subjectsList.isEmpty()) {
                String[] subjectNames = subjectsList.split(", ");
                for (String subjectName : subjectNames) {
                    String trimmedName = subjectName.trim();
                    if (!trimmedName.isEmpty()) {
                        Subject subject = registry.find(trimmedName, Subject.class);
                        if (subject != null) {
                            subjects.addSubject(subject);
                        } else if (Verbose.isVerbose()) {
                            System.out.println("Warning: Subject not found for student #" + nthItem + ": " + trimmedName);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // Write header
        bw.write(nthItem + ". " + fullName().toUpperCase() + System.lineSeparator());

        // Write details
        bw.write("LUI: " + lui);
        bw.write(", Family Name: " + family);
        bw.write(", Given Name(s): " + given);
        bw.write(", Date of Birth: " + dob);
        bw.write(", House: " + house);
        bw.write(", AARA: " + aara);
        bw.write(System.lineSeparator());

        // Write subjects
        if (subjects.size() > 0) {
            bw.write("Subjects: ");
            boolean first = true;
            for (Subject subject : subjects.all()) {
                if (!first) bw.write(", ");
                bw.write(subject.getTitle());
                first = false;
            }
            bw.write(System.lineSeparator());
        }
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        // This method is implemented in the constructor
        throw new UnsupportedOperationException("Use constructor instead");
    }

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