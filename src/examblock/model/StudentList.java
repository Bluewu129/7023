package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Student}s.
 */
public class StudentList extends ListManager<Student> implements StreamManager {

    /**
     * Constructs a StudentList with registry.
     *
     * @param registry - registry
     */
    public StudentList(Registry registry) {
        super(Student::new, registry, Student.class);
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key - the text used to identify the item
     * @return the item if found or null
     */
    @Override
    public Student find(String key) {
        for (Student student : super.all()) {
            if (student.getId().equals(key)) {
                return student;
            }
        }
        return null;
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key - the text used to identify the item
     * @return the item if found
     * @throws IllegalStateException - if no item is found
     */
    @Override
    public Student get(String key) throws IllegalStateException {
        Student student = find(key);
        if (student == null) {
            throw new IllegalStateException("No student found with key: " + key);
        }
        return student;
    }

    /**
     * Get the Student with a matching LUI.
     *
     * @param lui - the LUI of the Student to be found.
     * @return Student with a matching LUI, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *         find a matching student as that indicates there is a misalignment of
     *         the executing state and the complete list of possible students.
     */
    public Student byLui(Long lui) throws IllegalStateException {
        for (Student student : super.all()) {
            if (student.getLui().equals(lui)) {
                return student;
            }
        }
        throw new IllegalStateException("No such student!");
    }

    /**
     * Counts the number of either non-AARA or AARA students taking a particular Subject.
     *
     * @param subject the subject to be found.
     * @param aara true to count AARA students or false to count non-AARA students.
     * @return The number of either non-AARA or AARA students taking a particular subject.
     */
    public int countStudents(Subject subject, boolean aara) {
        int count = 0;
        for (Student student : super.all()) {
            if (student.isAara() == aara) {
                List<Subject> subjects = student.getSubjects().all();
                for (Subject check : subjects) {
                    if (check.equals(subject)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Returns detailed string representations of the contents of this student list.
     *
     * @return detailed string representations of the contents of this student list.
     */
    public String getFullDetail() {

        String topLine = """
                /================================\\
                |----------  STUDENTS  ----------|
                \\================================/
                 
                """;

        StringBuilder studentStrings = new StringBuilder();
        for (Student student : super.all()) {
            studentStrings.append(student.getFullDetail());
            studentStrings.append("\n");
        }
        return topLine + studentStrings + "\n";
    }

    /**
     * Returns a minimal string representation of the contents of this student list.
     *
     * @return a minimal string representation of the contents of this student list.
     */
    @Override
    public String toString() {

        String topLine = """
                /================================\\
                |----------  STUDENTS  ----------|
                \\================================/
                 
                """;

        StringBuilder studentStrings = new StringBuilder();
        for (Student student : super.all()) {
            studentStrings.append(student.toString());
        }
        return studentStrings.toString();
    }
}