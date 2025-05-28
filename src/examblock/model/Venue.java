package examblock.model;

/**
 * Represents an exam venue, consisting of one or more {@link Room}s.
 */
public class Venue extends Room {

    /** The number of rooms used in the venue; must be one of 1, 2, or 3. */
    private int roomCount; //
    /** The list of rooms used in the venue - there must be at least one room. */
    private RoomList rooms; // the
    /** The number of rows of Desks, running across the room. */
    private int rows; // the
    /** number of columns of Desks, running front to rear. */
    private int columns; // an optional third (maximum) room object involved in the venue.
    /** The total available Desks (may be less than rows x columns). */
    private int totalDesks; // the .
    /** Whether or not this venue is suitable for AARA exams. */
    private boolean aara; //

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
     */
    public Venue(String id, int roomCount, RoomList rooms,
                 int rows, int columns, int totalDesks, boolean aara) {
        super(id);
        this.roomCount = roomCount;
        // the passed parameter rooms is mutable, so we want our own list as it stands now
        // don't just set our own list to reference the passed parameter rooms!
        int counter = 0;
        this.rooms = new RoomList();
        for (Room room : rooms.all()) {
            counter++;
            this.rooms.addRoom(room);
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
    }

    /**
     * Gets the identifier of the venue.
     *
     * @return The identifier of the venue.
     */
    public String venueId() {
        return id;
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
            System.out.println("This venue only has " + totalDesks + " desks, "
                    + numberStudents + " students will not fit in this venue!");
        } else {
            return true;
        }
        return false;
    }

    /**
     * Returns a string representation of this venue.
     * (Returns the venue identifier.)
     *
     * @return The string representation of this venue.
     */
    @Override
    public String toString() {
        return id;
    }

}
