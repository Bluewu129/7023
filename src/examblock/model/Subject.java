package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An object describing a single Year 12 Subject.
 * Updated to implement StreamManager and ManageableListItem interfaces.
 */
public class Subject implements StreamManager, ManageableListItem {

    /** The Subject title (immutable). */
    private final String title;
    /** The Subject description (immutable). */
    private final String description;
    /** The registry for this subject. */
    private Registry registry;

    /**
     * Default constructor for factory use.
     */
    public Subject() {
        this.title = "";
        this.description = "";
        this.registry = null;
    }

    /**
     * Constructs a new Year 12 Subject object.
     *
     * @param title the string title of this subject
     * @param description the string description of this subject
     */
    public Subject(String title, String description) {
        this.title = cleanTitle(title);
        this.description = description;
        this.registry = null;
    }

    /**
     * Constructs a Subject with registry.
     *
     * @param title the string title of this subject
     * @param description the string description of this subject
     * @param registry the registry to register with
     */
    public Subject(String title, String description, Registry registry) {
        this.title = cleanTitle(title);
        this.description = description;
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Subject.class);
        }
    }

    /**
     * Constructs a Subject by reading from a stream.
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global object registry
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic failure
     */
    public Subject(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;

        String[] data = readSubjectData(br, nthItem);
        this.title = cleanTitle(data[0]);
        this.description = data[1];

        if (registry != null) {
            registry.add(this, Subject.class);
        }

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Subject: " + title);
        }
    }

    /**
     * Cleans the title by removing extra spaces and formatting correctly.
     *
     * @param rawTitle the raw title string
     * @return the cleaned title
     */
    private String cleanTitle(String rawTitle) {
        if (rawTitle == null) {
            return "";
        }
        // Remove leading/trailing spaces and compress multiple spaces to single space
        return rawTitle.trim().replaceAll("\\s+", " ");
    }

    /**
     * Reads subject data from the stream.
     *
     * @param br the BufferedReader
     * @param nthItem the item number
     * @return array containing [title, description]
     * @throws IOException on read error
     * @throws RuntimeException on format error
     */
    private String[] readSubjectData(BufferedReader br, int nthItem)
            throws IOException, RuntimeException {
        // Read header line: "1. ACCOUNTING"
        String headerLine = CSSE7023.getLine(br);
        if (headerLine == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem);
        }

        String[] headerParts = headerLine.split("\\. ", 2);
        if (headerParts.length != 2) {
            throw new RuntimeException("Invalid subject header format: " + headerLine);
        }

        int index = CSSE7023.toInt(headerParts[0],
                "Number format exception parsing Subject " + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Subject index out of sync! Expected " + nthItem +
                    " but got " + index);
        }

        String title = headerParts[1].trim();

        // Read the second line with the actual subject name
        String titleLine = CSSE7023.getLine(br);
        if (titleLine == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem + " title");
        }

        // The actual title is on this line
        String actualTitle = titleLine.trim();

        // Read description line: "The study of..."
        String descLine = CSSE7023.getLine(br);
        if (descLine == null) {
            throw new RuntimeException("EOF reading Subject #" + nthItem + " description");
        }

        // Remove quotes from description
        String description = descLine.trim();
        if (description.startsWith("\"") && description.endsWith("\"")) {
            description = description.substring(1, description.length() - 1);
        }

        return new String[]{actualTitle, description};
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + title.toUpperCase() + System.lineSeparator());
        bw.write(title + System.lineSeparator());
        bw.write("\"" + description + "\"" + System.lineSeparator());
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        // This method is implemented in the constructor
        throw new UnsupportedOperationException("Use constructor instead");
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

    @Override
    public String getFullDetail() {
        return title.toUpperCase() + "\n\"" + description + "\"\n";
    }

    @Override
    public Object[] toTableRow() {
        return new Object[]{title, description};
    }

    @Override
    public Object[] toLongTableRow() {
        return new Object[]{title, description, title.length(), description.length()};
    }

    @Override
    public String getId() {
        return title;
    }

    /**
     * Returns a brief string representation of this subject.
     *
     * @return the subject title in all uppercase and a newline.
     */
    @Override
    public String toString() {
        return title.toUpperCase() + "\n";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Subject subject = (Subject) obj;
        return title.equals(subject.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }
}