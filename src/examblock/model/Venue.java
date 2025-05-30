package examblock.model;

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
    /** The registry reference. */
    private Registry registry;

    /**
     * Constructs a new {@code Venue} object, consisting of one or more {@link Room}s.
     * Where a venue consists of multiple rooms, these are typically contiguous
     * with the room-dividers removed to make a single large venue.
     * Seating plans for venues ARE DIFFERENT to the plans for the individual rooms
     * and SOME joined rooms MAY fit more desks than the individual rooms would have.
     *
     * @param id a String identifier for the venue (e.g. "E101" or "L1+L2").
     * @param roomCount the number of rooms used in the venue; must be one of 1, 2, or 3.
     * @param rooms the list of room objects - there must be at least one room.
     * @param rows the number of rows of Desks, rows run across the room, counted front to back.
     * @param columns the number of columns of Desks, running front to back, counted left to right.
     * @param totalDesks the total available Desks (may be less than rows x columns).
     * @param aara the venue is to be used for AARA exam sessions.
     * @param registry the global object registry, needed to resolve textual Subject names
     */
    public Venue(String id, int roomCount, RoomList rooms,
                 int rows, int columns, int totalDesks, boolean aara, Registry registry) {
        super(id, registry);
        this.roomCount = roomCount;
        this.registry = registry;
        int counter = 0;
        this.rooms = new RoomList(registry);
        for (Room room : rooms.all()) {
            counter++;
            this.rooms.add(room);
        }
        this.rows = rows;
        this.columns = columns;
        this.totalDesks = totalDesks;

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

        if (registry != null) {
            registry.add(this, Venue.class);
        }
    }

    /**
     * Constructs a Venue by reading a description from a text stream
     *
     * @param br BufferedReader opened and ready to read from
     * @param registry the global object registry, needed to resolve textual Subject names
     * @param nthItem the index number of this serialized object
     * @throws IOException on any read failure
     * @throws RuntimeException on any logic related issues
     */
    public Venue(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        super();

        this.registry = registry;
        this.roomCount = 1;
        this.rooms = new RoomList(registry);
        this.rows = 0;
        this.columns = 0;
        this.totalDesks = 0;
        this.aara = false;

        streamIn(br, registry, nthItem);

        if (registry != null) {
            registry.add(this, Venue.class);
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
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write(nthItem + ". " + venueId() + " (" + totalDesks + " desks)"
                + System.lineSeparator());
        bw.write("Room Count: " + roomCount + ", Rooms: ");
        for (Room room : rooms.all()) {
            bw.write(room.roomId() + " ");
        }
        bw.write(", Rows: " + rows + ", Columns: " + columns
                + ", Desks: " + totalDesks + ", AARA: " + aara + System.lineSeparator());
    }

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.
     *
     * For the format of the text in the input stream, refer to the {@code streamOut} documentation.
     *
     * @param br reader, already opened.
     * @param registry the global object registry
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws RuntimeException on any logic related issues
     */
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws RuntimeException {
        //  "1. V1 (25 Non-AARA desks)"
        String heading = CSSE7023.getLine(br);
        if (heading == null) {
            throw new RuntimeException("EOF reading Venue #" + nthItem);
        }

        String[] bits = heading.split("\\. ", 2);
        if (bits.length != 2) {
            throw new RuntimeException("Invalid venue format: " + heading);
        }

        int index = CSSE7023.toInt(bits[0], "Number format exception parsing Venue "
                + nthItem + " header");
        if (index != nthItem) {
            throw new RuntimeException("Venue index out of sync!");
        }

        //  "V1 (25 Non-AARA desks)"
        String venueInfo = bits[1];
        if (venueInfo.contains(" (") && venueInfo.contains(" desks)")) {
            String[] venueParts = venueInfo.split(" \\(");
            this.setId(venueParts[0]);
            String deskInfo = venueParts[1].replace(" desks)", "");

            String[] deskParts = deskInfo.split(" ");
            if (deskParts.length >= 1) {
                try {
                    this.totalDesks = CSSE7023.toInt(deskParts[0],
                            "Invalid desk count: " + deskParts[0]);
                } catch (Exception e) {
                    System.err.println("Error parsing desk count from: " + deskInfo);
                    throw new RuntimeException("Invalid desk count format in: " + venueInfo);
                }
            } else {
                throw new RuntimeException("Invalid venue format in line: " + heading);
            }
        } else {
            throw new RuntimeException("Invalid venue format in line: " + heading);
        }

        String detailLine = CSSE7023.getLine(br);
        if (detailLine == null) {
            throw new RuntimeException("EOF reading Venue #" + nthItem + " details");
        }

        // "Room Count: 1, Rooms: R1, Rows: 5, Columns: 5, Desks: 25, AARA: false"
        String[] details = detailLine.split(", ");
        for (String detail : details) {
            String[] kv = CSSE7023.keyValuePair(detail);
            if (kv == null || kv.length != 2) {
                continue;
            }

            switch (kv[0]) {
                case "Room Count":
                    this.roomCount = CSSE7023.toInt(kv[1], "Invalid room count: " + kv[1]);
                    break;
                case "Rooms":
                    String[] roomIds = kv[1].trim().split(" ");
                    this.rooms = new RoomList(registry);
                    for (String roomId : roomIds) {
                        if (!roomId.trim().isEmpty()) {
                            roomId = roomId.trim();

                            Room existingRoom = registry.find(roomId, Room.class);

                            if (existingRoom != null) {
                                this.rooms.add(existingRoom);
                            } else {
                                Room newRoom = new Room(roomId, registry);
                                this.rooms.add(newRoom);
                            }
                        }
                    }
                    break;
                case "Rows":
                    this.rows = CSSE7023.toInt(kv[1], "Invalid rows: " + kv[1]);
                    break;
                case "Columns":
                    this.columns = CSSE7023.toInt(kv[1], "Invalid columns: " + kv[1]);
                    break;
                case "Desks":
                    int desks = CSSE7023.toInt(kv[1], "Invalid desks: " + kv[1]);
                    if (desks != this.totalDesks) {
                        System.out.println("Warning: Desk count mismatch. First line: "
                                + this.totalDesks + ", Detail line: " + desks);
                        this.totalDesks = desks;
                    }
                    break;
                case "AARA":
                    this.aara = CSSE7023.toBoolean(kv[1], "Invalid AARA value: " + kv[1]);
                    break;
            }
        }

        if (this.rooms == null || this.rooms.size() == 0) {
            this.rooms = new RoomList(registry);

            String defaultRoomId = this.venueId();
            Room defaultRoom = registry.find(defaultRoomId, Room.class);
            if (defaultRoom == null) {
                defaultRoom = new Room(defaultRoomId, registry);
            }
            this.rooms.add(defaultRoom);
            this.roomCount = 1;
        }

        System.out.println("Loaded Venue: " + this.venueId() + " with "
                + this.totalDesks + " desks, AARA: " + this.aara);
    }

    /**
     * Returns a detailed string representation of this venue.
     *
     * @return a detailed string representation of this venue.
     */
    @Override
    public String getFullDetail() {
        StringBuilder detail = new StringBuilder();
        detail.append("Venue: ").append(venueId()).append("\n");
        detail.append("Room Count: ").append(roomCount).append("\n");
        detail.append("Rooms: ");
        for (Room room : rooms.all()) {
            detail.append(room.roomId()).append(" ");
        }
        detail.append("\n");
        detail.append("Layout: ").append(rows).append(" rows x ")
                .append(columns).append(" columns\n");
        detail.append("Total Desks: ").append(totalDesks).append("\n");
        detail.append("AARA Suitable: ").append(aara ? "Yes" : "No").append("\n");
        return detail.toString();
    }

    /**
     * return an Object[] containing class values suitable for use in the view model
     *
     * @return an Object[] containing class values suitable for use in the view model
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{venueId(), roomCount, rows, columns, totalDesks, aara
        };
    }

    /**
     * Return a unique string identifying us
     *
     * @return a unique string identifying us
     */
    @Override
    public String getId() {
        return venueId();
    }

    /**
     * Gets the identifier of the venue.
     *
     * @return The identifier of the venue.
     */
    public String venueId() {
        return roomId();
    }

    /**
     * Gets the list of rooms that make up this venue.
     *
     * @return The list of rooms that make up this venue.
     */
    public RoomList getRooms() {
        return rooms;
    }

    /**
     * Gets the number of rows of {@link Desk}s in this venue.
     *
     * @return The number of rows of desks in this venue.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns of {@link Desk}s in this venue.
     *
     * @return The number of columns of desks in this venue.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Gets the total number of desks in the venue (may be less than rows x columns).
     *
     * @return The total number of desks in the venue.
     */
    public int deskCount() {
        return totalDesks;
    }

    /**
     * Is this an AARA venue?
     *
     * @return True if this is an AARA venue.
     */
    public boolean isAara() {
        return aara;
    }

    /**
     * Check if the venue type is AARA or not.
     * Print the appropriate message if the type doesn't match.
     * Messages are:
     * This is an AARA venue.
     * This is NOT an AARA venue.
     *
     * @param aara the venue is to be used for AARA exam sessions.
     * @return True if this venue is the same AARA type as the parameter.
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
     * Checks if numberStudents will fit in this venue. Otherwise, print the message:
     * This venue only has (totalDesks) desks, (numberStudents) students will not fit in this venue!
     *
     * @param numberStudents the number of students to test if they can fit in this venue.
     * @return True if numberStudents will fit in this venue.
     */
    public boolean willFit(int numberStudents) {
        if (totalDesks < numberStudents) {

            String message = "This venue only has " + totalDesks + " desks, "
                    + numberStudents + " students will not fit in this venue!";
            examblock.view.components.DialogUtils.showMessage(message);

            System.out.println(message);

            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of this venue.
     * (Returns the venue identifier.)
     *
     * @return The string representation of this venue.
     */
    @Override
    public String toString() {
        return venueId();
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
        Venue other = (Venue) o;
        return venueId().equals(other.venueId())
                && roomCount == other.roomCount
                && rows == other.rows
                && columns == other.columns
                && totalDesks == other.totalDesks
                && aara == other.aara;
    }

    /**
     * return the hash value of this object
     *
     * @return the hash value of this object
     */
    @Override
    public int hashCode() {
        return venueId().hashCode() + roomCount + rows + columns + totalDesks + (aara ? 1 : 0);
    }
}