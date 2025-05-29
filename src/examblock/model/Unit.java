package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An object describing a single-semester Year 12 Unit of a Year 12 Subject.
 * Updated to implement StreamManager and ManageableListItem interfaces.
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
    /** The registry for dependencies. */
    private Registry registry;

    /**
     * Default constructor for factory use.
     */
    public Unit() {
        this.subject = null;
        this.unitId = '1';
        this.title = "";
        this.description = "";
        this.registry = null;
    }

    /**
     * Constructs a new {@link Subject} {@code Unit} object.
     */
    public Unit(Subject subject, Character unitId, String title, String description) {
        this.subject = subject;
        this.unitId = unitId;
        this.title = cleanTitle(title);
        this.description = description;
        this.registry = null;
    }

    /**
     * Constructs a Unit with registry.
     */
    public Unit(Subject subject, Character unitId, String title, String description, Registry registry) {
        this.subject = subject;
        this.unitId = unitId;
        this.title = cleanTitle(title);
        this.description = description;
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Unit.class);
        }
    }

    /**
     * Constructs a Unit by reading from a stream.
     */
    public Unit(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        this.registry = registry;

        readUnitData(br, registry, nthItem);

        if (registry != null) {
            registry.add(this, Unit.class);
        }

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Unit: " + subject.getTitle() + " Unit " + unitId);
        }
    }

    private String cleanTitle(String rawTitle) {
        if (rawTitle == null) {
            return "";
        }
        return rawTitle.trim().replaceAll("\\s+", " ");
    }

    private void readUnitData(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        // Read header line: "1. ACCOUNTING"
        String headerLine = CSSE7023.getLine(br);
        if (headerLine == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem);
        }

        String[] headerParts = headerLine.split("\\. ", 2);
        if (headerParts.length != 2) {
            throw new RuntimeException("Invalid unit header format: " + headerLine);
        }

        int index = CSSE7023.toInt(headerParts[0],
                "Number format exception parsing Unit " + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Unit index out of sync!");
        }

        // The subject name in uppercase
        String subjectNameUpper = headerParts[1].trim();

        // Read the actual unit line: "Accounting, Unit 3: Managing resources"
        String unitLine = CSSE7023.getLine(br);
        if (unitLine == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem + " details");
        }

        // Parse "Accounting, Unit 3: Managing resources"
        String[] parts = unitLine.split(", Unit ");
        if (parts.length != 2) {
            throw new RuntimeException("Invalid unit format: " + unitLine);
        }

        String subjectTitle = parts[0].trim();

        // Parse "3: Managing resources"
        String unitPart = parts[1];
        String[] unitParts = unitPart.split(": ", 2);
        if (unitParts.length != 2) {
            throw new RuntimeException("Invalid unit details: " + unitPart);
        }

        this.unitId = unitParts[0].trim().charAt(0);
        this.title = unitParts[1].trim();

        // Find the subject
        this.subject = registry.find(subjectTitle, Subject.class);
        if (this.subject == null) {
            throw new RuntimeException("Subject not found for unit: " + subjectTitle);
        }

        // Read description line
        String descLine = CSSE7023.getLine(br);
        if (descLine == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem + " description");
        }

        // Remove quotes from description
        this.description = descLine.trim();
        if (this.description.startsWith("\"") && this.description.endsWith("\"")) {
            this.description = this.description.substring(1, this.description.length() - 1);
        }
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + subject.getTitle().toUpperCase() + System.lineSeparator());
        bw.write(subject.getTitle() + ", Unit " + unitId + ": " + title + System.lineSeparator());
        bw.write("\"" + description + "\"" + System.lineSeparator());
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        throw new UnsupportedOperationException("Use constructor instead");
    }

    /**
     * Gets the parent {@link Subject} of this unit.
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Returns the identifier of this unit.
     */
    public Character id() {
        return unitId;
    }

    /**
     * Gets the text {@code description} of the unit.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the title of this unit.
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String getFullDetail() {
        return subject.getTitle() + ": Unit " + unitId + ": " + title + "\n"
                + "\"" + description + "\"\n";
    }

    @Override
    public Object[] toTableRow() {
        return new Object[]{subject.getTitle(), unitId, title};
    }

    @Override
    public Object[] toLongTableRow() {
        return new Object[]{subject.getTitle(), unitId, title, description, title.length()};
    }

    @Override
    public String getId() {
        return subject.getTitle() + ":" + unitId;
    }

    @Override
    public String toString() {
        return subject.getTitle() + ": Unit " + unitId + ": " + title;
    }
}