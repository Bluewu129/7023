package examblock.model;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class UnitTest {
    private Registry registry;
    private Subject mathSubject;
    private Subject englishSubject;
    private Subject physicsSubject;

    @Before
    public void setUp() {
        registry = new RegistryImpl();
        mathSubject = new Subject("Mathematics", "Study of numbers and patterns", registry);
        englishSubject = new Subject("English", "Study of language and literature", registry);
        physicsSubject = new Subject("Physics", "Study of matter and energy", registry);
    }

    @Test
    public void testUnitCreation() {
        Unit unit = new Unit(mathSubject, '3', "Calculus", "Advanced calculus topics", registry);

        assertEquals(mathSubject, unit.getSubject());
        assertEquals(Character.valueOf('3'), unit.id());
        assertEquals("Calculus", unit.getTitle());
        assertEquals("Advanced calculus topics", unit.getDescription());
    }

    @Test
    public void testUnitCreationDifferentUnits() {
        Unit unit1 = new Unit(englishSubject, '1', "Introduction to Literature",
                "Basic literary concepts", registry);
        Unit unit2 = new Unit(englishSubject, '2', "Advanced Composition",
                "Advanced writing techniques", registry);
        Unit unit3 = new Unit(physicsSubject, '4', "Quantum Mechanics",
                "Introduction to quantum physics", registry);

        assertEquals('1', unit1.id().charValue());
        assertEquals('2', unit2.id().charValue());
        assertEquals('4', unit3.id().charValue());

        assertEquals(englishSubject, unit1.getSubject());
        assertEquals(englishSubject, unit2.getSubject());
        assertEquals(physicsSubject, unit3.getSubject());
    }

    @Test
    public void testGetId() {
        Unit unit = new Unit(mathSubject, '3', "Calculus", "Advanced calculus", registry);
        assertEquals("Mathematics:3", unit.getId());

        Unit unit2 = new Unit(englishSubject, 'A', "Special Topics", "Special unit", registry);
        assertEquals("English:A", unit2.getId());
    }

    @Test
    public void testGetFullDetail() {
        Unit unit = new Unit(mathSubject, '3', "Calculus", "Advanced calculus topics", registry);
        String detail = unit.getFullDetail();

        assertTrue(detail.contains("Mathematics: Unit 3: Calculus"));
        assertTrue(detail.contains("\"Advanced calculus topics\""));
    }

    @Test
    public void testToString() {
        Unit unit = new Unit(physicsSubject, '2', "Mechanics", "Classical mechanics", registry);
        assertEquals("Physics: Unit 2: Mechanics", unit.toString());
    }

    @Test
    public void testToTableRow() {
        Unit unit = new Unit(mathSubject, '4', "Statistics", "Statistical analysis", registry);
        Object[] row = unit.toTableRow();

        assertEquals("Mathematics", row[0]);
        assertEquals('4', row[1]);
        assertEquals("Statistics", row[2]);
    }

    @Test
    public void testToLongTableRow() {
        Unit unit = new Unit(englishSubject, '1', "Grammar Fundamentals",
                "Basic grammar rules", registry);
        Object[] row = unit.toLongTableRow();

        assertEquals("English", row[0]);
        assertEquals('1', row[1]);
        assertEquals("Grammar Fundamentals", row[2]);
        assertEquals("Basic grammar rules", row[3]);
        assertEquals(19, row[4]); // Length of title
    }

    @Test
    public void testStreamOut() throws IOException {
        Unit unit = new Unit(mathSubject, '3', "Calculus",
                "Advanced calculus and differential equations", registry);

        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        unit.streamOut(bw, 1);
        bw.close();

        String output = sw.toString();
        assertTrue(output.contains("1. MATHEMATICS"));
        assertTrue(output.contains("Mathematics, Unit 3: Calculus"));
        assertTrue(output.contains("\"Advanced calculus and differential equations\""));
    }

    @Test
    public void testStreamIn() throws IOException {
        String input = "1. ACCOUNTING\n" +
                "Accounting, Unit 3: Managing resources\n" +
                "\"The study of financial management\"\n";

        // Need to have the subject in registry first
        Subject accounting = new Subject("Accounting", "Financial studies", registry);

        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        Unit unit = new Unit(br, registry, 1);

        assertEquals(accounting, unit.getSubject());
        assertEquals(Character.valueOf('3'), unit.id());
        assertEquals("Managing resources", unit.getTitle());
        assertEquals("The study of financial management", unit.getDescription());
    }

    @Test
    public void testStreamInWithQuotesInDescription() throws IOException {
        String input = "1. ENGLISH\n" +
                "English, Unit 2: Creative Writing\n" +
                "\"Study of \"creative\" writing techniques\"\n";

        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        Unit unit = new Unit(br, registry, 1);

        assertEquals("Study of \"creative\" writing techniques", unit.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInInvalidFormat() throws IOException {
        String input = "Invalid format";
        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        new Unit(br, registry, 1); // Should throw RuntimeException
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInMissingSubject() throws IOException {
        String input = "1. NONEXISTENT\n" +
                "Nonexistent, Unit 1: Test\n" +
                "\"Test description\"\n";

        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        new Unit(br, registry, 1); // Should throw RuntimeException - subject not found
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWrongIndex() throws IOException {
        String input = "2. MATHEMATICS\n" + // Wrong index (should be 1)
                "Mathematics, Unit 1: Algebra\n" +
                "\"Basic algebra\"\n";

        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        new Unit(br, registry, 1); // Should throw RuntimeException - index mismatch
    }

    @Test
    public void testTitleCleaning() {
        // Test that title is cleaned properly
        Unit unit = new Unit(mathSubject, '1', "  Algebra   Basics  ",
                "Introduction to algebra", registry);
        assertEquals("Algebra Basics", unit.getTitle());
    }

    @Test
    public void testDefaultConstructor() {
        Unit unit = new Unit();
        assertEquals('1', unit.id().charValue());
        assertEquals("", unit.getTitle());
        assertEquals("", unit.getDescription());
        assertNull(unit.getSubject());
    }

    @Test
    public void testUnitWithSingleCharacterId() {
        // Test various single character IDs
        Unit unitA = new Unit(mathSubject, 'A', "Advanced Topics", "Advanced math", registry);
        Unit unitB = new Unit(mathSubject, 'B', "Basic Topics", "Basic math", registry);
        Unit unit9 = new Unit(mathSubject, '9', "Ninth Unit", "Unit 9", registry);

        assertEquals('A', unitA.id().charValue());
        assertEquals('B', unitB.id().charValue());
        assertEquals('9', unit9.id().charValue());
    }
}