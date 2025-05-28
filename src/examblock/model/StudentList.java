package examblock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Student}s.
 */
public class StudentList {

    /** This instance's list of students. */
    private final List<Student> students;

    /**
     * Constructs an empty list of {@link Student}s.
     */
    public StudentList() {
        this.students = new ArrayList<>();
    }

    /**
     * Adds a {@link Student} to this list of {@link Student}s.
     *
     * @param student - the student object being added to this list.
     */
    public void add(Student student) {
        this.students.add(student);
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
        for (Student student : this.students) {
            if (student.getLui() == lui) {
                return student;
            }
        }
        throw new IllegalStateException("No such student!");
    }

    /**
     * Creates a new {@code List} holding {@code references} to all the {@link Student}s
     * managed by this {@code StudentList} and returns it.
     *
     * @return a new {@code List} holding {@code references} to all the {@link Student}s
     * managed by this {@code StudentList}.
     */
    public List<Student> all() {
        return new ArrayList<>(this.students);
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
        for (Student student : this.students) {
            if (student.isAara() == aara) {
                List<Subject> subjects = student.getSubjects().all();
                for (Subject check : subjects) {
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
        for (Student student : this.students) {
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
        for (Student student : this.students) {
            studentStrings.append(student.toString());
        }
        return studentStrings.toString();
    }
}
