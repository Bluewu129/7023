package examblock.model;

import java.time.LocalDate;

/**
 * An object describing a single Year 12 Student.
 */
public class Student {

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
     */
    public Student(Long lui, String givenNames, String familyName, int day,
                   int month, int year, String house) {
        this(lui, givenNames, familyName, day, month, year, house, false);
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
     */
    public Student(Long lui, String givenNames, String familyName, int day,
                   int month, int year, String house, Boolean aara) {
        this.lui = lui;
        given = givenNames;
        family = familyName;
        dob = LocalDate.of(year, month, day);
        this.house = house;
        this.aara = aara;
        subjects = new SubjectList();
        units = new UnitList();
        exams = new ExamList();
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
        // only set the given names if the supplied names are not null or empty
        if (givenNames != null && givenNames.length() > 0) {
            given = givenNames;
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
        // only set the family name if the supplied name is not null or empty
        if (familyName != null && familyName.length() > 0) {
            family = familyName;
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
        subjects.addSubject(subject);
    }

    /**
     * Removes a subject from this student.
     *
     * @param subject the {@link Subject} being removed from this student.
     */
    public void removeSubject(Subject subject) {
        this.subjects.removeSubject(subject);
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
}