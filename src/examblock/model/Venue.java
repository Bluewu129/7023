package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Represents an exam venue, consisting of one or more {@link Room}s.
 */
public class Venue extends Room {

    /** The number of rooms used in the venue; must be one of 1, 2, or 3. */
    private int roomCount;
    /** The list of rooms used in the venue - there must be at least one room. */
    private RoomList rooms;
    /** The number of rows of Desks, running across the room. */
    private int rows;
    /** number of columns of Desks, running front to rear. */
    private int columns;
    /** The total available Desks (may be less than rows x columns). */
    private int totalDesks;
    /** Whether or not this venue is suitable for AARA exams. */
    private boolean aara;

    /**
     * Default constructor for factory use.
     */
    public Venue() {
        super("", new RegistryImpl()); // Provide required parameters for Room constructor
        this.roomCount = 0;
        this.rooms = new RoomList(new RegistryImpl());
        this.rows = 0;
        this.columns = 0;
        this.totalDesks = 0;
        this.aara = false;
    }

    /**
     * Constructs a new {@code Venue} object, consisting of one or more {@link Room}s.
     */
    public Venue(String id, int roomCount, RoomList rooms,
                 int rows, int columns, int totalDesks, boolean aara) {
        super(id, new RegistryImpl()); // Use the Room constructor that takes id and registry
        this.roomCount = roomCount;

        
        this.rooms = new RoomList(rooms.getRegistry());
        int counter = 0;
        for (Room room : rooms.getItems()) {
            counter++;
            this.rooms.add(room);
        }

        this.rows = rows;
        this.columns = columns;
        this.totalDesks = totalDesks;

        // Validation logic
        if (counter < 1) {
            System.out.println("Venue has no rooms!");
            System.out.println("Therefore has no desks! Resetting all desk values to zero.");
            this.rows = 0;
            this.columns = 0;
            this.totalDesks = 0;
        }
        if (counter != roomCount) {
            System.out.println("Venue called with " + roomCount + " roomCount, but with "
                    + counter + " rooms!");
            System.out.println("Resetting roomCount to " + counter + " rooms!");
            this.roomCount = counter;
        }
        counter = rows * columns;
        if (totalDesks > counter) {
            System.out.println("Venue called with " + totalDesks + " totalDesks, but with only "
                    + counter + " desks!");
            System.out.println("Resetting totalDesks to " + counter + "!");
            this.totalDesks = counter;
        }
        this.aara = aara;
    }

    /**
     * Constructs a Venue by reading from a stream.
     */
    public Venue(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        super("temp", registry); // Temporary ID, will be set by streamIn
        this.rooms = new RoomList(registry);
        streamIn(br, registry, nthItem);

        if (registry != null) {
            registry.add(this, Venue.class);
        }

        if (Verbose.isVerbose()) {
            System.out.println("Loaded Venue: " + venueId());
        }
    }

    /**
     * Gets the identifier of the venue.
     */
    public String venueId() {
        return roomId(); // Use the inherited roomId method
    }

    /**
     * Gets the list of rooms that make up this venue.
     */
    public RoomList getRooms() {
        return rooms;
    }

    /**
     * Gets the number of rows of {@link Desk}s in this venue.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns of {@link Desk}s in this venue.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Gets the total number of desks in the venue.
     */
    public int deskCount() {
        return totalDesks;
    }

    /**
     * Is this an AARA venue?
     */
    public boolean isAara() {
        return aara;
    }

    /**
     * Check if the venue type is AARA or not.
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
        // Write venue data in format:
        // 1. V1 2 R101,R102 10 8 80 false
        StringBuilder sb = new StringBuilder();
        sb.append(nthItem).append(". ");
        sb.append(venueId()).append(" ");
        sb.append(roomCount).append(" ");

        // Write room IDs
        boolean first = true;
        for (Room room : rooms.getItems()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(room.roomId());
            first = false;
        }
        sb.append(" ");
        sb.append(rows).append(" ");
        sb.append(columns).append(" ");
        sb.append(totalDesks).append(" ");
        sb.append(aara);

        bw.write(sb.toString() + System.lineSeparator());
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {

        String line = CSSE7023.getLine(br);
        if (line == null) {
            throw new RuntimeException("EOF reading Venue #" + nthItem);
        }

        String[] parts = line.split("\\. ", 2);
        if (parts.length != 2) {
            throw new RuntimeException("Invalid venue format: " + line);
        }

        int index = CSSE7023.toInt(parts[0], "Number format exception parsing Venue " + nthItem);
        if (index != nthItem) {
            throw new RuntimeException("Venue index out of sync!");
        }

        // Parse: V1 2 R101,R102 10 8 80 false
        String[] data = parts[1].split(" ");
        if (data.length < 6) {
            throw new RuntimeException("Invalid venue data format");
        }

        setId(data[0]); // Venue ID
        roomCount = CSSE7023.toInt(data[1], "Invalid room count");

        // Parse room IDs
        String[] roomIds = data[2].split(",");
        this.rooms = new RoomList(registry);
        for (String roomId : roomIds) {
            Room room = new Room(roomId.trim(), registry);
            this.rooms.add(room);
        }

        rows = CSSE7023.toInt(data[3], "Invalid rows");
        columns = CSSE7023.toInt(data[4], "Invalid columns");
        totalDesks = CSSE7023.toInt(data[5], "Invalid total desks");
        aara = data.length > 6 ? CSSE7023.toBoolean(data[6], "Invalid AARA flag") : false;
    }

    @Override
    public Object[] toTableRow() {
        return new Object[]{venueId(), roomCount, rows + "x" + columns, totalDesks, aara ? "Yes" : "No"};
    }

    @Override
    public Object[] toLongTableRow() {
        return new Object[]{venueId(), roomCount, rooms.size(), rows, columns, totalDesks, aara ? "AARA" : "Regular"};
    }

    @Override
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        sb.append("Venue: ").append(venueId()).append("\n");
        sb.append("Rooms: ").append(roomCount).append(" (");
        boolean first = true;
        for (Room room : rooms.getItems()) {
            if (!first) sb.append(", ");
            sb.append(room.roomId());
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
}