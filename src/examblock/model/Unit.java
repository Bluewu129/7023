package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An object describing a single-semester Year 12 Unit of a Year 12 Subject.
 * These are typically Unit 3 or Unit 4 for the Year 12 units, but may be different.
 */
public class Unit implements StreamManager, ManageableListItem {

    /** This Unit's parent subject. */
    private Subject subject;
    /** The single-character identifier of this Unit. */
    private Character unitId;
    /** The title of this Unit. */
    private String title;
    /** The description of the Unit. */
    private String description;
    /** The registry reference. */
    private Registry registry;

    /**
     * Constructs a new Subject Unit object.
     * Consists of a parent Subject,
     * the applicable unitId (typically '3' or '4' for Year 12),
     * as a single character (i.e. '0' to '9' or 'A' to 'Z');
     * a unit title that may be multiple (optionally capitalised) words,
     * including numbers (in words or digits) and/or Roman numerals (I,IV, etc.),
     * each separated by a SINGLE space, with NO leading or trailing spaces and
     * no trailing full stop (.), but other internal punctuation may be present -
     * (titles supplied with multiple spaces or leading or trailing spaces
     * must be rectified); AND a description, in whole English sentences,
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
     * @param registry the global object registry, needed to resolve textual Subject names
     */
    public Unit(Subject subject, Character unitId, String title,
                String description, Registry registry) {
        this.subject = subject;
        this.unitId = unitId;
        this.title = title;
        this.description = description;
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Unit.class);
        }
    }

    /**
     * Constructs a Unit by reading a description from a text stream
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Unit(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {

        this.registry = registry;
        this.subject = null;
        this.unitId = '0';
        this.title = "";
        this.description = "";

        streamIn(br, registry, nthItem);

        if (registry != null) {
            registry.add(this, Unit.class);
        }
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
    public void streamOut(BufferedWriter bw, int nthItem)
            throws IOException {
        bw.write(nthItem + ". " + subject.getTitle().toUpperCase()
                + System.lineSeparator());
        bw.write(subject.getTitle() + ", Unit " + unitId + ": " + title
                + System.lineSeparator());
        bw.write("\"" + description + "\""
                + System.lineSeparator());
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
        // "1. ACCOUNTING"
        String heading = CSSE7023.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem);
        }

        String[] bits = heading.split("\\. ", 2);
        if (bits.length != 2) {
            throw new RuntimeException("Invalid unit format: " + heading);
        }

        int index = CSSE7023.toInt(bits[0], "Number format exception parsing Unit "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Unit index out of sync!");
        }

        String subjectTitle = bits[1];

        if (registry != null) {
            this.subject = registry.find(subjectTitle, Subject.class);
            if (this.subject == null) {
                this.subject = new Subject(subjectTitle, "Default description.", registry);
            }
        } else {
            this.subject = new Subject(subjectTitle, "Default description.", null);
        }

        // "Accounting, Unit 3: Managing resources"
        String unitLine = CSSE7023.getLine(br);
        if (unitLine == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem + " details");
        }

        if (unitLine.contains(", Unit ") && unitLine.contains(": ")) {
            String[] parts = unitLine.split(", Unit ");
            if (parts.length == 2) {
                String[] unitParts = parts[1].split(": ", 2);
                if (unitParts.length == 2) {
                    this.unitId = unitParts[0].charAt(0);
                    this.title = unitParts[1];
                } else {
                    throw new RuntimeException("Invalid unit format in line: " + unitLine);
                }
            } else {
                throw new RuntimeException("Invalid unit format in line: " + unitLine);
            }
        } else {
            throw new RuntimeException("Invalid unit format in line: " + unitLine);
        }

        String descLine = CSSE7023.getLine(br);
        if (descLine == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem + " description");
        }

        if (descLine.startsWith("\"") && descLine.endsWith("\"")) {
            this.description = descLine.substring(1, descLine.length() - 1);
        } else {
            this.description = descLine;
        }
    }

    /**
     * Returns a detailed string representation of this unit.
     *
     * @return a detailed string representation of this unit.
     */
    @Override
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
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return an Object[] containing class values suitable for use in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{subject.getTitle(), unitId, title, description
        };
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return subject.getTitle() + "-" + unitId;
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
     * Gets the text description of the unit.
     *
     * @return the string description of the unit.
     */
    public String getDescription() {
        return description;
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
        Unit other = (Unit) o;
        return unitId.equals(other.unitId)
                && subject.equals(other.subject)
                && title.equals(other.title)
                && description.equals(other.description);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return subject.hashCode() + unitId.hashCode() + title.hashCode() + description.hashCode();
    }
}