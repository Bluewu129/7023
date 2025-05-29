package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private final List<Subject> subjects;
    /** The list of the Student's current units. */
    private final List<Unit> units;
    /** The list of the Student's exams for the current exam block. */
    private final List<Exam> exams;
    /** The registry for dependencies. */
    private Registry registry;

    /**
     * Default constructor for factory use.
     */
    public Student() {
        this.lui = 0L;
        this.given = "";
        this.family = "";
        this.dob = LocalDate.now();
        this.house = "";
        this.aara = false;
        this.subjects = new ArrayList<>();
        this.units = new ArrayList<>();
        this.exams = new ArrayList<>();
        this.registry = null;
    }

    /**
     * Constructs a Student with parameters as per specification.
     * Order: lui, givenNames, familyName, day, month, year, house, aara, registry
     */
    public Student(Long lui, String givenNames, String familyName, int day, int month, int year,
                   String house, Boolean aara, Registry registry) {
        this.lui = lui;
        this.given = cleanName(givenNames);
        this.family = cleanName(familyName);
        this.dob = LocalDate.of(year, month, day);
        this.house = house;
        this.aara = aara;
        this.subjects = new ArrayList<>();
        this.units = new ArrayList<>();
        this.exams = new ArrayList<>();
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Student.class);
        }
    }

    /**
     * Constructs a Student by reading from a stream.
     * As per specification
     */
    public Student(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;
        this.subjects = new ArrayList<>();
        this.units = new ArrayList<>();
        this.exams = new ArrayList<>();

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

        // Remove numbers and special characters, keep only letters, spaces, hyphens, and apostrophes
        String cleaned = rawName.replaceAll("[^a-zA-Z\\s\\-']", "");

        // Normalize spaces
        cleaned = cleaned.trim().replaceAll("\\s+", " ");

        return cleaned;
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
                            // Handle various problematic date formats
                            if (value.startsWith("0200") || value.startsWith("200-")) {
                                value = value.replaceFirst("^0?200", "2007");
                            }

                            String[] dateParts = value.split("-");
                            if (dateParts.length == 3) {
                                int year = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]);
                                int day = Integer.parseInt(dateParts[2]);

                                // Validate year
                                if (year < 1900 || year > 2100) {
                                    year = 2007; // Default year
                                }

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
                            // Directly add to the student's subject list
                            subjects.add(subject);
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
            for (Subject subject : subjects) {
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

    /**
     * Gets a wrapper for the student's subjects list.
     * NOTE: This method is deprecated and should not be used.
     * Use getSubjectsList() instead.
     */
    @Deprecated
    public SubjectList getSubjects() {
        // Create a dummy SubjectList that doesn't use the registry
        // This is a workaround for compatibility
        return new SubjectList() {
            @Override
            public ArrayList<Subject> all() {
                return new ArrayList<>(subjects);
            }

            @Override
            public int size() {
                return subjects.size();
            }

            @Override
            public List<Subject> getItems() {
                return new ArrayList<>(subjects);
            }
        };
    }

    /**
     * Gets a wrapper for the student's exams list.
     * NOTE: This method is deprecated and should not be used.
     * Use getExamsList() instead.
     */
    @Deprecated
    public ExamList getExams() {
        // Create a dummy ExamList that doesn't use the registry
        // This is a workaround for compatibility
        return new ExamList() {
            @Override
            public ArrayList<Exam> all() {
                return new ArrayList<>(exams);
            }

            @Override
            public int size() {
                return exams.size();
            }

            @Override
            public List<Exam> getItems() {
                return new ArrayList<>(exams);
            }
        };
    }

    public void addSubject(Subject subject) {
        if (!subjects.contains(subject)) {
            subjects.add(subject);
        }
    }

    public void removeSubject(Subject subject) {
        subjects.remove(subject);
    }

    public void addExam(Exam exam) {
        if (!exams.contains(exam)) {
            exams.add(exam);
        }
    }

    /**
     * Clears the exams list.
     */
    public void clearExams() {
        exams.clear();
    }

    /**
     * Gets the actual subjects list.
     * This is the preferred method to use.
     */
    public List<Subject> getSubjectsList() {
        return new ArrayList<>(subjects);
    }

    /**
     * Gets the actual exams list.
     * This is the preferred method to use.
     */
    public List<Exam> getExamsList() {
        return new ArrayList<>(exams);
    }

    @Override
    public String getFullDetail() {
        final String nameLine = String.valueOf(lui) + " " + shortName() + "\n";
        StringBuilder studentPrint = new StringBuilder();
        studentPrint.append(nameLine);

        // Print subjects
        int counter = 1;
        for (Subject subject : subjects) {
            studentPrint.append(counter++).append(". ").append(subject.toString());
        }
        studentPrint.append("\n");

        // Print exams
        counter = 1;
        for (Exam exam : exams) {
            studentPrint.append(counter++).append(". ").append(exam.toString());
        }

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
        return getFullDetail();
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