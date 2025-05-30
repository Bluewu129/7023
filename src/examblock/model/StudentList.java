package examblock.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

/**
 * A collection object for holding and managing {@link Student}s.
 * Updated to extend ListManager for streaming support.
 */
public class StudentList extends ListManager<Student> {

    /**
     * Factory for creating Student instances.
     */
    private static class StudentFactory implements ItemFactory<Student> {
        @Override
        public Student createItem(BufferedReader br, Registry registry, int index) throws IOException {
            return new Student(br, registry, index);
        }
    }

    /**
     * Constructs a StudentList with a specific registry.
     *
     * @param registry the registry to use
     */
    public StudentList(Registry registry) {
        super(new StudentFactory(), registry, Student.class);
    }

    /**
     * Get the {@link Student} with a matching {@code LUI}.
     *
     * @param lui - the {@code LUI} of the {@link Student} to be found.
     * @return {@link Student} with a matching {@code LUI}, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *         find a matching student as that indicates there is a misalignment of
     *         the executing state and the complete list of possible students.
     */
    public Student byLui(Long lui) throws IllegalStateException {
        Student student = find(String.valueOf(lui));
        if (student != null) {
            return student;
        }
        throw new IllegalStateException("No such student!");
    }

    @Override
    public Student find(String key) {
        Optional<Student> student = super.all()
                .stream()
                .filter(s -> s.getId().equals(key))
                .findFirst();

        return student.orElse(null);
    }

    @Override
    public Student get(String key) throws IllegalStateException {
        Student student = find(key);
        if (student != null) {
            return student;
        }
        throw new IllegalStateException("Item with ID " + key + " not found for type Student");
    }

    /**
     * Counts the number of either non-AARA or AARA students taking a particular {@link Subject}.
     *
     * @param subject the subject to be found.
     * @param aara true to count AARA students or false to count non-AARA students.
     * @return The number of either non-AARA or AARA students taking a particular subject.
     */
    public int countStudents(Subject subject, boolean aara) {
        int count = 0;
        for (Student student : getItems()) {
            if (student.isAara() == aara) {
                // Use the existing SubjectList's methods
                SubjectList studentSubjects = student.getSubjects();
                for (Subject check : studentSubjects.getItems()) {
                    if (check == subject) {
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
        for (Student student : getItems()) {
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
        for (Student student : getItems()) {
            studentStrings.append(student.toString());
        }
        return studentStrings.toString();
    }
}