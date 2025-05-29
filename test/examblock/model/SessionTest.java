package examblock.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

public class SessionTest {
    private Registry registry;
    private Venue venue;
    private Venue aaraVenue;
    private Session session;
    private Session aaraSession;
    private Subject mathSubject;
    private Subject englishSubject;
    private Exam mathExam;
    private Exam englishExam;
    private StudentList students;

    @Before
    public void setUp() {
        registry = new RegistryImpl();

        // Create rooms
        RoomList rooms = new RoomList(registry);
        rooms.add(new Room("R1", registry));
        rooms.add(new Room("R2", registry));

        // Create venues
        venue = new Venue("V1", 1, rooms, 5, 5, 25, false);
        aaraVenue = new Venue("V2", 1, rooms, 3, 3, 9, true);

        // Create subjects
        mathSubject = new Subject("Mathematics", "Study of numbers", registry);
        englishSubject = new Subject("English", "Language arts", registry);

        // Create exams
        mathExam = new Exam(mathSubject, Exam.ExamType.EXTERNAL,
                10, 3, 2025, 9, 0, registry);
        englishExam = new Exam(englishSubject, Exam.ExamType.INTERNAL,
                10, 3, 2025, 14, 0, registry);

        // Create sessions
        session = new Session(venue, 1, LocalDate.of(2025, 3, 10), LocalTime.of(9, 0));
        aaraSession = new Session(aaraVenue, 1, LocalDate.of(2025, 3, 10), LocalTime.of(9, 0));

        // Create students
        students = new StudentList(registry);
    }

    @Test
    public void testSessionCreation() {
        assertEquals(venue, session.getVenue());
        assertEquals(1, session.getSessionNumber());
        assertEquals(LocalDate.of(2025, 3, 10), session.getDate());
        assertEquals(LocalTime.of(9, 0), session.getTime());
        assertEquals(0, session.getExams().size());
        assertEquals(0, session.countStudents());
    }

    @Test
    public void testSessionCreationDifferentTimes() {
        Session morningSession = new Session(venue, 1,
                LocalDate.of(2025, 5, 15), LocalTime.of(8, 30));
        Session afternoonSession = new Session(venue, 2,
                LocalDate.of(2025, 5, 15), LocalTime.of(14, 30));

        assertEquals(LocalTime.of(8, 30), morningSession.getTime());
        assertEquals(LocalTime.of(14, 30), afternoonSession.getTime());
        assertEquals(1, morningSession.getSessionNumber());
        assertEquals(2, afternoonSession.getSessionNumber());
    }

    @Test
    public void testScheduleExam() {
        session.scheduleExam(mathExam, 20);

        List<Exam> exams = session.getExams();
        assertEquals(1, exams.size());
        assertTrue(exams.contains(mathExam));
        assertEquals(20, session.countStudents());
    }

    @Test
    public void testScheduleMultipleExams() {
        session.scheduleExam(mathExam, 15);
        session.scheduleExam(englishExam, 10);

        List<Exam> exams = session.getExams();
        assertEquals(2, exams.size());
        assertTrue(exams.contains(mathExam));
        assertTrue(exams.contains(englishExam));
        assertEquals(25, session.countStudents()); // 15 + 10
    }

    @Test
    public void testScheduleSameExamTwice() {
        session.scheduleExam(mathExam, 15);
        session.scheduleExam(mathExam, 20); // Update student count

        List<Exam> exams = session.getExams();
        assertEquals(1, exams.size()); // Should not duplicate
        assertEquals(20, session.countStudents()); // Should use updated count
    }

    @Test
    public void testGetStudentCountForExam() {
        session.scheduleExam(mathExam, 15);
        session.scheduleExam(englishExam, 10);

        assertEquals(15, session.getStudentCountForExam(mathExam));
        assertEquals(10, session.getStudentCountForExam(englishExam));

        // Test for exam not in session
        Subject physicsSubject = new Subject("Physics", "Study of matter", registry);
        Exam physicsExam = new Exam(physicsSubject, Exam.ExamType.EXTERNAL,
                10, 3, 2025, 9, 0, registry);
        assertEquals(0, session.getStudentCountForExam(physicsExam));
    }

    @Test
    public void testAllocateStudents() {
        // Create students - using constructor without registry for first two
        Student student1 = new Student(1111111111L, "Alice", "Anderson",
                1, 1, 2007, "Blue", false);
        Student student2 = new Student(2222222222L, "Bob", "Brown",
                1, 1, 2007, "Red", false);
        Student student3 = new Student(3333333333L, "Charlie", "Clark",
                1, 1, 2007, "Green", false);

        // Add subjects to students
        student1.addSubject(mathSubject);
        student2.addSubject(mathSubject);
        student3.addSubject(mathSubject);

        // Add students to list
        students.add(student1);
        students.add(student2);
        students.add(student3);

        // Create exam list
        ExamList exams = new ExamList(registry);
        exams.add(mathExam);

        // Schedule exam in session
        session.scheduleExam(mathExam, 3);

        // Allocate students
        session.allocateStudents(exams, students);

        // Test allocation happened (would need access to desk info to verify details)
        assertEquals(3, session.countStudents());
    }

    @Test
    public void testAllocateStudentsWithAara() {
        // Create AARA students - using constructor with AARA parameter
        Student aaraStudent1 = new Student(4444444444L, "Diana", "Davis",
                1, 1, 2007, "Blue", true);
        Student aaraStudent2 = new Student(5555555555L, "Emma", "Evans",
                1, 1, 2007, "Red", true);

        // Add subjects
        aaraStudent1.addSubject(mathSubject);
        aaraStudent2.addSubject(mathSubject);

        // Add to student list
        students.add(aaraStudent1);
        students.add(aaraStudent2);

        // Create exam list
        ExamList exams = new ExamList(registry);
        exams.add(mathExam);

        // Schedule in AARA session
        aaraSession.scheduleExam(mathExam, 2);

        // Allocate
        aaraSession.allocateStudents(exams, students);

        assertEquals(2, aaraSession.countStudents());
    }

    @Test
    public void testToString() {
        String expected = "V1: 1: 2025-03-10 09:00";
        assertEquals(expected, session.toString());
    }

    @Test
    public void testSessionWithDifferentVenues() {
        Session session1 = new Session(venue, 1,
                LocalDate.of(2025, 3, 10), LocalTime.of(9, 0));
        Session session2 = new Session(aaraVenue, 1,
                LocalDate.of(2025, 3, 10), LocalTime.of(9, 0));

        assertNotEquals(session1.getVenue(), session2.getVenue());
        assertEquals(session1.getSessionNumber(), session2.getSessionNumber());
    }

    @Test
    public void testPrintDesks() {
        // This test verifies the method runs without error
        // Actual output goes to System.out
        session.printDesks();
        // No exception should be thrown
    }

    @Test
    public void testCountStudentsEmpty() {
        assertEquals(0, session.countStudents());
    }

    @Test
    public void testGetExamsReturnsNewList() {
        session.scheduleExam(mathExam, 10);
        List<Exam> exams1 = session.getExams();
        List<Exam> exams2 = session.getExams();

        // Should return new list each time
        assertNotSame(exams1, exams2);
        assertEquals(exams1.size(), exams2.size());
    }

    @Test
    public void testSessionNumbersNotSequential() {
        // Session numbers don't have to be sequential
        Session session5 = new Session(venue, 5,
                LocalDate.of(2025, 3, 10), LocalTime.of(9, 0));
        Session session10 = new Session(venue, 10,
                LocalDate.of(2025, 3, 10), LocalTime.of(14, 0));

        assertEquals(5, session5.getSessionNumber());
        assertEquals(10, session10.getSessionNumber());
    }
}