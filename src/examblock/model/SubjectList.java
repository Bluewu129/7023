package examblock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Subject}s.
 */
public class SubjectList {

    /** This instance's list of subjects. */
    private final List<Subject> subjects;

    /**
     * Constructs an empty list of {@link Subject}s.
     */
    public SubjectList() {
        subjects = new ArrayList<>();
    }

    /**
     * Adds a {@link Subject} to this list of {@link Subject}s.
     *
     * @param subject - the subject object being added to this list.
     */
    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    /**
     * Removes a given {@link Subject} from the {@code SubjectList}.
     *
     * @param subject - the subject to remove from this list.
     */
    public void removeSubject(Subject subject) {
        subjects.remove(subject);
    }

    /**
     * Get the first {@link Subject} with a matching {@code title}.
     *
     * @param title the {@code title} of the {@link Subject} to be found.
     * @return The first {@link Subject} with a matching {@code title}, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't
     *         find a matching subject as that indicates there is a misalignment of
     *         the executing state and the complete list of possible subjects.
     */
    public Subject byTitle(String title) throws IllegalStateException {
        for (Subject subject : this.subjects) {
            if (subject.getTitle().equals(title)) {
                return subject;
            }
        }
        throw new IllegalStateException("No such subject!");
    }

    /**
     * Creates a new {@code List} holding {@code references} to all the {@link Subject}s
     * managed by this {@code SubjectList} and returns it.
     *
     * @return a new {@code List} holding {@code references} to all the {@link Subject}s
     * managed by this {@code SubjectList}.
     */
    public List<Subject> all() {
        return new ArrayList<>(this.subjects);
    }

    /**
     * Returns detailed string representations of the contents of this subject list.
     *
     * @return detailed string representations of the contents of this subject list.
     */
    public String getFullDetail() {

        StringBuilder subjectStrings = new StringBuilder();
        int counter = 1;
        for (Subject subject : this.subjects) {
            subjectStrings.append(counter);
            subjectStrings.append(". ");
            subjectStrings.append(subject.getFullDetail());
            counter += 1;
        }
        return subjectStrings + "\n";
    }

    /**
     * Returns a brief string representation of the contents of this subject list.
     *
     * @return a brief string representation of the contents of this subject list.
     */
    @Override
    public String toString() {

        StringBuilder subjectStrings = new StringBuilder();
        int counter = 1;
        for (Subject subject : this.subjects) {
            subjectStrings.append(counter);
            subjectStrings.append(". ");
            subjectStrings.append(subject.toString());
            counter += 1;
        }
        return subjectStrings.toString();
    }
}