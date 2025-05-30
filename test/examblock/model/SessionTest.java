package examblock.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for Session functionality
 */
public class SessionTest {

    private Registry registry;
    private Venue testVenue;
    private Venue aaraVenue;
    private Session testSession;
    private Subject mathSubject;
    private Subject physicsSubject;
    private Exam mathExam;
    private Exam physicsExam;
    private Student student1;
    private Student student2;
    private Student aaraStudent;

    @Before
    public void setUp() {
        registry = new RegistryImpl();

        // Create venue
        RoomList roomList = new RoomList(registry);
        Room room1 = new Room("R1", registry);
        roomList.add(room1);
        testVenue = new Venue("V1", 1, roomList, 5, 5, 25, false, registry);

        // Create AARA venue
        RoomList aaraRoomList = new RoomList(registry);
        Room aaraRoom = new Room("S101", registry);
        aaraRoomList.add(aaraRoom);
        aaraVenue = new Venue("W1", 1, aaraRoomList, 1, 5, 5, true, registry);

        // Create subjects
        mathSubject = new Subject("Mathematics", "Study of numbers.", registry);
        physicsSubject = new Subject("Physics", "Study of matter and energy.", registry);

        // Create exams
        mathExam = new Exam(mathSubject, Exam.ExamType.INTERNAL,
                10, 3, 2025, 8, 30, registry);
        physicsExam = new Exam(physicsSubject, Exam.ExamType.INTERNAL,
                10, 3, 2025, 12, 30, registry);

        // Create students
        student1 = new Student(9999111111L, "John", "Smith", 1, 1, 2008, "Blue", false, registry);
        student2 = new Student(9999222222L, "Jane", "Doe", 2, 2, 2008, "Red", false, registry);
        aaraStudent = new Student(9999333333L, "Bob", "Wilson", 3, 3, 2008, "Green", true, registry);

        // Add subjects to students
        student1.addSubject(mathSubject);
        student2.addSubject(mathSubject);
        student2.addSubject(physicsSubject);
        aaraStudent.addSubject(mathSubject);

        // Create session
        testSession = new Session(testVenue, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
    }

    @After
    public void tearDown() {
        registry = null;
        testVenue = null;
        aaraVenue = null;
        testSession = null;
        mathSubject = null;
        physicsSubject = null;
        mathExam = null;
        physicsExam = null;
        student1 = null;
        student2 = null;
        aaraStudent = null;
    }

    @Test
    public void testBasicConstruction() {
        assertEquals(testVenue, testSession.getVenue());
        assertEquals(1, testSession.getSessionNumber());
        assertEquals(LocalDate.of(2025, 3, 10), testSession.getDate());
        assertEquals(LocalTime.of(8, 30), testSession.getTime());
        assertEquals("V1_1", testSession.getId());
        assertEquals(0, testSession.getExams().size());
        assertEquals(25, testSession.getTotalDesks());
    }

    @Test
    public void testConstructionWithNullVenue() {
        Session nullVenueSession = new Session(null, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);

        assertNull(nullVenueSession.getVenue());
        assertEquals(0, nullVenueSession.getTotalDesks());
        assertNull(nullVenueSession.desks);
    }

    @Test
    public void testExamScheduling() {
        // Initially no exams
        assertEquals(0, testSession.getExams().size());

        // Schedule math exam
        testSession.scheduleExam(mathExam);
        assertEquals(1, testSession.getExams().size());
        assertTrue(testSession.getExams().contains(mathExam));

        // Schedule physics exam
        testSession.scheduleExam(physicsExam);
        assertEquals(2, testSession.getExams().size());
        assertTrue(testSession.getExams().contains(physicsExam));

        // Try to schedule same exam again (should not duplicate)
        testSession.scheduleExam(mathExam);
        assertEquals(2, testSession.getExams().size());
    }

    @Test
    public void testExamRemoval() {
        testSession.scheduleExam(mathExam);
        testSession.scheduleExam(physicsExam);
        assertEquals(2, testSession.getExams().size());

        testSession.removeExam(mathExam);
        assertEquals(1, testSession.getExams().size());
        assertFalse(testSession.getExams().contains(mathExam));
        assertTrue(testSession.getExams().contains(physicsExam));
    }

    @Test
    public void testCountStudents() {
        // Initially no students
        assertEquals(0, testSession.countStudents());

        // Schedule math exam
        testSession.scheduleExam(mathExam);

        // Should count non-AARA students for non-AARA venue
        int expectedCount = 2; // student1 and student2 have math, aaraStudent doesn't match venue type
        assertEquals(expectedCount, testSession.countStudents());
    }

    @Test
    public void testCountStudentsWithAARAVenue() {
        Session aaraSession = new Session(aaraVenue, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
        aaraSession.scheduleExam(mathExam);

        // Should count only AARA students for AARA venue
        int expectedCount = 1; // Only aaraStudent matches venue type and has math
        assertEquals(expectedCount, aaraSession.countStudents());
    }

    @Test
    public void testGetFullDetail() {
        testSession.scheduleExam(mathExam);
        String detail = testSession.getFullDetail();

        assertTrue(detail.contains("Session: V1_1"));
        assertTrue(detail.contains("Venue: V1"));
        assertTrue(detail.contains("Session Number: 1"));
        assertTrue(detail.contains("Date: 2025-03-10"));
        assertTrue(detail.contains("Time: 08:30"));
        assertTrue(detail.contains("Students: 2")); // Non-AARA students
        assertTrue(detail.contains("Exams: 1"));
    }

    @Test
    public void testToString() {
        String expected = "V1: 1: 2025-03-10 08:30";
        assertEquals(expected, testSession.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        Session session1 = new Session(testVenue, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
        Session session2 = new Session(testVenue, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
        Session session3 = new Session(testVenue, 2, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);

        // Test equals
        assertTrue(session1.equals(session2));
        assertFalse(session1.equals(session3));
        assertFalse(session1.equals(null));
        assertFalse(session1.equals("Not a session"));

        // Test reflexivity
        assertTrue(session1.equals(session1));

        // Test hash code consistency
        assertEquals(session1.hashCode(), session2.hashCode());
    }

    @Test
    public void testAllocateStudents() {
        // Schedule exam and allocate students
        testSession.scheduleExam(mathExam);

        ExamList examList = new ExamList(registry);
        examList.add(mathExam);

        StudentList studentList = new StudentList(registry);
        studentList.add(student1);
        studentList.add(student2);
        studentList.add(aaraStudent); // Won't be allocated to non-AARA venue

        testSession.allocateStudents(examList, studentList);

        // Check that students are allocated to desks
        assertNotNull(testSession.desks);

        boolean foundStudent1 = false;
        boolean foundStudent2 = false;

        for (int i = 0; i < testVenue.getRows(); i++) {
            for (int j = 0; j < testVenue.getColumns(); j++) {
                if (testSession.desks[i][j] != null) {
                    String familyName = testSession.desks[i][j].deskFamilyName();
                    if ("Smith".equals(familyName)) foundStudent1 = true;
                    if ("Doe".equals(familyName)) foundStudent2 = true;
                }
            }
        }

        assertTrue("Student1 should be allocated", foundStudent1);
        assertTrue("Student2 should be allocated", foundStudent2);
    }

    @Test
    public void testStreamOut() throws IOException {
        testSession.scheduleExam(mathExam);

        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        testSession.streamOut(bufferedWriter, 1);
        bufferedWriter.flush();

        String output = stringWriter.toString();
        String[] lines = output.split(System.lineSeparator());

        assertTrue(lines[0].contains("1. Venue: V1"));
        assertTrue(lines[0].contains("Session Number: 1"));
        assertTrue(lines[0].contains("Day: 2025-03-10"));
        assertTrue(lines[0].contains("Start: 08:30"));
        assertTrue(lines[0].contains("Exams: 1"));
        assertTrue(lines[1].contains("Year 12 Internal Assessment Mathematics"));
        assertTrue(lines[2].contains("[Desks: 0, Session not finalised]"));
    }

    @Test
    public void testStreamOutWithAllocatedStudents() throws IOException {
        testSession.scheduleExam(mathExam);

        // Manually allocate a student to test desk output
        testSession.desks[0][0].setStudent(student1);
        testSession.desks[0][0].setExam(mathExam);

        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        testSession.streamOut(bufferedWriter, 1);
        bufferedWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("[Desks: 1]"));
        assertTrue(output.contains("Desk: 1"));
        assertTrue(output.contains("LUI: 9999111111"));
        assertTrue(output.contains("Name: Smith"));
    }

    @Test
    public void testStreamIn() throws IOException {
        String input = "1. Venue: V1, Session Number: 1, Day: 2025-03-10, Start: 08:30, Exams: 1" +
                System.lineSeparator() +
                "Year 12 Internal Assessment Mathematics" + System.lineSeparator() +
                "[Desks: 0, Session not finalised]" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Session session = new Session(reader, registry, 1);

        assertEquals(testVenue, session.getVenue());
        assertEquals(1, session.getSessionNumber());
        assertEquals(LocalDate.of(2025, 3, 10), session.getDate());
        assertEquals(LocalTime.of(8, 30), session.getTime());
        assertEquals(1, session.getExams().size());
    }

    @Test
    public void testStreamInWithAllocatedDesks() throws IOException {
        String input = "1. Venue: V1, Session Number: 1, Day: 2025-03-10, Start: 08:30, Exams: 1" +
                System.lineSeparator() +
                "Year 12 Internal Assessment Mathematics" + System.lineSeparator() +
                "[Desks: 1]" + System.lineSeparator() +
                "Desk: 1, LUI: 9999111111, Name: Smith, John" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        Session session = new Session(reader, registry, 1);

        assertNotNull(session.desks);
        assertEquals("Smith", session.desks[0][0].deskFamilyName());
        assertEquals(9999111111L, session.desks[0][0].deskLui());
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidIndex() throws IOException {
        String input = "2. Venue: V1, Session Number: 1, Day: 2025-03-10, Start: 08:30, Exams: 1" +
                System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Session(reader, registry, 1); // Expected index 1, but input has 2
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithInvalidFormat() throws IOException {
        String input = "Invalid format line" + System.lineSeparator();

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Session(reader, registry, 1);
    }

    @Test(expected = RuntimeException.class)
    public void testStreamInWithEOF() throws IOException {
        String input = "1. Venue: V1, Session Number: 1, Day: 2025-03-10, Start: 08:30, Exams: 1" +
                System.lineSeparator();
        // Missing exam and desk information

        BufferedReader reader = new BufferedReader(new StringReader(input));
        new Session(reader, registry, 1);
    }

    @Test
    public void testRegistryIntegration() {
        // Test that session is added to registry
        assertTrue(registry.contains("V1_1", Session.class));
        assertEquals(testSession, registry.find("V1_1", Session.class));
    }

    @Test
    public void testPrintDesks() {
        testSession.scheduleExam(mathExam);

        // Allocate some students
        testSession.desks[0][0].setStudent(student1);
        testSession.desks[0][1].setStudent(student2);

        // Test that printDesks doesn't throw exception
        testSession.printDesks();

        // Test StringBuilder version
        StringBuilder sb = new StringBuilder();
        testSession.printDesks(sb);
        String output = sb.toString();

        assertTrue(output.contains("Desk 1"));
        assertTrue(output.contains("Desk 2"));
        assertTrue(output.contains("Smith"));
        assertTrue(output.contains("Doe"));
    }

    @Test
    public void testPrintDesksWithNullDesks() {
        // Create session with null venue (results in null desks)
        Session nullSession = new Session(null, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);

        // Should handle null desks gracefully
        nullSession.printDesks();

        StringBuilder sb = new StringBuilder();
        nullSession.printDesks(sb);
        // Should not add anything to StringBuilder when desks is null
        assertEquals(0, sb.length());
    }

    @Test
    public void testMultipleExamsInSession() {
        testSession.scheduleExam(mathExam);
        testSession.scheduleExam(physicsExam);

        List<Exam> exams = testSession.getExams();
        assertEquals(2, exams.size());
        assertTrue(exams.contains(mathExam));
        assertTrue(exams.contains(physicsExam));
    }

    @Test
    public void testSessionWithDifferentTimes() {
        Session morningSession = new Session(testVenue, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
        Session afternoonSession = new Session(testVenue, 2, LocalDate.of(2025, 3, 10),
                LocalTime.of(12, 30), registry);

        assertNotEquals(morningSession.getTime(), afternoonSession.getTime());
        assertNotEquals(morningSession.getId(), afternoonSession.getId());
    }

    @Test
    public void testSessionWithDifferentDates() {
        Session day1Session = new Session(testVenue, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
        Session day2Session = new Session(testVenue, 1, LocalDate.of(2025, 3, 11),
                LocalTime.of(8, 30), registry);

        assertNotEquals(day1Session.getDate(), day2Session.getDate());
        // Same venue and session number but different dates should be different sessions
        assertFalse(day1Session.equals(day2Session));
    }

    @Test
    public void testCountStudentsWithNoExams() {
        // Session with no exams should have no students
        assertEquals(0, testSession.countStudents());
    }

    @Test
    public void testCountStudentsWithMultipleExams() {
        testSession.scheduleExam(mathExam);
        testSession.scheduleExam(physicsExam);

        // student1 has math only = 1 student
        // student2 has both math and physics = 2 student-exam pairs
        // Total should be 3 (student1 for math + student2 for math + student2 for physics)
        int expectedCount = 3;
        assertEquals(expectedCount, testSession.countStudents());
    }

    @Test
    public void testAllocateStudentsWithInsufficientSpace() {
        // Create a small venue
        RoomList smallRoomList = new RoomList(registry);
        Room smallRoom = new Room("Small", registry);
        smallRoomList.add(smallRoom);
        Venue smallVenue = new Venue("Small", 1, smallRoomList, 1, 1, 1, false, registry);

        Session smallSession = new Session(smallVenue, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
        smallSession.scheduleExam(mathExam);

        StudentList studentList = new StudentList(registry);
        studentList.add(student1);
        studentList.add(student2);

        ExamList examList = new ExamList(registry);
        examList.add(mathExam);

        // Should handle allocation when there are more students than desks
        smallSession.allocateStudents(examList, studentList);

        // Only one desk should be filled
        int allocatedDesks = 0;
        for (int i = 0; i < smallVenue.getRows(); i++) {
            for (int j = 0; j < smallVenue.getColumns(); j++) {
                if (smallSession.desks[i][j] != null &&
                        smallSession.desks[i][j].deskFamilyName() != null &&
                        !smallSession.desks[i][j].deskFamilyName().trim().isEmpty()) {
                    allocatedDesks++;
                }
            }
        }
        assertTrue("Should allocate at least one desk", allocatedDesks >= 1);
        assertTrue("Should not allocate more desks than available", allocatedDesks <= 1);
    }

    @Test
    public void testSessionIdGeneration() {
        Session session1 = new Session(testVenue, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
        Session session2 = new Session(testVenue, 2, LocalDate.of(2025, 3, 10),
                LocalTime.of(12, 30), registry);

        assertEquals("V1_1", session1.getId());
        assertEquals("V1_2", session2.getId());
        assertNotEquals(session1.getId(), session2.getId());
    }

    @Test
    public void testSessionWithNullVenueId() {
        Session nullVenueSession = new Session(null, 1, LocalDate.of(2025, 3, 10),
                LocalTime.of(8, 30), registry);
        assertEquals("null_1", nullVenueSession.getId());
    }
}