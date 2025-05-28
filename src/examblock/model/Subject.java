package examblock.model;

/**
 * An object describing a single Year 12 Subject.
 */
public class Subject {

    /** The Subject title (immutable). */
    private final String title;
    /** The Subject description (immutable). */
    private final String description;

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
     */
    public Subject(String title, String description) {
        this.title = title;
        this.description = description;
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
     * Returns a detailed string representation of this subject.
     * Returns the {@code title} in all uppercase, then on a new line,
     * the entire text {@code description} inside double quotes.
     *
     * @return a string representation of this subject.
     */
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
     * Returns a brief string representation of this subject.
     * Returns the subject {@code title} in all uppercase.
     *
     * @return the subject title as a String in all uppercase and a newline.
     */
    @Override
    public String toString() {
        return this.getTitle().toUpperCase() + "\n";
    }
}
