package examblock.model;

import examblock.view.components.Verbose;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents an individual desk in an exam venue.
 */
public class Desk {

    /** The desk number. */
    private int id;

    /** The student's family name. */
    private String familyName;

    /** The student's first given name and initial of first middle name, if any. */
    private String givenAndInit;

    /** The LUI of the student assigned to this desk. */
    private long lui;

    /** The exam being taken at this desk. */
    private Exam exam;

    /**
     * Constructs a Desk.
     * Assigns the integer deskNumber as the numerical identifier and
     * assigns empty Strings to the names.
     *
     * @param deskNumber the non-zero positive integer desk number.
     */
    public Desk(int deskNumber) {
        id = deskNumber;
        familyName = "";
        givenAndInit = "";
        lui = 0L;
        exam = null;
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
     * Return the student allocated to this desk
     *
     * @return the Student, or an empty string if not allocated
     */
    public String deskStudent() {
        if (!familyName.isEmpty()) {
            return familyName + (givenAndInit.isEmpty() ? "" : ", " + givenAndInit);
        }
        return "";
    }

    /**
     * Gets the LUI of the student assigned to this desk.
     *
     * @return The LUI of the student assigned to this desk.
     */
    public long deskLui() {
        return lui;
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
     * Allocate a student to this desk
     *
     * @param student - student to assign
     */
    public void setStudent(Student student) {
        if (student != null) {
            this.familyName = student.familyName();
            this.lui = student.getLui();
            String givenNames = student.givenNames();

            if (givenNames != null && !givenNames.isEmpty()) {
                String[] names = givenNames.split(" ");
                if (names.length > 1) {
                    this.givenAndInit = names[0] + " " + names[1].substring(0, 1) + ".";
                } else {
                    this.givenAndInit = names[0];
                }
            } else {
                this.givenAndInit = "";
            }
        } else {
            this.familyName = "";
            this.givenAndInit = "";
            this.lui = 0L;
        }
    }

    /**
     * Sets the first given name and initial of the student assigned to this desk.
     *
     * @param givenAndInit a single string with the first given name, a space,
     *                     the initial of first middle name, if any, with a full stop
     *                     after the initial (if present) of the student assigned.
     */
    public void setGivenAndInit(String givenAndInit) {
        this.givenAndInit = givenAndInit;
    }

    /**
     * Allocate an exam for this desk
     *
     * @param exam - exam to allocate
     */
    public void setExam(Exam exam) {
        this.exam = exam;
    }

    /**
     * Return the exam being taken at this desk
     *
     * @return the exam at this desk
     */
    public String deskExam() {
        if (exam != null) {
            return exam.getShortTitle();
        } else {
            return "";
        }
    }

    /**
     * Returns a string representation of this desk.
     * (Returns the desk number and any assigned student.)
     *
     * @return The string representation of this desk.
     */
    @Override
    public String toString() {
        String assignedStudent = "";
        if (familyName != null && !familyName.isBlank()) {
            assignedStudent = familyName;
            if (givenAndInit != null && !givenAndInit.isBlank()) {
                assignedStudent = givenAndInit + " " + assignedStudent;
            }
        }
        return "Desk: " + id + " " + assignedStudent;
    }

    /**
     * Write a string representation of this desk to disk.
     *
     * Format:
     * Desk: [id] [familyName], [givenAndInit] - LUI: [lui] - Exam: [examName]
     *
     * Example:
     * Desk: 1 Smith, John A. - LUI: 1234567890 - Exam:
     * Year 12 External Assessment Mathematical Methods Paper 1
     *
     * [8] Exception handling in stream operations.
     *
     * @param bw - stream to write to
     * @throws IOException - on any IO related issues
     */
    public void streamOut(BufferedWriter bw) throws IOException {
        StringBuilder line = new StringBuilder();
        line.append("Desk: ").append(id);

        if (!familyName.isEmpty()) {
            line.append(" ").append(familyName);
            if (!givenAndInit.isEmpty()) {
                line.append(", ").append(givenAndInit);
            }
            line.append(" - LUI: ").append(lui);
        }

        if (exam != null) {
            line.append(" - Exam: ").append(exam.getShortTitle());
        }

        bw.write(line.toString() + System.lineSeparator());

        if (Verbose.isVerbose()) {
            System.out.println("Wrote desk: " + id);
        }
    }

    /**
     * Read itself from an input stream.
     *
     * [8] Exception handling for data parsing operations.
     * [9] Reflection usage for setting private Desk fields.
     * Expected format:
     * Desk: [id] [familyName], [givenAndInit] - LUI: [lui] - Exam: [examName]
     *
     * @param br - stream to read from
     * @param examName - Use this as the exam name value (saves duplicating it for each desk)
     * @throws IOException - on any IO related issues
     */
    public void streamIn(BufferedReader br, String examName) throws IOException {
        String line = CSSE7023.getLine(br);
        if (line == null) {
            throw new IOException("EOF reading Desk");
        }

        if (!line.startsWith("Desk: ")) {
            throw new IOException("Invalid desk format: " + line);
        }

        try {
            String content = line.substring("Desk: ".length());

            String[] parts = content.split(" - ");
            String firstPart = parts[0];
            String[] firstTokens = firstPart.split(" ", 2);
            this.id = Integer.parseInt(firstTokens[0]);
            this.familyName = "";
            this.givenAndInit = "";
            this.lui = 0L;
            this.exam = null;

            if (firstTokens.length > 1) {
                String studentInfo = firstTokens[1];
                if (studentInfo.contains(",")) {
                    String[] nameParts = studentInfo.split(",", 2);
                    this.familyName = nameParts[0].trim();
                    this.givenAndInit = nameParts[1].trim();
                } else {
                    this.familyName = studentInfo.trim();
                }
            }

            for (String part : parts) {
                if (part.startsWith("LUI: ")) {
                    this.lui = Long.parseLong(part.substring("LUI: ".length()));
                }
            }

            if (Verbose.isVerbose()) {
                System.out.println("Loaded Desk: " + id
                        + (familyName.isEmpty() ? " (unassigned)" : " assigned to " + familyName));
            }

        } catch (NumberFormatException e) {
            throw new IOException("Number format exception parsing Desk: " + line, e);
        } catch (Exception e) {
            throw new IOException("Error parsing Desk: " + line, e);
        }
    }
}