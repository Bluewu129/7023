package examblock.model;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class VenueTest {
    private Registry registry;
    private RoomList rooms;
    private Room room1;
    private Room room2;
    private Room room3;

    @Before
    public void setUp() {
        registry = new RegistryImpl();
        rooms = new RoomList(registry);
        room1 = new Room("R1", registry);
        room2 = new Room("R2", registry);
        room3 = new Room("R3", registry);
        rooms.add(room1);
        rooms.add(room2);
        rooms.add(room3);
    }

    @Test
    public void testVenueCreationSingleRoom() {
        Venue venue = new Venue("V1", 1, rooms, 5, 5, 25, false);
        assertEquals("V1", venue.venueId());
        assertEquals(5, venue.getRows());
        assertEquals(5, venue.getColumns());
        assertEquals(25, venue.deskCount());
        assertFalse(venue.isAara());
    }

    @Test
    public void testVenueCreationMultipleRooms() {
        Venue venue = new Venue("V1+V2+V3", 3, rooms, 5, 15, 75, true);
        assertEquals("V1+V2+V3", venue.venueId());
        assertEquals(5, venue.getRows());
        assertEquals(15, venue.getColumns());
        assertEquals(75, venue.deskCount());
        assertTrue(venue.isAara());
    }

    @Test
    public void testVenueCreationWithFewerDesks() {
        // Test when total desks is less than rows * columns
        Venue venue = new Venue("V1", 1, rooms, 5, 5, 20, false);
        assertEquals(20, venue.deskCount());
        assertEquals(5, venue.getRows());
        assertEquals(5, venue.getColumns());
    }

    @Test
    public void testIsAara() {
        Venue aaraVenue = new Venue("V1", 1, rooms, 3, 3, 9, true);
        Venue nonAaraVenue = new Venue("V2", 1, rooms, 3, 3, 9, false);

        assertTrue(aaraVenue.isAara());
        assertFalse(nonAaraVenue.isAara());
    }

    @Test
    public void testCheckVenueType() {
        Venue aaraVenue = new Venue("V1", 1, rooms, 3, 3, 9, true);
        Venue nonAaraVenue = new Venue("V2", 1, rooms, 3, 3, 9, false);

        // AARA venue with AARA requirement
        assertTrue(aaraVenue.checkVenueType(true));
        // AARA venue without AARA requirement
        assertFalse(aaraVenue.checkVenueType(false));

        // Non-AARA venue without AARA requirement
        assertTrue(nonAaraVenue.checkVenueType(false));
        // Non-AARA venue with AARA requirement
        assertFalse(nonAaraVenue.checkVenueType(true));
    }

    @Test
    public void testWillFit() {
        Venue venue = new Venue("V1", 1, rooms, 5, 5, 25, false);

        // Test with fewer students than desks
        assertTrue(venue.willFit(20));
        assertTrue(venue.willFit(25));

        // Test with more students than desks
        assertFalse(venue.willFit(26));
        assertFalse(venue.willFit(30));
    }

    @Test
    public void testGetRooms() {
        Venue venue = new Venue("V1+V2", 2, rooms, 5, 10, 50, false);
        RoomList venueRooms = venue.getRooms();

        assertNotNull(venueRooms);
        // Note: The actual room retrieval depends on the registry implementation
    }

    @Test
    public void testVenueStreaming() throws IOException {
        Venue original = new Venue("V1+V2", 2, rooms, 5, 10, 48, true);

        // Write to stream
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        original.streamOut(bw, 1);
        bw.close();

        // Verify output format
        String output = sw.toString();
        assertTrue(output.contains("V1+V2"));
        assertTrue(output.contains("48 AARA desks"));
        assertTrue(output.contains("Room Count: 2"));
        assertTrue(output.contains("Rows: 5"));
        assertTrue(output.contains("Columns: 10"));
    }

    @Test
    public void testVenueStreamIn() throws IOException {
        // Create test input
        String input = "1. V1 (25 Non-AARA desks)\n" +
                "Room Count: 1, Rooms: R1, Rows: 5, Columns: 5, Desks: 25, AARA: false\n";

        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        Venue venue = new Venue(br, registry, 1);

        assertEquals("V1", venue.venueId());
        assertEquals(5, venue.getRows());
        assertEquals(5, venue.getColumns());
        assertEquals(25, venue.deskCount());
        assertFalse(venue.isAara());
    }

    @Test
    public void testVenueStreamInAara() throws IOException {
        // Create test input for AARA venue
        String input = "1. W1+W2 (15 AARA desks)\n" +
                "Room Count: 2, Rooms: S101 S102, Rows: 3, Columns: 5, Desks: 15, AARA: true\n";

        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        Venue venue = new Venue(br, registry, 1);

        assertEquals("W1+W2", venue.venueId());
        assertEquals(3, venue.getRows());
        assertEquals(5, venue.getColumns());
        assertEquals(15, venue.deskCount());
        assertTrue(venue.isAara());
    }

    @Test(expected = RuntimeException.class)
    public void testVenueStreamInInvalidFormat() throws IOException {
        String input = "Invalid format";
        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        new Venue(br, registry, 1); // Should throw RuntimeException
    }

    @Test
    public void testToString() {
        Venue venue = new Venue("TestVenue", 1, rooms, 4, 6, 24, false);
        assertEquals("TestVenue", venue.toString());
    }

    @Test
    public void testGetFullDetail() {
        Venue venue = new Venue("V1", 2, rooms, 5, 8, 40, true);
        String detail = venue.getFullDetail();

        assertTrue(detail.contains("Venue: V1"));
        assertTrue(detail.contains("Rooms: 2"));
        assertTrue(detail.contains("Layout: 5 rows x 8 columns"));
        assertTrue(detail.contains("Total Desks: 40"));
        assertTrue(detail.contains("Type: AARA"));
    }

    @Test
    public void testToTableRow() {
        Venue venue = new Venue("V1", 2, rooms, 5, 6, 30, true);
        Object[] row = venue.toTableRow();

        assertEquals("V1", row[0]);
        assertEquals(2, row[1]);
        assertEquals("5x6", row[2]);
        assertEquals(30, row[3]);
        assertEquals("Yes", row[4]);
    }

    @Test
    public void testEquals() {
        Venue venue1 = new Venue("V1", 1, rooms, 5, 5, 25, false);
        Venue venue2 = new Venue("V1", 1, rooms, 5, 5, 25, false);
        Venue venue3 = new Venue("V2", 1, rooms, 5, 5, 25, false);

        // Venues should be equal based on their ID (inherited from Room)
        assertEquals(venue1, venue2);
        assertNotEquals(venue1, venue3);
    }

    @Test
    public void testHashCode() {
        Venue venue1 = new Venue("V1", 1, rooms, 5, 5, 25, false);
        Venue venue2 = new Venue("V1", 1, rooms, 5, 5, 25, false);

        // Equal venues should have equal hash codes
        assertEquals(venue1.hashCode(), venue2.hashCode());
    }
}