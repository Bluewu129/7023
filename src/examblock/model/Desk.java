package examblock.model;

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
     * Sets the family name of the student assigned to this desk.
     *
     * @param familyName the family name of the student assigned to this desk.
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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
}
