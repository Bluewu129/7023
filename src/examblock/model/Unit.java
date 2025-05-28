package examblock.model;

/**
 * An object describing a single-semester Year 12 Unit of a Year 12 Subject.
 * These are typically Unit 3 or Unit 4 for the Year 12 units, but may be different.
 */
public class Unit {

    /** This Unit's parent subject. */
    private final Subject subject;
    /** The single-character identifier of this Unit. */
    private final Character unitId;
    /** The title of this Unit. */
    private final String title;
    /** The description of the Unit. */
    private final String description;

    /**
     * Constructs a new {@link Subject} {@code Unit} object.
     * Consists of a parent {@link Subject},
     * the applicable {@code unitId} (typically '3' or '4' for Year 12),
     * as a single character (i.e. '0' to '9' or 'A' to 'Z');
     * a unit {@code title} that may be multiple (optionally capitalised) words,
     * including numbers (in words or digits) and/or Roman numerals (I,IV, etc.),
     * each separated by a SINGLE space, with NO leading or trailing spaces and
     * no trailing full stop (.), but other internal punctuation may be present -
     * ({@code title}s supplied with multiple spaces or leading or trailing spaces
     * must be rectified); AND a {@code description}, in whole English sentences,
     * each beginning with a capital letter and finishing with a full stop.
     *
     * @param subject the parent subject of this unit.
     * @param unitId the single character unit identifier of this unit.
     * @param title the string title of this unit,
     *              consisting of one or more capitalised words
     *              separated by one or more spaces or other punctuation.
     * @param description the string description of this unit, in whole sentences,
     *                    each beginning with a capital and finishing with a full stop,
     *                    with words separated by one or more spaces or other punctuation.
     */
    public Unit(Subject subject, Character unitId, String title, String description) {
        this.subject = subject;
        this.unitId = unitId;
        this.title = title;
        this.description = description;
    }

    /**
     * Gets the parent {@link Subject} of this unit.
     *
     * @return the reference to the unit's parent subject.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Returns the identifier of this unit.
     *
     * @return the identifier of this unit.
     */
    public Character id() {
        return unitId;
    }

    /**
     * Gets the text {@code description} of the unit.
     *
     * @return the string {@code description} of the unit.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a detailed string representation of this unit.
     *
     * @return a detailed string representation of this unit.
     */
    public String getFullDetail() {
        return subject.getTitle() + ": Unit "
                + unitId + ": "
                + title
                + "\n"
                + '"'
                + description
                + '"'
                + "\n";
    }

    /**
     * Returns a brief string representation of this unit.
     *
     * @return the unitID and title of this unit.
     */
    @Override
    public String toString() {
        return subject.getTitle() + ": Unit "
                + unitId + ": "
                + title;
    }
}
