package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An object describing a single Year 12 Subject.
 */
public class Subject implements StreamManager, ManageableListItem {

    /** The Subject title (immutable). */
    private String title;
    /** The Subject description (immutable). */
    private String description;
    /** The registry reference. */
    private Registry registry;

    /**
     * Constructs a new Year 12 Subject object.
     * Consists of a {@code title} that may be multiple capitalised words,
     * including numbers (in words or digits) and/or Roman numerals (I,IV, etc.),
     * each separated by a SINGLE space, with NO leading or trailing spaces and
     * no trailing full stop (.), but other internal punctuation may be present -
     * ({@code title}s supplied with multiple spaces or leading or trailing spaces
     * must be rectified); AND a {@code description}, in whole English sentences,
     * each beginning with a capital letter and finishing with a full stop.
     *
     * @param title the string title of this subject,
     *              consisting of one or more capitalised words
     *              separated by one or more spaces or other punctuation.
     * @param description the string description of this subject, in whole sentences,
     *                    each beginning with a capital and finishing with a full stop,
     *                    with words separated by one or more spaces or other punctuation.
     * @param registry the global object registry, needed to resolve textual Subject names
     */
    public Subject(String title, String description, Registry registry) {
        this.title = sanitiseTitle(title);
        this.description = sanitiseDescription(description);
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Subject.class);
        }
    }

    /**
     * Constructs a Subject by reading a description from a text stream
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Subject(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {

        this.registry = registry;
        this.title = "";
        this.description = "";

        streamIn(br, registry, nthItem);

        if (registry != null) {
            registry.add(this, Subject.class);
        }
    }

    /**
     * Used to write data to the disk.
     *
     * The format of the text written to the stream must be matched exactly by streamIn, so it
     * is very important to format the output as described.
     *
     * 1. ACCOUNTING
     * Accounting
     * "The study of the management of financial resources of the public sector, businesses,
     * and individuals."
     *
     * @param bw writer, already opened. Your data should be written at the current
     *           file position
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + title.toUpperCase() + System.lineSeparator());
        bw.write(title + System.lineSeparator());
        bw.write("\"" + description + "\"" + System.lineSeparator());
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
            throw new RuntimeException("EOF reading Subject #" + nthItem);
        }

        String[] bits = heading.split("\\. ", 2);
        if (bits.length != 2) {
            throw new RuntimeException("Invalid subject format: " + heading);
        }

        int index = CSSE7023.toInt(bits[0], "Number format exception parsing Subject "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Subject index out of sync!");
        }

        String actualTitle = CSSE7023.getLine(br);
        if (actualTitle == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem + " title");
        }
        this.title = sanitiseTitle(actualTitle.trim());

        String descLine = CSSE7023.getLine(br);
        if (descLine == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem + " description");
        }

        if (descLine.startsWith("\"") && descLine.endsWith("\"") && descLine.length() >= 2) {
            this.description = sanitiseDescription(descLine.substring(1, descLine.length() - 1));
        } else {
            this.description = sanitiseDescription(descLine);
        }
    }

    /**
     * Returns a detailed string representation of this subject.
     * Returns the {@code title} in all uppercase, then on a new line,
     * the entire text {@code description} inside double quotes.
     *
     * @return a string representation of this subject.
     */
    @Override
    public String getFullDetail() {
        String title = this.getTitle().toUpperCase();
        return title
                + "\n"
                + '"'
                + this.getDescription()
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
        return new Object[]{title, description
        };
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return title;
    }

    /**
     * Return a string from the input string, following these rules:
     * - there may be multiple capitalised words,
     * - including numbers (in words or digits) and/or Roman numerals (I,IV, etc.),
     * - each separated by a SINGLE space,
     * - with NO leading or trailing spaces, and
     * - no trailing full stop (.), but other internal punctuation may be present
     *
     * @param text - the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseTitle(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String cleaned = text.trim().replaceAll("\\s+", " ");

        if (cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }

    /**
     * Return the string description of this subject, in whole sentences,
     * each beginning with a capital and finishing with a full stop,
     * with words separated by one or more spaces or other punctuation.
     *
     * @param text - the string to sanitise
     * @return the sanitised string
     */
    public String sanitiseDescription(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String cleaned = text.trim().replaceAll("\\s+", " ");

        if (!cleaned.isEmpty() && !cleaned.endsWith(".")) {
            cleaned += ".";
        }

        return cleaned;
    }

    /**
     * Gets the {@code title} of this subject.
     *
     * @return the String title of this subject.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the text {@code description} of this subject.
     *
     * @return the String description of this subject.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a brief string representation of this subject.
     * Returns the subject {@code title} in all uppercase.
     *
     * @return the subject title as a String in all uppercase and a newline.
     */
    @Override
    public String toString() {
        return this.getTitle().toUpperCase() + "\n";
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
        Subject other = (Subject) o;
        return title.equals(other.title) && description.equals(other.description);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return title.hashCode() + description.hashCode();
    }
}