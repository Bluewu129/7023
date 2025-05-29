package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an exam venue, consisting of one or more {@link Room}s.
 * Note: Venue should NOT inherit from Room - this is a composition relationship, not inheritance.
 * However, to maintain compatibility with existing code structure, we keep the inheritance
 * but handle it carefully.
 */
public class Venue extends Room {

    /** The number of rooms used in the venue; must be one of 1, 2, or 3. */
    private int roomCount;
    /** The list of room IDs that make up this venue. */
    private List<String> roomIds;
    /** The number of rows of Desks, running across the room. */
    private int rows;
    /** number of columns of Desks, running front to rear. */
    private int columns;
    /** The total available Desks (may be less than rows x columns). */
    private int totalDesks;
    /** Whether or not this venue is suitable for AARA exams. */
    private boolean aara;
    /** The registry reference for looking up rooms. */
    private Registry venueRegistry;

    /**
     * Default constructor for factory use.
     */
    public Venue() {
        super("", null); // Don't register the venue as a room
        this.roomCount = 0;
        this.roomIds = new ArrayList<>();
        this.rows = 0;
        this.columns = 0;
        this.totalDesks = 0;
        this.aara = false;
        this.venueRegistry = null;
    }

    /**
     * Constructs a new {@code Venue} object, consisting of one or more {@link Room}s.
     * As per specification - Registry is last parameter
     *
     * @param id the venue identifier
     * @param roomCount the number of rooms (1, 2, or 3)
     * @param rooms the list of rooms
     * @param rows the number of rows of desks
     * @param columns the number of columns of desks
     * @param totalDesks the total number of desks
     * @param aara whether this is an AARA venue
     * @param registry the registry
     */
    public Venue(String id, int roomCount, RoomList rooms,
                 int rows, int columns, int totalDesks, boolean aara, Registry registry) 
        throws IOException, RuntimeException {
        super(id, null); // Don't register the venue as a room
        this.roomCount = roomCount;
        this.roomIds = new ArrayList<>();

        // Store room IDs, not room objects
        for (Room room : rooms.getItems()) {
            this.roomIds.add(room.roomId());
        }

        this.rows = rows;
        this.columns = columns;
        this.totalDesks = totalDesks;
        this.aara = aara;
        this.venueRegistry = registry != null ? registry : rooms.getRegistry();

        if (this.venueRegistry != null) {
            this.venueRegistry.add(this, Venue.class);
        }
    }

    /**
     * Constructs a Venue by reading from a stream.
     * As per specification
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global object registry
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic failure
     */
    public Venue(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        super("temp", null); // Don't register as a room
        this.roomIds = new ArrayList<>();
        this.venueRegistry = registry;
        streamIn(br, registry, nthItem);

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Venue: " + venueId());
        }
    }

    /**
     * Gets the identifier of the venue.
     *
     * @return The identifier of the venue.
     */
    public String venueId() {
        return roomId(); // Use the inherited roomId method
    }

    /**
     * Gets the list of rooms that make up this venue.
     * This creates a temporary RoomList with the actual room objects.
     *
     * @return The list of rooms.
     */
    public RoomList getRooms() {
        RoomList rooms = new RoomList(venueRegistry);
        for (String roomId : roomIds) {
            Room room = venueRegistry.find(roomId, Room.class);
            if (room != null && !(room instanceof Venue)) {
                rooms.add(room);
            }
        }
        return rooms;
    }

    /**
     * Gets the number of rows of {@link Desk}s in this venue.
     *
     * @return The number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns of {@link Desk}s in this venue.
     *
     * @return The number of columns.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Gets the total number of desks in the venue.
     *
     * @return The total number of desks.
     */
    public int deskCount() {
        return totalDesks;
    }

    /**
     * Is this an AARA venue?
     *
     * @return true if this is an AARA venue, false otherwise.
     */
    public boolean isAara() {
        return aara;
    }

    /**
     * Check if the venue type is AARA or not.
     *
     * @param aara whether checking for AARA venue
     * @return true if venue type matches
     */
    public boolean checkVenueType(boolean aara) {
        if (aara) {
            if (this.aara) {
                return true;
            } else {
                System.out.println("This is NOT an AARA venue.");
            }
        } else {
            if (this.aara) {
                System.out.println("This is an AARA venue.");
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if numberStudents will fit in this venue.
     *
     * @param numberStudents the number of students to check
     * @return true if they will fit, false otherwise
     */
    public boolean willFit(int numberStudents) {
        if (totalDesks < numberStudents) {
            System.out.println("This venue only has " + totalDesks + " desks, "
                    + numberStudents + " students will not fit in this venue!");
            return false;
        }
        return true;
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        // Write venue data in the expected format
        bw.write(nthItem + ". " + venueId() + " (" + totalDesks + " ");
        if (aara) {
            bw.write("AARA ");
        } else {
            bw.write("Non-AARA ");
        }
        bw.write("desks)" + System.lineSeparator());

        // Write details line
        bw.write("Room Count: " + roomCount);
        bw.write(", Rooms: ");
        boolean first = true;
        for (String roomId : roomIds) {
            if (!first) bw.write(" ");
            bw.write(roomId);
            first = false;
        }
        bw.write(", Rows: " + rows);
        bw.write(", Columns: " + columns);
        bw.write(", Desks: " + totalDesks);
        bw.write(", AARA: " + aara);
        bw.write(System.lineSeparator());
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws RuntimeException {

        // Read header line: "1. V1 (25 Non-AARA desks)"
        String line = CSSE7023.getLine(br);
        if (line == null) {
            throw new RuntimeException("EOF reading Venue #" + nthItem);
        }

        String[] parts = line.split("\\. ", 2);
        if (parts.length != 2) {
            throw new RuntimeException("Invalid venue header format: " + line);
        }

        int index = CSSE7023.toInt(parts[0], "Number format exception parsing Venue " + nthItem);
        if (index != nthItem) {
            throw new RuntimeException("Venue index out of sync!");
        }

        // Parse "V1 (25 Non-AARA desks)"
        String venueInfo = parts[1];
        String venueId = "";

        // Extract venue ID (everything before the '(')
        int parenIndex = venueInfo.indexOf('(');
        if (parenIndex > 0) {
            venueId = venueInfo.substring(0, parenIndex).trim();
        }

        setId(venueId); // Set the venue ID

        // Check if AARA venue
        this.aara = venueInfo.contains("AARA desks)") && !venueInfo.contains("Non-AARA");

        // Read details line: "Room Count: 1, Rooms: R1, Rows: 5, Columns: 5, Desks: 25, AARA: false"
        String detailsLine = CSSE7023.getLine(br);
        if (detailsLine == null) {
            throw new RuntimeException("EOF reading Venue #" + nthItem + " details");
        }

        // Parse the key-value pairs
        String[] details = detailsLine.split(", ");
        String roomsList = "";

        for (String detail : details) {
            String[] kv = detail.split(": ");
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();

                switch (key) {
                    case "Room Count":
                        roomCount = CSSE7023.toInt(value, "Invalid room count");
                        break;
                    case "Rooms":
                        roomsList = value;
                        break;
                    case "Rows":
                        rows = CSSE7023.toInt(value, "Invalid rows");
                        break;
                    case "Columns":
                        columns = CSSE7023.toInt(value, "Invalid columns");
                        break;
                    case "Desks":
                        totalDesks = CSSE7023.toInt(value, "Invalid total desks");
                        break;
                    case "AARA":
                        aara = CSSE7023.toBoolean(value, "Invalid AARA flag");
                        break;
                }
            }
        }

        // Store room IDs (don't create new rooms!)
        this.roomIds.clear();
        if (!roomsList.isEmpty()) {
            String[] roomIdArray = roomsList.split(" ");
            for (String roomId : roomIdArray) {
                roomId = roomId.trim();
                if (!roomId.isEmpty()) {
                    // Just store the ID, don't create or register rooms
                    this.roomIds.add(roomId);
                }
            }
        }

        // Register the venue (but not as a room!)
        if (registry != null) {
            registry.add(this, Venue.class);
        }
    }

    @Override
    public Object[] toTableRow() {
        return new Object[]{venueId(), roomCount, rows + "x" + columns, totalDesks, aara ? "Yes" : "No"};
    }

    @Override
    public Object[] toLongTableRow() {
        return new Object[]{venueId(), roomCount, roomIds.size(), rows, columns, totalDesks, aara ? "AARA" : "Regular"};
    }

    @Override
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        sb.append("Venue: ").append(venueId()).append("\n");
        sb.append("Rooms: ").append(roomCount).append(" (");
        boolean first = true;
        for (String roomId : roomIds) {
            if (!first) sb.append(", ");
            sb.append(roomId);
            first = false;
        }
        sb.append(")\n");
        sb.append("Layout: ").append(rows).append(" rows x ").append(columns).append(" columns\n");
        sb.append("Total Desks: ").append(totalDesks).append("\n");
        sb.append("Type: ").append(aara ? "AARA" : "Regular").append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return venueId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Venue venue = (Venue) o;
        return venueId().equals(venue.venueId());
    }

    @Override
    public int hashCode() {
        return venueId().hashCode();
    }
}