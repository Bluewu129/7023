package examblock.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Test class for Venue functionality
 */
public class VenueTest {

    private Registry registry;
    private RoomList roomList;
    private Room room1;
    private Room room2;
    private Venue testVenue;

    @Before
    public void setUp() {
        registry = new RegistryImpl();
        roomList = new RoomList(registry);
        room1 = new Room("R1", registry);
        room2 = new Room("R2", registry);
        roomList.add(room1);

        testVenue = new Venue("V1", 1, roomList, 5, 5, 25, false, registry);
    }

    @After
    public void tearDown() {
        registry = null;
        roomList = null;
        room1 = null;
        room2 = null;
        testVenue = null;
    }

    @Test
    public void testBasicConstruction() {
        assertEquals("V1", testVenue.venueId());
        assertEquals("V1", testVenue.getId());
        assertEquals(1, testVenue.getRooms().size());
        assertEquals(5, testVenue.getRows());
        assertEquals(5, testVenue.getColumns());
        assertEquals(25, testVenue.deskCount());
        assertFalse(testVenue.isAara());
    }

    @Test
    public void testMultiRoomVenue() {
        roomList.add(room2);
        Venue multiRoomVenue = new Venue("V1+V2", 2, roomList, 5, 10, 50, true, registry);

        assertEquals("V1+V2", multiRoomVenue.venueId());
        assertEquals(2, multiRoomVenue.getRooms().size());
        assertEquals(5, multiRoomVenue.getRows());
        assertEquals(10, multiRoomVenue.getColumns());
        assertEquals(50, multiRoomVenue.deskCount());
        assertTrue(multiRoomVenue.isAara());
    }

    @Test
    public void testVenueWithNoRooms() {
        RoomList emptyRoomList = new RoomList(registry);
        Venue emptyVenue = new Venue("Empty", 0, emptyRoomList, 5, 5, 25, false, registry);

        // Should adjust values when no rooms provided
        assertEquals(0, emptyVenue.getRows());
        assertEquals(0, emptyVenue.getColumns());
        assertEquals(0, emptyVenue.deskCount());
    }

    @Test
    public void testVenueWithMismatchedRoomCount() {
        // Constructor says 2 rooms but only provide 1
        Venue venue = new Venue("Test", 2, roomList, 5, 5, 25, false, registry);

        // Should print warning and adjust room count
        // The actual room count should match what was provided
        assertEquals(1, venue.getRooms().size());
    }

    @Test
    public void testVenueWithTooManyDesks() {
        // Constructor says 50 desks but 5x5 = 25 max
        Venue venue = new Venue("Test", 1, roomList, 5, 5, 50, false, registry);

        // Should adjust desk count to match rows * columns
        assertEquals(25, venue.deskCount());
    }

    @Test
    public void testWillFit() {
        assertTrue(testVenue.willFit(20));  // 20 students fit in 25 desks
        assertTrue(testVenue.willFit(25));  // Exactly 25 students fit
        assertFalse(testVenue.willFit(30)); // 30 students don't fit in 25 desks
    }

    @Test
    public void testCheckVenueType() {
        // Test non-AARA venue
        assertTrue(testVenue.checkVenueType(false));  // Matches venue type
        assertFalse(testVenue.checkVenueType(true));  // Doesn't match venue type

        // Test AARA venue
        Venue aaraVenue = new Venue("AARA1", 1, roomList, 3, 5, 15, true, registry);
        assertTrue(aaraVenue.checkVenueType(true));   // Matches AARA type
        assertFalse(aaraVenue.checkVenueType(false)); // Doesn't match non-AARA type
    }

    @Test
    public void testGetFullDetail() {
        String detail = testVenue.getFullDetail();

        assertTrue(detail.contains("Venue: V1"));
        assertTrue(detail.contains("Room Count: 1"));
        assertTrue(detail.contains("Rooms: R1"));
        assertTrue(detail.contains("Layout: 5 rows x 5 columns"));
        assertTrue(detail.contains("Total Desks: 25"));
        assertTrue(detail.contains("AARA Suitable: No"));
    }

    @Test
    public void testToTableRow() {
        Object[] row = testVenue.toTableRow();
        assertEquals(6, row.length);
        assertEquals("V1", row[0]);
        assertEquals(1, row[1]);
        assertEquals(5, row[2]);
        assertEquals(5, row[3]);
        assertEquals(25, row[4]);
        assertEquals(false, row[5]);
    }

    @Test
    public void testToString() {
        assertEquals("V1", testVenue.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        RoomList roomList2 = new RoomList(registry);
        Room room1Copy = new Room("R1", registry);
        roomList2.add(room1Copy);

        Venue venue1 = new Venue("V1", 1, roomList, 5, 5, 25, false, registry);
        Venue venue2 = new Venue("V1", 1, roomList2, 5, 5, 25, false, registry);
        Venue venue3 = new Venue("V2", 1, roomList, 5, 5, 25, false, registry);

        // Test equals
        assertTrue(venue1.equals(venue2));  // Same properties
        assertFalse(venue1.equals(venue3)); // Different venue ID
        assertFalse(venue1.equals(null));
        assertFalse(venue1.equals("Not a venue"));

        // Test reflexivity
        assertTrue(venue1.equals(venue1));

        // Test hash code consistency
        assertEquals(venue1.hashCode(), venue2.hashCode());
    }

    @Test
    public void testStreamOut() throws IOException {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        testVenue.streamOut(bufferedWriter, 1);
        bufferedWriter.flush();

        String output = stringWriter.toString();
        String[] lines = output.split(System.lineSeparator());

        assertEquals(2, lines.length);
        assertEquals("1. V1 (25 desks)", lines[0]);
        assertTrue(lines[1].contains("Room Count: 1"));
        assertTrue(lines[1].contains("Rooms: R1"));
        assertTrue(lines[1].contains("Rows: 5"));
        assertTrue(lines[1].contains("Columns: 5"));
        assertTrue(lines[1].contains("Desks: 25"));
        assertTrue(lines[1].contains("AARA: false"));
    }

    @Test
    public void testStreamIn() throws IOException {
        String input = "1. V2 (30 desks)" + System.lineSeparator() +
                "Room Count: 1, Rooms: R2, Rows: 6, Columns: 5, Desks: 30, AARA: true" +
                System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Venue venue = new Venue(reader, registry, 1);

        assertEquals("V2", venue.venueId());
        assertEquals(1, venue.getRooms().size());
        assertEquals(6, venue.getRows());
        assertEquals(5, venue.getColumns());
        assertEquals(30, venue.deskCount());
        assertTrue(venue.isAara());
    }

    @Test
    public void testStreamInWithMultipleRooms() throws IOException {
        String input = "1. V1+V2+V3 (80 desks)" + System.lineSeparator() +
                "Room Count: 3, Rooms: R1 R2 R3, Rows: 5, Columns: 16, Desks: 80, AARA: false" +
                System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Venue venue = new Venue(reader, registry, 1);

        assertEquals("V1+V2+V3", venue.venueId());
        assertEquals(3, venue.getRooms().size());
        assertEquals(5, venue.getRows());
        assertEquals(16, venue.getColumns());
        assertEquals(80, venue.deskCount());
        assertFalse(venue.isAara());
    }

    @Test
    public void testStreamInWithMismatchedDeskCounts() throws IOException {
        String input = "1. V1 (20 desks)" + System.lineSeparator() +
                "Room Count: 1, Rooms: R1, Rows: 5, Columns: 5, Desks: 25, AARA: false" +
                System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Venue venue = new Venue(reader, registry, 1);

        // Should use the desk count from detail line (25) over header line (20)
        assertEquals(25, venue.deskCount());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidIndex() throws IOException {
        String input = "2. V1 (25 desks)" + System.lineSeparator() +
                "Room Count: 1, Rooms: R1, Rows: 5, Columns: 5, Desks: 25, AARA: false" +
                System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Venue(reader, registry, 1); // Expected index 1, but input has 2
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidFormat() throws IOException {
        String input = "Invalid format line" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Venue(reader, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithEOF() throws IOException {
        String input = "1. V1 (25 desks)" + System.lineSeparator();
        // Missing detail line

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Venue(reader, registry, 1);
    }

    @Test
    public void testRegistryIntegration() {
        // Test that venue is added to registry
        assertTrue(registry.contains("V1", Venue.class));
        assertEquals(testVenue, registry.find("V1", Venue.class));
    }

    @Test
    public void testVenueInheritance() {
        // Venue extends Room, so test Room functionality
        assertEquals("V1", testVenue.roomId());
        assertEquals("V1", testVenue.toString());
    }

    @Test
    public void testAaraVenue() {
        Venue aaraVenue = new Venue("W1", 1, roomList, 2, 5, 10, true, registry);

        assertTrue(aaraVenue.isAara());
        assertEquals("W1", aaraVenue.venueId());
        assertEquals(10, aaraVenue.deskCount());
    }

    @Test
    public void testVenueCapacity() {
        // Test various capacity scenarios
        assertTrue(testVenue.willFit(1));     // Single student
        assertTrue(testVenue.willFit(25));    // Full capacity
        assertFalse(testVenue.willFit(26));   // Over capacity
        assertFalse(testVenue.willFit(100));  // Way over capacity
    }

    @Test
    public void testStreamInWithExistingRooms() throws IOException {
        // Pre-create rooms in registry
        Room existingRoom = new Room("R5", registry);

        String input = "1. V5 (15 desks)" + System.lineSeparator() +
                "Room Count: 1, Rooms: R5, Rows: 3, Columns: 5, Desks: 15, AARA: true" +
                System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Venue venue = new Venue(reader, registry, 1);

        assertEquals("V5", venue.venueId());
        assertEquals(1, venue.getRooms().size());
        // Should use existing room from registry
        assertTrue(venue.getRooms().getItems().contains(existingRoom));
    }

    @Test
    public void testStreamInWithEmptyRooms() throws IOException {
        String input = "1. V6 (10 desks)" + System.lineSeparator() +
                "Room Count: 0, Rooms: , Rows: 2, Columns: 5, Desks: 10, AARA: false" +
                System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Venue venue = new Venue(reader, registry, 1);

        // Should create default room when no rooms specified
        assertEquals(1, venue.getRooms().size());
        assertEquals("V6", venue.getRooms().all().get(0).roomId());
    }

    @Test
    public void testVenueWithComplexId() {
        Venue complexVenue = new Venue("A101+A102+A103", 3, roomList, 8, 15, 120, true, registry);
        assertEquals("A101+A102+A103", complexVenue.venueId());
        assertEquals("A101+A102+A103", complexVenue.getId());
    }
}