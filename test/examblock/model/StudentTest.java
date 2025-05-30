package examblock.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Test class for Student functionality
 */
public class StudentTest {

    private Registry registry;
    private Student testStudent;
    private Subject mathSubject;
    private Subject physicsSubject;

    @Before
    public void setUp() {
        registry = new RegistryImpl();
        mathSubject = new Subject("Mathematics", "Study of numbers.", registry);
        physicsSubject = new Subject("Physics", "Study of matter and energy.", registry);
        testStudent = new Student(9999365663L, "Liam Alexander", "Smith",
                8, 12, 2007, "Blue", registry);
    }

    @After
    public void tearDown() {
        registry = null;
        testStudent = null;
        mathSubject = null;
        physicsSubject = null;
    }

    @Test
    public void testBasicConstruction() {
        assertEquals(Long.valueOf(9999365663L), testStudent.getLui());
        assertEquals("Liam Alexander", testStudent.givenNames());
        assertEquals("Smith", testStudent.familyName());
        assertEquals("Liam", testStudent.firstName());
        assertEquals("Blue", testStudent.getHouse());
        assertEquals(LocalDate.of(2007, 12, 8), testStudent.getDob());
        assertFalse(testStudent.isAara());
    }

    @Test
    public void testConstructionWithAara() {
        Student aaraStudent = new Student(9999123456L, "Jane", "Doe",
                15, 6, 2008, "Red", true, registry);
        assertTrue(aaraStudent.isAara());
    }

    @Test
    public void testNameSanitization() {
        // Test multiple spaces in given names
        Student student1 = new Student(9999111111L, "John    William", "Brown",
                1, 1, 2008, "Green", registry);
        assertEquals("John William", student1.givenNames());

        // Test leading/trailing spaces
        Student student2 = new Student(9999222222L, "  Mary  ", "  Johnson  ",
                2, 2, 2008, "Yellow", registry);
        assertEquals("Mary", student2.givenNames());
        assertEquals("Johnson", student2.familyName());

        // Test special characters (should be removed)
        Student student3 = new Student(9999333333L, "Sarah123", "O'Connor-Smith",
                3, 3, 2008, "White", registry);
        assertEquals("Sarah", student3.givenNames());
        assertEquals("O'Connor-Smith", student3.familyName()); // Hyphens and apostrophes allowed
    }

    @Test
    public void testSanitiseName() {
        assertEquals("John Smith", testStudent.sanitiseName("John Smith"));
        assertEquals("Mary Jane", testStudent.sanitiseName("Mary    Jane"));
        assertEquals("O'Connor", testStudent.sanitiseName("O'Connor"));
        assertEquals("Smith-Jones", testStudent.sanitiseName("Smith-Jones"));
        assertEquals("", testStudent.sanitiseName(null));
        assertEquals("", testStudent.sanitiseName(""));
        assertEquals("", testStudent.sanitiseName("   "));
        assertEquals("Test Name", testStudent.sanitiseName("Test123Name456"));
    }

    @Test
    public void testNameMethods() {
        assertEquals("Liam Alexander", testStudent.givenNames());
        assertEquals("Liam", testStudent.firstName());
        assertEquals("Smith", testStudent.familyName());
        assertEquals("Liam Smith", testStudent.shortName());
        assertEquals("Liam Alexander Smith", testStudent.fullName());
    }

    @Test
    public void testNameMethodsWithComplexNames() {
        Student student = new Student(9999444444L, "Mary Jane Elizabeth", "van der Berg",
                4, 4, 2008, "Blue", registry);
        assertEquals("Mary Jane Elizabeth", student.givenNames());
        assertEquals("Mary", student.firstName());
        assertEquals("van der Berg", student.familyName());
        assertEquals("Mary van der Berg", student.shortName());
        assertEquals("Mary Jane Elizabeth van der Berg", student.fullName());
    }

    @Test
    public void testNameMethodsWithSingleName() {
        Student student = new Student(9999555555L, "John", "Doe",
                5, 5, 2008, "Red", registry);
        assertEquals("John", student.givenNames());
        assertEquals("John", student.firstName());
        assertEquals("John Doe", student.shortName());
    }

    @Test
    public void testSetters() {
        testStudent.changeLui(9999999999L);
        assertEquals(Long.valueOf(9999999999L), testStudent.getLui());

        testStudent.setGiven("Michael James");
        assertEquals("Michael James", testStudent.givenNames());
        assertEquals("Michael", testStudent.firstName());

        testStudent.setFamily("Johnson");
        assertEquals("Johnson", testStudent.familyName());
    }

    @Test
    public void testSettersWithInvalidInput() {
        // Test setting null given name
        testStudent.setGiven(null);
        assertEquals("Liam Alexander", testStudent.givenNames()); // Should not change

        // Test setting empty given name
        testStudent.setGiven("");
        assertEquals("Liam Alexander", testStudent.givenNames()); // Should not change

        // Test setting null family name
        testStudent.setFamily(null);
        assertEquals("Smith", testStudent.familyName()); // Should not change

        // Test setting empty family name
        testStudent.setFamily("");
        assertEquals("Smith", testStudent.familyName()); // Should not change
    }

    @Test
    public void testSubjectManagement() {
        // Initially no subjects
        assertEquals(0, testStudent.getSubjects().size());

        // Add subjects
        testStudent.addSubject(mathSubject);
        assertEquals(1, testStudent.getSubjects().size());
        assertTrue(testStudent.getSubjects().getItems().contains(mathSubject));

        testStudent.addSubject(physicsSubject);
        assertEquals(2, testStudent.getSubjects().size());
        assertTrue(testStudent.getSubjects().getItems().contains(physicsSubject));

        // Remove subject
        testStudent.removeSubject(mathSubject);
        assertEquals(1, testStudent.getSubjects().size());
        assertFalse(testStudent.getSubjects().getItems().contains(mathSubject));
        assertTrue(testStudent.getSubjects().getItems().contains(physicsSubject));
    }

    @Test
    public void testToTableRow() {
        Object[] row = testStudent.toTableRow();
        assertEquals(6, row.length);
        assertEquals(9999365663L, row[0]);
        assertEquals("Liam", row[1]);
        assertEquals("Smith", row[2]);
        assertEquals(LocalDate.of(2007, 12, 8), row[3]);
        assertEquals("Blue", row[4]);
        assertEquals(false, row[5]);
    }

    @Test
    public void testGetId() {
        assertEquals("9999365663", testStudent.getId());
    }

    @Test
    public void testEqualsAndHashCode() {
        Student student1 = new Student(9999365663L, "Liam Alexander", "Smith",
                8, 12, 2007, "Blue", registry);
        Student student2 = new Student(9999365663L, "Different", "Name",
                1, 1, 2000, "Red", registry);
        Student student3 = new Student(9999999999L, "Liam Alexander", "Smith",
                8, 12, 2007, "Blue", registry);

        // Test equals (based only on LUI)
        assertTrue(testStudent.equals(student1));
        assertTrue(testStudent.equals(student2)); // Same LUI, different details
        assertFalse(testStudent.equals(student3)); // Different LUI
        assertFalse(testStudent.equals(null));
        assertFalse(testStudent.equals("Not a student"));

        // Test reflexivity
        assertTrue(testStudent.equals(testStudent));

        // Test hash code consistency
        assertEquals(testStudent.hashCode(), student1.hashCode());
        assertEquals(testStudent.hashCode(), student2.hashCode());
        assertNotEquals(testStudent.hashCode(), student3.hashCode());
    }

    @Test
    public void testStreamOut() throws IOException {
        // Add a subject to test subject output
        testStudent.addSubject(mathSubject);

        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        testStudent.streamOut(bufferedWriter, 1);
        bufferedWriter.flush();

        String output = stringWriter.toString();
        String[] lines = output.split(System.lineSeparator());

        assertEquals(3, lines.length);
        assertEquals("1. LIAM ALEXANDER SMITH", lines[0]);
        assertTrue(lines[1].contains("LUI: 9999365663"));
        assertTrue(lines[1].contains("Family Name: Smith"));
        assertTrue(lines[1].contains("Given Name(s): Liam Alexander"));
        assertTrue(lines[1].contains("Date of Birth: 2007-12-08"));
        assertTrue(lines[1].contains("House: Blue"));
        assertTrue(lines[1].contains("AARA: false"));
        assertEquals("Subjects: Mathematics", lines[2]);
    }

    @Test
    public void testStreamOutWithMultipleSubjects() throws IOException {
        testStudent.addSubject(mathSubject);
        testStudent.addSubject(physicsSubject);

        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        testStudent.streamOut(bufferedWriter, 1);
        bufferedWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("Subjects: Mathematics, Physics"));
    }

    @Test
    public void testStreamIn() throws IOException {
        String input = "1. JOHN WILLIAM DOE" + System.lineSeparator() +
                "LUI: 9999123456, Family Name: Doe, Given Name(s): John William, " +
                "Date of Birth: 2008-05-15, House: Green, AARA: true" + System.lineSeparator() +
                "Subjects: Mathematics, Physics" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Student student = new Student(reader, registry, 1);

        assertEquals(Long.valueOf(9999123456L), student.getLui());
        assertEquals("Doe", student.familyName());
        assertEquals("John William", student.givenNames());
        assertEquals(LocalDate.of(2008, 5, 15), student.getDob());
        assertEquals("Green", student.getHouse());
        assertTrue(student.isAara());
        assertEquals(2, student.getSubjects().size());
    }

    @Test
    public void testStreamInWithoutSubjects() throws IOException {
        String input = "1. JANE SMITH" + System.lineSeparator() +
                "LUI: 9999654321, Family Name: Smith, Given Name(s): Jane, " +
                "Date of Birth: 2007-03-20, House: Red, AARA: false" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Student student = new Student(reader, registry, 1);

        assertEquals(Long.valueOf(9999654321L), student.getLui());
        assertEquals("Smith", student.familyName());
        assertEquals("Jane", student.givenNames());
        assertFalse(student.isAara());
        assertEquals(0, student.getSubjects().size());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidIndex() throws IOException {
        String input = "2. JOHN DOE" + System.lineSeparator() +
                "LUI: 123456, Family Name: Doe, Given Name(s): John, " +
                "Date of Birth: 2008-01-01, House: Blue, AARA: false" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Student(reader, registry, 1); // Expected index 1, but input has 2
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithEOF() throws IOException {
        String input = "1. JOHN DOE" + System.lineSeparator();
        // Missing details line

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Student(reader, registry, 1);
    }

    @Test
    public void testRegistryIntegration() {
        // Test that student is added to registry
        assertTrue(registry.contains("9999365663", Student.class));
        assertEquals(testStudent, registry.find("9999365663", Student.class));
    }

    @Test
    public void testGetFullDetail() {
        testStudent.addSubject(mathSubject);
        String detail = testStudent.getFullDetail();

        assertTrue(detail.contains("9999365663 Liam Smith"));
        assertTrue(detail.contains("Mathematics"));
        assertTrue(detail.contains("=".repeat(60)));
    }

    @Test
    public void testToString() {
        testStudent.addSubject(mathSubject);
        String toString = testStudent.toString();

        assertTrue(toString.contains("9999365663 Liam Smith"));
        assertTrue(toString.contains("Mathematics"));
        assertTrue(toString.contains("=".repeat(60)));
    }

    @Test
    public void testHyphenatedNames() {
        Student student = new Student(9999777777L, "Mary-Jane", "Smith-Jones",
                7, 7, 2008, "Blue", registry);
        assertEquals("Mary-Jane", student.givenNames());
        assertEquals("Smith-Jones", student.familyName());
        assertEquals("Mary-Jane", student.firstName());
    }

    @Test
    public void testApostropheNames() {
        Student student = new Student(9999888888L, "Sean", "O'Connor",
                8, 8, 2008, "Green", registry);
        assertEquals("O'Connor", student.familyName());
    }

    @Test
    public void testStreamInWithMissingSubjects() throws IOException {
        // Test when subjects in file don't exist in registry
        String input = "1. STUDENT NAME" + System.lineSeparator() +
                "LUI: 9999999999, Family Name: Name, Given Name(s): Student, " +
                "Date of Birth: 2008-01-01, House: Blue, AARA: false" + System.lineSeparator() +
                "Subjects: NonExistentSubject" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Student student = new Student(reader, registry, 1);

        // Should handle missing subject gracefully
        assertEquals(0, student.getSubjects().size());
    }
}