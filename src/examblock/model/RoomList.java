package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A collection object for holding and managing {@link Room}s.
 */
public class RoomList extends ListManager<Room> {

    /**
     * construtor
     *
     * @param registry registry
     */
    public RoomList(Registry registry) {
        //
        super(Room::new, registry, Room.class);
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key the text used to identify the item
     * @return the item if found or null
     */
    @Override
    public Room find(String key) {
        Optional<Room> p = all()
                .stream()
                .filter(pn -> pn.getId().equals(key))
                .findFirst();

        return p.orElse(null);
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key the text used to identify the item
     * @return the item if found
     * @throws IllegalStateException if no item is found
     */
    @Override
    public Room get(String key) throws IllegalStateException {
        Room p = find(key);
        if (p != null) {
            return p;
        }
        throw new IllegalStateException("Item with ID " + key + " not found for type Room");
    }

    /**
     * Get the first {@link Room} with a matching {@code id}.
     *
     * @param id - the {@code id} of the {@link Room} to be found.
     * @return The first {@link Room} with a matching {@code id}, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *                               find a matching room as that indicates there is a misalignment
     *                               of the executing state and the complete list of possible rooms.
     */
    public Room getRoom(String id) throws IllegalStateException {
        for (Room room : this.getItems()) {
            if (room.roomId().equals(id)) {
                return room;
            }
        }
        throw new IllegalStateException("No such room!");
    }

    /**
     * Returns detailed string representations of the contents of this room list.
     *
     * @return Detailed string representations of the contents of this room list.
     */
    public String getFullDetail() {

        StringBuilder roomStrings = new StringBuilder();
        int counter = 1;
        for (Room room : this.getItems()) {
            roomStrings.append(counter);
            roomStrings.append(". ");
            roomStrings.append(room.getFullDetail());
            roomStrings.append("\n");
            counter += 1;
        }
        return roomStrings + "\n";
    }

    /**
     * Returns a brief string representation of the contents of this room list.
     *
     * @return A brief string representation of the contents of this room list.
     */
    @Override
    public String toString() {

        StringBuilder roomStrings = new StringBuilder();
        int counter = 1;
        for (Room room : this.getItems()) {
            roomStrings.append(counter);
            roomStrings.append(". ");
            roomStrings.append(room.toString());
            roomStrings.append("\n");
            counter += 1;
        }
        return roomStrings.toString();
    }

    /**
     * Writes the list to disk, including a header like "[Exams: N]".
     *
     * @param bw      writer, already opened
     * @param nthItem position in the stream
     * @throws IOException if a stream error occurs
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {

        // We have to count and then stream out only the rooms that are not the superclass of
        // a Venue. This is because venues store their their ID in their super()'s ID field,

        List<Room> rooms = new ArrayList<>();

        for (int i = 0; i < this.getItems().size(); i++) {
            if (this.getItems().get(i) instanceof Venue) {
                continue;
            }

            rooms.add(this.getItems().get(i));
        }

        bw.write("[Rooms: " + rooms.size() + "]" + System.lineSeparator());

        int index = 1;
        for (Room item : rooms) {
            item.streamOut(bw, index++);
        }

        bw.write(System.lineSeparator());

        if (Verbose.isVerbose()) {
            System.out.println("Wrote " + size() + " rooms successfully");
        }
    }
}
