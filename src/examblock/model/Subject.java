package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents a Year 12 academic subject with title and comprehensive description.
 * Maintains data integrity through automatic title and description sanitization.
 * Serves as core entity for organizing exams, units, and student enrollments.
 */
public class Subject implements StreamManager, ManageableListItem {

    /** Subject title - immutable after creation and sanitization */
    private String title;

    /** Subject description - immutable after creation and sanitization */
    private String description;

    /** Reference to global registry for object management */
    private Registry registry;

    /**
     * Creates a new subject with automatic title and description sanitization.
     * Title may contain multiple capitalized words, numbers, and Roman numerals.
     * Description should be complete sentences with proper punctuation.
     *
     * @param title subject title (capitalized words separated by single spaces)
     * @param description subject description (complete sentences with periods)
     * @param registry global registry for centralized object management
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
     * Creates a subject by deserializing from a text stream.
     * Used for loading subject data from files with comprehensive error handling.
     *
     * @param br BufferedReader opened and positioned at subject data
     * @param registry global object registry for object management
     * @param nthItem expected index of this subject in the serialization stream
     * @throws IOException on stream reading failures
     * @throws RuntimeException on data format or logic errors
     */
    public Subject(BufferedReader br, Registry registry, int nthItem) throws IOException, RuntimeException {
        this.registry = registry;
        this.title = "";
        this.description = "";

        streamIn(br, registry, nthItem);

        if (registry != null) {
            registry.add(this, Subject.class);
        }
    }

    /**
     * Writes subject data to output stream in standardized format.
     *
     * Format structure:
     * 1. SUBJECT_TITLE_UPPERCASE
     * Subject Title in Proper Case  
     * "Complete subject description with proper punctuation."
     *
     * @param bw writer already opened at current file position
     * @param nthItem subject's position in serialization stream for consistency checks
     * @throws IOException on stream writing failures
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + title.toUpperCase() + System.lineSeparator());
        bw.write(title + System.lineSeparator());
        bw.write("\"" + description + "\"" + System.lineSeparator());
    }

    /**
     * Reads subject data from input stream with format validation.
     * Handles both quoted and unquoted description formats for flexibility.
     *
     * @param br reader already opened at subject data position
     * @param registry global object registry (parameter maintained for interface consistency)
     * @param nthItem expected subject index for consistency validation
     * @throws IOException on stream reading failures
     * @throws RuntimeException on data format or consistency errors
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem) throws IOException, RuntimeException {
        // Parse subject header: "1. SUBJECT_TITLE"
        String heading = CSSE7023.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem);
        }

        String[] bits = heading.split("\\. ", 2);
        if (bits.length != 2) {
            throw new RuntimeException("Invalid subject format: " + heading);
        }

        int index = CSSE7023.toInt(bits[0], "Number format exception parsing Subject " + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Subject index out of sync!");
        }

        // Parse actual subject title
        String actualTitle = CSSE7023.getLine(br);
        if (actualTitle == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem + " title");
        }
        this.title = sanitiseTitle(actualTitle.trim());

        // Parse subject description (handle both quoted and unquoted formats)
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
     * Sanitizes subject title according to formatting requirements.
     * Normalizes whitespace to single spaces and removes trailing periods.
     * Preserves capitalization, numbers, and Roman numerals as specified.
     *
     * @param text the title text to sanitize
     * @return cleaned title conforming to formatting standards
     */
    public String sanitiseTitle(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Normalize multiple spaces to single spaces and trim boundaries
        String cleaned = text.trim().replaceAll("\\s+", " ");

        // Remove trailing period if present (other internal punctuation preserved)
        if (cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }

    /**
     * Sanitizes subject description to ensure proper sentence formatting.
     * Normalizes whitespace and ensures description ends with appropriate punctuation.
     *
     * @param text the description text to sanitize
     * @return cleaned description with proper sentence structure
     */
    public String sanitiseDescription(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Normalize whitespace to single spaces
        String cleaned = text.trim().replaceAll("\\s+", " ");

        // Ensure description ends with period for proper sentence structure
        if (!cleaned.isEmpty() && !cleaned.endsWith(".")) {
            cleaned += ".";
        }

        return cleaned;
    }

    /**
     * Creates detailed string representation with title and quoted description.
     * Returns title in uppercase followed by description in quotes on new line.
     *
     * @return formatted detailed representation of this subject
     */
    @Override
    public String getFullDetail() {
        return title.toUpperCase() + "\n\"" + description + "\"\n";
    }

    /**
     * Creates array of values suitable for table display in view components.
     * Provides core subject information for JTable integration.
     *
     * @return array containing title and description for table display
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{title, description};
    }

    /**
     * Provides unique string identifier for registry lookup.
     * Uses title as the unique identifier for this subject.
     *
     * @return the subject title
     */
    @Override
    public String getId() {
        return title;
    }

    /**
     * Gets the subject title.
     *
     * @return the subject title string
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the subject description.
     *
     * @return the subject description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Creates brief string representation showing title in uppercase.
     * Used for general toString() purposes and simple displays.
     *
     * @return subject title in uppercase with newline
     */
    @Override
    public String toString() {
        return this.getTitle().toUpperCase() + "\n";
    }

    /**
     * Implements class-specific equality comparison based on title and description.
     * Two subjects are equal if they have identical title and description fields.
     *
     * @param o the other object to compare
     * @return true if objects represent the same subject content
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
     * Generates hash code based on title and description for consistent hashing.
     *
     * @return hash code derived from title and description
     */
    @Override
    public int hashCode() {
        return title.hashCode() + description.hashCode();
    }
}