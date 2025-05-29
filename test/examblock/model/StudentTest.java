package examblock.model;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class StudentTest {
    private Registry registry;
    private Subject mathSubject;
    private Subject englishSubject;
    private Subject physicsSubject;
    private Exam mathExam;
    private Exam englishExam;

    @Before
    public void setUp() {
        registry = new RegistryImpl();

        // Create subjects
        mathSubject = new Subject("Mathematics", "Study of numbers", registry);
        englishSubject = new Subject("English", "Language arts", registry);
        physicsSubject = new Subject("Physics", "Study of matter", registry);

        // Create exams
        mathExam = new Exam(mathSubject, Exam.ExamType.EXTERNAL,
                10, 3, 2025, 9, 0, registry);
        englishExam = new Exam(englishSubject, Exam.ExamType.INTERNAL,
                10, 3, 2025, 14, 0, registry);
    }

    @Test
    public void testStudentCreationBasic() {
        // Constructor without AARA (defaults to false)
        Student student = new Student(1234567890L, "John William", "Smith",
                15, 6, 2007, "Blue");

        assertEquals(Long.valueOf(1234567890L), student.getLui());
        assertEquals("John William", student.givenNames());
        assertEquals("John", student.firstName());
        assertEquals("Smith", student.familyName());
        assertEquals(LocalDate.of(2007, 6, 15), student.getDob());
        assertEquals("Blue", student.getHouse());
        assertFalse(student.isAara());
    }

    @Test
    public void testStudentCreationWithAara() {
        // Constructor with AARA and Registry
        Student student = new Student(9876543210L, "Jane", "Doe",
                1, 1, 2007, "Red", true, registry);

        assertTrue(student.isAara());
        assertEquals("Red", student.getHouse());
    }

    @Test
    public void testStudentNameHandling() {
        // Test with multiple given names - using constructor without registry
        Student student1 = new Student(1111111111L, "Mary Elizabeth Anne", "Johnson",
                1, 1, 2007, "Green");
        assertEquals("Mary Elizabeth Anne", student1.givenNames());
        assertEquals("Mary", student1.firstName());
        assertEquals("Mary Elizabeth Anne Johnson", student1.fullName());
        assertEquals("Mary Johnson", student1.shortName());

        // Test with single given name
        Student student2 = new Student(2222222222L, "Bob", "Williams",
                1, 1, 2007, "Yellow");
        assertEquals("Bob", student2.givenNames());
        assertEquals("Bob", student2.firstName());
    }

    @Test
    public void testStudentNameCleaning() {
        // Test name cleaning functionality
        Student student = new Student(3333333333L, "  John   Paul  ", "  Smith  ",
                1, 1, 2007, "Blue");

        // Names should be trimmed and normalized
        assertEquals("John Paul", student.givenNames());
        assertEquals("Smith", student.familyName());
    }

    @Test
    public void testChangeLui() {
        Student student = new Student(1111111111L, "Test", "Student",
                1, 1, 2007, "Blue");

        assertEquals(Long.valueOf(1111111111L), student.getLui());
        student.changeLui(9999999999L);
        assertEquals(Long.valueOf(9999999999L), student.getLui());
    }

    @Test
    public void testSetGiven() {
        Student student = new Student(1111111111L, "Original", "Name",
                1, 1, 2007, "Blue");

        student.setGiven("New Given Names");
        assertEquals("New Given Names", student.givenNames());

        // Test null and empty handling
        student.setGiven(null); // Should keep existing
        assertEquals("New Given Names", student.givenNames());

        student.setGiven(""); // Should keep existing
        assertEquals("New Given Names", student.givenNames());
    }

    @Test
    public void testSetFamily() {
        Student student = new Student(1111111111L, "First", "Original",
                1, 1, 2007, "Blue");

        student.setFamily("NewFamily");
        assertEquals("NewFamily", student.familyName());
    }

    @Test
    public void testAddSubject() {
        Student student = new Student(1111111111L, "Test", "Student",
                1, 1, 2007, "Green");

        assertEquals(0, student.getSubjectsList().size());

        student.addSubject(mathSubject);
        assertEquals(1, student.getSubjectsList().size());
        assertTrue(student.getSubjectsList().contains(mathSubject));

        // Test adding same subject twice
        student.addSubject(mathSubject);
        assertEquals(1, student.getSubjectsList().size()); // Should not duplicate

        student.addSubject(englishSubject);
        assertEquals(2, student.getSubjectsList().size());
    }

    @Test
    public void testRemoveSubject() {
        Student student = new Student(1111111111L, "Test", "Student",
                1, 1, 2007, "Green");

        student.addSubject(mathSubject);
        student.addSubject(englishSubject);
        assertEquals(2, student.getSubjectsList().size());

        student.removeSubject(mathSubject);
        assertEquals(1, student.getSubjectsList().size());
        assertFalse(student.getSubjectsList().contains(mathSubject));
        assertTrue(student.getSubjectsList().contains(englishSubject));
    }

    @Test
    public void testAddExam() {
        Student student = new Student(1111111111L, "Test", "Student",
                1, 1, 2007, "Blue");

        assertEquals(0, student.getExamsList().size());

        student.addExam(mathExam);
        assertEquals(1, student.getExamsList().size());
        assertTrue(student.getExamsList().contains(mathExam));

        // Test adding same exam twice
        student.addExam(mathExam);
        assertEquals(1, student.getExamsList().size()); // Should not duplicate
    }

    @Test
    public void testClearExams() {
        Student student = new Student(1111111111L, "Test", "Student",
                1, 1, 2007, "Blue");

        student.addExam(mathExam);
        student.addExam(englishExam);
        assertEquals(2, student.getExamsList().size());

        student.clearExams();
        assertEquals(0, student.getExamsList().size());
    }

    @Test
    public void testGetId() {
        Student student = new Student(9876543210L, "Test", "Student",
                1, 1, 2007, "Blue");
        assertEquals("9876543210", student.getId());
    }

    @Test
    public void testEquals() {
        Student student1 = new Student(1234567890L, "John", "Smith",
                1, 1, 2007, "Blue");
        Student student2 = new Student(1234567890L, "Jane", "Doe",
                2, 2, 2008, "Red");
        Student student3 = new Student(9876543210L, "John", "Smith",
                1, 1, 2007, "Blue");

        // Students with same LUI should be equal
        assertEquals(student1, student2);
        // Students with different LUI should not be equal
        assertNotEquals(student1, student3);

        // Test with null and different type
        assertNotEquals(student1, null);
        assertNotEquals(student1, "Not a student");
    }

    @Test
    public void testHashCode() {
        Student student1 = new Student(1234567890L, "John", "Smith",
                1, 1, 2007, "Blue");
        Student student2 = new Student(1234567890L, "Jane", "Doe",
                2, 2, 2008, "Red");

        // Equal students should have same hash code
        assertEquals(student1.hashCode(), student2.hashCode());
    }

    @Test
    public void testToTableRow() {
        Student student = new Student(1234567890L, "John", "Smith",
                1, 1, 2007, "Blue", true, registry);
        Object[] row = student.toTableRow();

        assertEquals(1234567890L, row[0]);
        assertEquals("John Smith", row[1]);
        assertEquals("Blue", row[2]);
        assertEquals("Yes", row[3]);
    }

    @Test
    public void testGetFullDetail() {
        Student student = new Student(1234567890L, "John", "Smith",
                1, 1, 2007, "Blue");
        student.addSubject(mathSubject);
        student.addSubject(englishSubject);
        student.addExam(mathExam);

        String detail = student.getFullDetail();
        assertTrue(detail.contains("1234567890"));
        assertTrue(detail.contains("John Smith"));
        assertTrue(detail.contains("=")); // Contains separator line
    }

    @Test
    public void testStreamOut() throws IOException {
        Student student = new Student(9999365663L, "Liam Alexander", "Smith",
                8, 12, 2007, "Blue", false, registry);
        student.addSubject(englishSubject);
        student.addSubject(mathSubject);

        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        student.streamOut(bw, 1);
        bw.close();

        String output = sw.toString();
        assertTrue(output.contains("1. LIAM ALEXANDER SMITH"));
        assertTrue(output.contains("LUI: 9999365663"));
        assertTrue(output.contains("Family Name: Smith"));
        assertTrue(output.contains("Given Name(s): Liam Alexander"));
        assertTrue(output.contains("Date of Birth: 2007-12-08"));
        assertTrue(output.contains("House: Blue"));
        assertTrue(output.contains("AARA: false"));
        assertTrue(output.contains("Subjects: English, Mathematics"));
    }

    @Test
    public void testStreamIn() throws IOException {
        String input = "1. JOHN DOE\n" +
                "LUI: 1234567890, Family Name: Doe, Given Name(s): John, " +
                "Date of Birth: 2007-05-15, House: Red, AARA: true\n" +
                "Subjects: Mathematics, English\n";

        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        Student student = new Student(br, registry, 1);

        assertEquals(Long.valueOf(1234567890L), student.getLui());
        assertEquals("Doe", student.familyName());
        assertEquals("John", student.givenNames());
        assertEquals(LocalDate.of(2007, 5, 15), student.getDob());
        assertEquals("Red", student.getHouse());
        assertTrue(student.isAara());
        assertEquals(2, student.getSubjectsList().size());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInInvalidFormat() throws IOException {
        String input = "Invalid format";
        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        new Student(br, registry, 1); // Should throw RuntimeException
    }

    @Test
    public void testStreamInWithProblematicDate() throws IOException {
        // Test with problematic date format mentioned in code
        String input = "1. TEST STUDENT\n" +
                "LUI: 1111111111, Family Name: Student, Given Name(s): Test, " +
                "Date of Birth: 0200-01-01, House: Blue, AARA: false\n";

        StringReader sr = new StringReader(input);
        BufferedReader br = new BufferedReader(sr);

        Student student = new Student(br, registry, 1);

        // Should handle problematic date and default to 2007
        assertEquals(LocalDate.of(2007, 1, 1), student.getDob());
    }
}