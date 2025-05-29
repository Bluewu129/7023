package examblock.model;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubjectTest {
    private Registry registry;

    @Before
    public void setUp() {
        registry = new RegistryImpl();
    }

    @Test
    public void testSubjectCreation() {
        Subject subject = new Subject("Mathematics", "Study of numbers", registry);
        assertEquals("Mathematics", subject.getTitle());
        assertEquals("Study of numbers", subject.getDescription());
    }

    @Test
    public void testSubjectEquality() {
        Subject subject1 = new Subject("English", "Language arts", registry);
        Subject subject2 = new Subject("English", "Different description", registry);
        assertEquals(subject1, subject2); // Should be equal based on title
    }

    @Test
    public void testSubjectStreaming() throws IOException {
        Subject original = new Subject("Physics", "Study of matter", registry);

        // Write to stream
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        original.streamOut(bw, 1);
        bw.close();

        // Read from stream
        StringReader sr = new StringReader(sw.toString());
        BufferedReader br = new BufferedReader(sr);
        Subject loaded = new Subject(br, registry, 1);

        assertEquals(original.getTitle(), loaded.getTitle());
        assertEquals(original.getDescription(), loaded.getDescription());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidStreamFormat() throws IOException {
        StringReader sr = new StringReader("Invalid format");
        BufferedReader br = new BufferedReader(sr);
        new Subject(br, registry, 1); // Should throw RuntimeException
    }

    @Test
    public void testGetId() {
        Subject subject = new Subject("Chemistry", "Study of elements", registry);
        assertEquals("Chemistry", subject.getId());
    }

    @Test
    public void testToString() {
        Subject subject = new Subject("Biology", "Study of life", registry);
        assertTrue(subject.toString().contains("BIOLOGY"));
    }
}