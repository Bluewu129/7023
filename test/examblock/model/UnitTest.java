package examblock.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Test class for Unit functionality
 */
public class UnitTest {

    private Registry registry;
    private Subject testSubject;
    private Unit testUnit;

    @Before
    public void setUp() {
        registry = new RegistryImpl();
        testSubject = new Subject("Mathematics", "The study of numbers.", registry);
        testUnit = new Unit(testSubject, '3', "Advanced Calculus",
                "Study of differential and integral calculus.", registry);
    }

    @After
    public void tearDown() {
        registry = null;
        testSubject = null;
        testUnit = null;
    }

    @Test
    public void testBasicConstruction() {
        assertEquals(testSubject, testUnit.getSubject());
        assertEquals(Character.valueOf('3'), testUnit.id());
        assertEquals("Study of differential and integral calculus.", testUnit.getDescription());
        assertEquals("Mathematics-3", testUnit.getId());
    }

    @Test
    public void testConstructionWithDifferentUnitIds() {
        Unit unit4 = new Unit(testSubject, '4', "Statistics", "Statistical analysis.", registry);
        assertEquals(Character.valueOf('4'), unit4.id());
        assertEquals("Mathematics-4", unit4.getId());

        Unit unitA = new Unit(testSubject, 'A', "Algebra", "Algebraic structures.", registry);
        assertEquals(Character.valueOf('A'), unitA.id());
        assertEquals("Mathematics-A", unitA.getId());
    }

    @Test
    public void testToString() {
        String expected = "Mathematics: Unit 3: Advanced Calculus";
        assertEquals(expected, testUnit.toString());
    }

    @Test
    public void testGetFullDetail() {
        String expected = "Mathematics: Unit 3: Advanced Calculus\n" +
                "\"Study of differential and integral calculus.\"\n";
        assertEquals(expected, testUnit.getFullDetail());
    }

    @Test
    public void testToTableRow() {
        Object[] row = testUnit.toTableRow();
        assertEquals(4, row.length);
        assertEquals("Mathematics", row[0]);
        assertEquals('3', row[1]);
        assertEquals("Advanced Calculus", row[2]);
        assertEquals("Study of differential and integral calculus.", row[3]);
    }

    @Test
    public void testEqualsAndHashCode() {
        Unit unit1 = new Unit(testSubject, '3', "Advanced Calculus",
                "Study of differential and integral calculus.", registry);
        Unit unit2 = new Unit(testSubject, '3', "Advanced Calculus",
                "Study of differential and integral calculus.", registry);
        Unit unit3 = new Unit(testSubject, '4', "Statistics", "Statistical methods.", registry);

        // Test equals
        assertTrue(unit1.equals(unit2));
        assertFalse(unit1.equals(unit3));
        assertFalse(unit1.equals(null));
        assertFalse(unit1.equals("Not a unit"));

        // Test reflexivity
        assertTrue(unit1.equals(unit1));

        // Test hash code consistency
        assertEquals(unit1.hashCode(), unit2.hashCode());
    }

    @Test
    public void testStreamOut() throws IOException {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        testUnit.streamOut(bufferedWriter, 1);
        bufferedWriter.flush();

        String output = stringWriter.toString();
        String[] lines = output.split(System.lineSeparator());

        assertEquals(3, lines.length);
        assertEquals("1. MATHEMATICS", lines[0]);
        assertEquals("Mathematics, Unit 3: Advanced Calculus", lines[1]);
        assertEquals("\"Study of differential and integral calculus.\"", lines[2]);
    }

    @Test
    public void testStreamIn() throws IOException {
        String input = "1. MATHEMATICS" + System.lineSeparator() +
                "Mathematics, Unit 3: Advanced Calculus" + System.lineSeparator() +
                "\"Study of differential and integral calculus.\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Unit unit = new Unit(reader, registry, 1);

        assertEquals("Mathematics", unit.getSubject().getTitle());
        assertEquals(Character.valueOf('3'), unit.id());
        assertEquals("Study of differential and integral calculus.", unit.getDescription());
    }

    @Test
    public void testStreamInWithNewSubject() throws IOException {
        String input = "1. PHYSICS" + System.lineSeparator() +
                "Physics, Unit 4: Quantum Mechanics" + System.lineSeparator() +
                "\"Study of quantum phenomena.\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Unit unit = new Unit(reader, registry, 1);

        assertEquals("Physics", unit.getSubject().getTitle());
        assertEquals(Character.valueOf('4'), unit.id());
        assertEquals("Study of quantum phenomena.", unit.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidIndex() throws IOException {
        String input = "2. MATHEMATICS" + System.lineSeparator() +
                "Mathematics, Unit 3: Advanced Calculus" + System.lineSeparator() +
                "\"Description\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Unit(reader, registry, 1); // Expected index 1, but input has 2
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidUnitFormat() throws IOException {
        String input = "1. MATHEMATICS" + System.lineSeparator() +
                "Invalid unit format line" + System.lineSeparator() +
                "\"Description\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Unit(reader, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithMissingColon() throws IOException {
        String input = "1. MATHEMATICS" + System.lineSeparator() +
                "Mathematics, Unit 3 Missing Colon" + System.lineSeparator() +
                "\"Description\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Unit(reader, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithEOF() throws IOException {
        String input = "1. MATHEMATICS" + System.lineSeparator();
        // Missing unit details and description

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Unit(reader, registry, 1);
    }

    @Test
    public void testRegistryIntegration() {
        // Test that unit is added to registry
        assertTrue(registry.contains("Mathematics-3", Unit.class));
        assertEquals(testUnit, registry.find("Mathematics-3", Unit.class));
    }

    @Test
    public void testMultipleUnitsForSameSubject() {
        Unit unit3 = new Unit(testSubject, '3', "Unit 3 Title", "Unit 3 description.", registry);
        Unit unit4 = new Unit(testSubject, '4', "Unit 4 Title", "Unit 4 description.", registry);

        assertEquals("Mathematics-3", unit3.getId());
        assertEquals("Mathematics-4", unit4.getId());

        assertTrue(registry.contains("Mathematics-3", Unit.class));
        assertTrue(registry.contains("Mathematics-4", Unit.class));
    }

    @Test
    public void testUnitWithDifferentSubjects() {
        Subject physicsSubject = new Subject("Physics", "The study of matter and energy.", registry);
        Unit physicsUnit = new Unit(physicsSubject, '3', "Thermodynamics",
                "Study of heat and energy transfer.", registry);

        assertEquals("Physics-3", physicsUnit.getId());
        assertEquals("Physics", physicsUnit.getSubject().getTitle());
        assertNotEquals(testUnit.getSubject(), physicsUnit.getSubject());
    }

    @Test
    public void testStreamInWithQuotedDescription() throws IOException {
        String input = "1. CHEMISTRY" + System.lineSeparator() +
                "Chemistry, Unit 4: Organic Chemistry" + System.lineSeparator() +
                "\"Study of carbon-based compounds.\"" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Unit unit = new Unit(reader, registry, 1);

        assertEquals("Study of carbon-based compounds.", unit.getDescription());
    }

    @Test
    public void testStreamInWithUnquotedDescription() throws IOException {
        String input = "1. BIOLOGY" + System.lineSeparator() +
                "Biology, Unit 3: Cell Biology" + System.lineSeparator() +
                "Study of cellular structures" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Unit unit = new Unit(reader, registry, 1);

        assertEquals("Study of cellular structures", unit.getDescription());
    }

    @Test
    public void testUnitWithAlphabeticId() {
        Unit unitZ = new Unit(testSubject, 'Z', "Special Topic", "Advanced topic.", registry);
        assertEquals(Character.valueOf('Z'), unitZ.id());
        assertEquals("Mathematics-Z", unitZ.getId());
    }

    @Test
    public void testUnitWithNumericId() {
        Unit unit9 = new Unit(testSubject, '9', "Advanced Topic", "Final year topic.", registry);
        assertEquals(Character.valueOf('9'), unit9.id());
        assertEquals("Mathematics-9", unit9.getId());
    }

    @Test
    public void testComplexUnitTitle() {
        Unit complexUnit = new Unit(testSubject, '3',
                "Advanced Mathematical Methods and Applications",
                "Comprehensive study of advanced mathematical techniques.",
                registry);

        String expected = "Mathematics: Unit 3: Advanced Mathematical Methods and Applications";
        assertEquals(expected, complexUnit.toString());
    }
}