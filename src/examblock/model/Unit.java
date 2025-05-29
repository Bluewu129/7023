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
    private final Subject subject;
    /** The single-character identifier of this Unit. */
    private final Character unitId;
    /** The title of this Unit. */
    private final String title;
    /** The description of the Unit. */
    private final String description;
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

        String[] data = readUnitData(br, nthItem);
        this.subject = registry.find(data[0], Subject.class);
        this.unitId = data[1].charAt(0);
        this.title = cleanTitle(data[2]);
        this.description = data[3];

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

    private String[] readUnitData(BufferedReader br, int nthItem)
            throws IOException, RuntimeException {
        // Read header line: "1. Mathematics Methods: Unit 3: Calculus"
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

        // Parse "Mathematics Methods: Unit 3: Calculus"
        String[] parts = headerParts[1].split(": ");
        if (parts.length < 3) {
            throw new RuntimeException("Invalid unit format: " + headerParts[1]);
        }

        String subjectTitle = parts[0];
        String unitIdPart = parts[1].replace("Unit ", "");
        String unitTitle = parts[2];

        // Read description line
        String descLine = CSSE7023.getLine(br);
        if (descLine == null) {
            throw new RuntimeException("EOF reading Unit #" + nthItem + " description");
        }

        String description = descLine;
        if (description.startsWith("\"") && description.endsWith("\"")) {
            description = description.substring(1, description.length() - 1);
        }

        return new String[]{subjectTitle, unitIdPart, unitTitle, description};
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + subject.getTitle() + ": Unit " + unitId + ": " + title + System.lineSeparator());
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