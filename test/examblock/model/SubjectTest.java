package examblock.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Test class for Subject functionality
 */
public class SubjectTest {

    private Registry registry;
    private Subject testSubject;

    @Before
    public void setUp() {
        registry = new RegistryImpl();
        testSubject = new Subject("Mathematics", "The study of numbers and patterns.", registry);
    }

    @After
    public void tearDown() {
        registry = null;
        testSubject = null;
    }

    @Test
    public void testBasicConstruction() {
        assertEquals("Mathematics", testSubject.getTitle());
        assertEquals("The study of numbers and patterns.", testSubject.getDescription());
        assertEquals("Mathematics", testSubject.getId());
    }

    @Test
    public void testConstructionWithNullValues() {
        Subject subject = new Subject(null, null, registry);
        assertEquals("", subject.getTitle());
        assertEquals("", subject.getDescription());
    }

    @Test
    public void testConstructionWithEmptyValues() {
        Subject subject = new Subject("", "", registry);
        assertEquals("", subject.getTitle());
        assertEquals("", subject.getDescription());
    }

    @Test
    public void testTitleSanitization() {
        // Test multiple spaces
        Subject subject1 = new Subject("Advanced    Mathematics", "Description", registry);
        assertEquals("Advanced Mathematics", subject1.getTitle());

        // Test leading/trailing spaces
        Subject subject2 = new Subject("  Physics  ", "Description", registry);
        assertEquals("Physics", subject2.getTitle());

        // Test trailing period removal
        Subject subject3 = new Subject("Chemistry.", "Description", registry);
        assertEquals("Chemistry", subject3.getTitle());

        // Test multiple spaces and trailing period
        Subject subject4 = new Subject("  Biology   .", "Description", registry);
        assertEquals("Biology", subject4.getTitle());
    }

    @Test
    public void testDescriptionSanitization() {
        // Test multiple spaces
        Subject subject1 = new Subject("Title", "This    is    a    description", registry);
        assertEquals("This is a description.", subject1.getDescription());

        // Test leading/trailing spaces
        Subject subject2 = new Subject("Title", "  Description  ", registry);
        assertEquals("Description.", subject2.getDescription());

        // Test missing period addition
        Subject subject3 = new Subject("Title", "No period", registry);
        assertEquals("No period.", subject3.getDescription());

        // Test existing period preservation
        Subject subject4 = new Subject("Title", "Has period.", registry);
        assertEquals("Has period.", subject4.getDescription());
    }

    @Test
    public void testSanitizeTitle() {
        assertEquals("Mathematics", testSubject.sanitiseTitle("Mathematics"));
        assertEquals("Advanced Mathematics", testSubject.sanitiseTitle("Advanced    Mathematics"));
        assertEquals("Physics", testSubject.sanitiseTitle("  Physics  "));
        assertEquals("Chemistry", testSubject.sanitiseTitle("Chemistry."));
        assertEquals("", testSubject.sanitiseTitle(null));
        assertEquals("", testSubject.sanitiseTitle(""));
        assertEquals("", testSubject.sanitiseTitle("   "));
    }

    @Test
    public void testSanitizeDescription() {
        assertEquals("Test description.", testSubject.sanitiseDescription("Test description"));
        assertEquals("Test description.", testSubject.sanitiseDescription("Test description."));
        assertEquals("Multiple words here.", testSubject.sanitiseDescription("Multiple    words    here"));
        assertEquals("Trimmed.", testSubject.sanitiseDescription("  Trimmed  "));
        assertEquals("", testSubject.sanitiseDescription(null));
        assertEquals("", testSubject.sanitiseDescription(""));
        assertEquals("", testSubject.sanitiseDescription("   "));
    }

    @Test
    public void testGetFullDetail() {
        String expected = "MATHEMATICS\n\"The study of numbers and patterns.\"\n";
        assertEquals(expected, testSubject.getFullDetail());
    }

    @Test
    public void testToString() {
        String expected = "MATHEMATICS\n";
        assertEquals(expected, testSubject.toString());
    }

    @Test
    public void testToTableRow() {
        Object[] row = testSubject.toTableRow();
        assertEquals(2, row.length);
        assertEquals("Mathematics", row[0]);
        assertEquals("The study of numbers and patterns.", row[1]);
    }

    @Test
    public void testEqualsAndHashCode() {
        Subject subject1 = new Subject("Mathematics", "The study of numbers and patterns.", registry);
        Subject subject2 = new Subject("Mathematics", "The study of numbers and patterns.", registry);
        Subject subject3 = new Subject("Physics", "The study of matter and energy.", registry);

        // Test equals
        assertTrue(subject1.equals(subject2));
        assertFalse(subject1.equals(subject3));
        assertFalse(subject1.equals(null));
        assertFalse(subject1.equals("Not a subject"));

        // Test reflexivity
        assertTrue(subject1.equals(subject1));

        // Test hash code consistency
        assertEquals(subject1.hashCode(), subject2.hashCode());
        assertNotEquals(subject1.hashCode(), subject3.hashCode());
    }

    @Test
    public void testStreamOut() throws IOException {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        testSubject.streamOut(bufferedWriter, 1);
        bufferedWriter.flush();

        String output = stringWriter.toString();
        String[] lines = output.split(System.lineSeparator());

        assertEquals(3, lines.length);
        assertEquals("1. MATHEMATICS", lines[0]);
        assertEquals("Mathematics", lines[1]);
        assertEquals("\"The study of numbers and patterns.\"", lines[2]);
    }

    @Test
    public void testStreamIn() throws IOException {
        String input = "1. MATHEMATICS" + System.lineSeparator() +
                "Mathematics" + System.lineSeparator() +
                "\"The study of numbers and patterns.\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Subject subject = new Subject(reader, registry, 1);

        assertEquals("Mathematics", subject.getTitle());
        assertEquals("The study of numbers and patterns.", subject.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidIndex() throws IOException {
        String input = "2. MATHEMATICS" + System.lineSeparator() +
                "Mathematics" + System.lineSeparator() +
                "\"Description\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Subject(reader, registry, 1); // Expected index 1, but input has 2
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidFormat() throws IOException {
        String input = "Invalid format line" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Subject(reader, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithEOF() throws IOException {
        String input = "1. MATHEMATICS" + System.lineSeparator();
        // Missing title and description lines

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Subject(reader, registry, 1);
    }

    @Test
    public void testRegistryIntegration() {
        // Test that subject is added to registry
        assertTrue(registry.contains("Mathematics", Subject.class));
        assertEquals(testSubject, registry.find("Mathematics", Subject.class));
    }

    @Test
    public void testSubjectWithComplexTitle() {
        Subject subject = new Subject("English and Literature Extension",
                "Advanced study of literary texts.", registry);
        assertEquals("English and Literature Extension", subject.getTitle());
        assertEquals("English and Literature Extension", subject.getId());
    }

    @Test
    public void testSubjectWithRomanNumerals() {
        Subject subject = new Subject("History IV", "Fourth level history course.", registry);
        assertEquals("History IV", subject.getTitle());
    }

    @Test
    public void testSubjectWithNumbers() {
        Subject subject = new Subject("Mathematics 2", "Second year mathematics.", registry);
        assertEquals("Mathematics 2", subject.getTitle());
    }

    @Test
    public void testStreamInWithQuotedDescription() throws IOException {
        String input = "1. PHYSICS" + System.lineSeparator() +
                "Physics" + System.lineSeparator() +
                "\"The study of matter and energy.\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Subject subject = new Subject(reader, registry, 1);

        assertEquals("Physics", subject.getTitle());
        assertEquals("The study of matter and energy.", subject.getDescription());
    }

    @Test
    public void testStreamInWithUnquotedDescription() throws IOException {
        String input = "1. CHEMISTRY" + System.lineSeparator() +
                "Chemistry" + System.lineSeparator() +
                "The study of chemical reactions" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Subject subject = new Subject(reader, registry, 1);

        assertEquals("Chemistry", subject.getTitle());
        assertEquals("The study of chemical reactions.", subject.getDescription());
    }
}