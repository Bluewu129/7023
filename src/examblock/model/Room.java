package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents a room in a building that may or may not be in a potential exam venue.
 */
public class Room implements StreamManager, ManageableListItem {

    /**
     * The room identifier (e.g. "A102").
     */
    private String id;

    /**
     * store the registry passed via constructor
     */
    private Registry registry;

    /**
     * Default constructs a {@code Room}.
     * WARNING: This room does not register itself. It is the responsibility of the Venue
     * that is constructing this Room in its streaming constructor to register the room!
     */
    public Room() {
        this.id = Long.toString(System.currentTimeMillis());
        this.registry = null;
    }

    /**
     * Constructs a {@code Room}.
     *
     * @param id       - the String room identifier (e.g. "A102").
     * @param registry - the registry
     */
    public Room(String id, Registry registry) {
        this.id = id;
        this.registry = registry;

        if (registry != null) {
            registry.add(this, Room.class);
        }
    }

    /**
     * Constructs a Room by reading a description from a text stream
     *
     * @param br       BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem  the index number of this serialized object
     * @throws IOException on any read failure
     */
    public Room(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {

        this.registry = registry;
        streamIn(br, registry, nthItem);

        if (registry != null) {
            registry.add(this, Room.class);
        }
    }

    /**
     * Used to write data to the disk.<br>
     * <br>
     * The format of the text written to the stream must be matched exactly by streamIn, so it
     * is very important to format the output as described.<br>
     * <br>
     * 3. R3<br>
     * 4. S101<br>
     * <br>
     *
     * @param bw      writer, already opened. Your data should be written at the current
     *                file position
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + id + System.lineSeparator());
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.<br>
     * <br>
     * For the format of the text in the input stream, refer to the {@code streamOut} documentation.
     *
     * @param br       reader, already opened.
     * @param registry the global object registry
     * @param nthItem  a number representing this item's position in the stream. Used for sanity
     *                 checks
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br,
                         Registry registry,
                         int nthItem)
            throws IOException, RuntimeException {

        // Read line like "1. R1"
        String heading = CSSE7023.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Room #" + nthItem);
        }

        String[] bits = heading.split("\\. ", 2);
        if (bits.length != 2) {
            throw new RuntimeException("Invalid room format: " + heading);
        }

        int index = CSSE7023.toInt(bits[0], "Number format exception parsing Room "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Room index out of sync!");
        }

        id = bits[1].trim();

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Room: " + id);
        }
    }

    /**
     * Returns a detailed string representation of this exam
     *
     * @return a detailed string representation of this exam.
     */
    public String getFullDetail() {
        return toString();
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return array of objects representing one row of data
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{roomId()};
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return this.toString();
    }

    /**
     * Gets the identifier of the room.
     *
     * @return The identifier of the room.
     */
    public String roomId() {
        return id;
    }

    /**
     * required to be protected due to design choices in A1
     *
     * @param id new id for this room
     */
    protected void setId(String id) {
        this.id = id;
    }

    /**
     * Returns a string representation of this room.
     * (Returns the roomId.)
     *
     * @return The string representation of this room.
     */
    @Override
    public String toString() {
        return id;
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
        Room other = (Room) o;
        return other.id.equals(this.id);
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}