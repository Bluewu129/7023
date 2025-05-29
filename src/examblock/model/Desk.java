package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents an individual desk in an exam venue.
 */

public class Desk {

    /** The desk number. */
    private final int id;
    /** The student's family name. */
    private String familyName;
    /** The student's first given name and initial of first middle name, if any. */
    private String givenAndInit;


    private Exam exam;
    private Student student;
    private Long lui;


    /**
     * Constructs a {@code Desk}.
     * Assigns the integer deskNumber as the numerical identifier and
     * assigns null Strings to the names.
     *
     * @param deskNumber the non-zero positive integer desk number.
     */
    public Desk(int deskNumber) {
        id = deskNumber; // Note: does no enforcement of requirements
        familyName = null; // Should be null per javadoc, NOT empty ""
        givenAndInit = null; // Will test for this = null, not empty ""
    }

    /**
     * Gets the number of this desk.
     *
     * @return The number of this desk.
     */
    public int deskNumber() {
        return id;
    }

    /**
     * Gets the family name of the student assigned to this desk.
     *
     * @return The family name of the student assigned to this desk.
     */
    public String deskFamilyName() {
        return familyName;
    }

    /**
     * Gets the first given name and initial of the student assigned to this desk.
     * Gets the first given name, a space, the initial of first middle name, if any,
     * with a full stop after the initial (if present) of the student assigned to this desk.
     *
     * @return The first given name and initial of the student assigned to this desk.
     */
    public String deskGivenAndInit() {
        return givenAndInit;
    }

    /**
     * Sets the first given name and initial of the student assigned to this desk.
     *
     * @param givenAndInit a single string with the first given name, a space,
     *                     the initial of first middle name, if any, with a full stop
     *                     after the initial (if present) of the student assigned.
     */
    public void setGivenAndInit(String givenAndInit) {
        this.givenAndInit = givenAndInit; // Note: does no enforcement of requirements
    }

    /**
     * Returns a string representation of this desk.
     *  * (Returns the desk number and any assigned student.)
     *  *
     *  * @return The string representation of this desk.
     */
    @Override
    public String toString() {
        String assignedStudent = "";
        if (familyName != null && !familyName.isBlank()) {
            assignedStudent = familyName;
            if (givenAndInit != null && !givenAndInit.isBlank()) {
                assignedStudent += ", " + givenAndInit;
            }
        }
        return "Desk: " + id + " " + assignedStudent;
    }
    public String deskExam() {
        return exam.getTitle();
    }

    public long deskLui() {
        return lui;
    }

    public String deskStudent() {
        return student.familyName();
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public void setStudent(Student student) {
        this.student = student;
        if (student != null) {
            this.lui = student.getLui();
            setGivenAndInit(getGivenAndInit(student.givenNames()));
        }
    }

    public void streamIn(BufferedReader br, String examName) throws IOException {
        // Implementation for reading desk data
        String line = br.readLine();
        if (line != null) {
            // Parse desk data from line
            // Format: "deskNumber,familyName,givenAndInit"
            String[] parts = line.split(",");
            if (parts.length >= 1) {
                // Parse as needed
            }
        }
    }

    public void streamOut(BufferedWriter bw) throws IOException {
        bw.write(id + "," +
                (familyName != null ? familyName : "") + "," +
                (givenAndInit != null ? givenAndInit : ""));
        bw.newLine();
    }

    // Helper method for getGivenAndInit
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
}
