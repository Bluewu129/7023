package examblock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A collection object for holding and managing {@link Subject}s.
 * Updated to extend ListManager for streaming support.
 */
public class SubjectList extends ListManager<Subject> {

    /**
     * Constructs an empty list of {@link Subject}s.
     */
    public SubjectList() {
        super(Subject::new, new RegistryImpl(), Subject.class);
    }

    /**
     * Constructs a SubjectList with a specific registry.
     *
     * @param registry the registry to use
     */
    public SubjectList(Registry registry) {
        super(Subject::new, registry, Subject.class);
    }

    /**
     * Adds a {@link Subject} to this list of {@link Subject}s.
     *
     * @param subject - the subject object being added to this list.
     */
    public void addSubject(Subject subject) {
        add(subject);
    }

    /**
     * Removes a given {@link Subject} from the {@code SubjectList}.
     *
     * @param subject - the subject to remove from this list.
     */
    public void removeSubject(Subject subject) {
        remove(subject);
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
        Subject subject = find(title);
        if (subject != null) {
            return subject;
        }
        throw new IllegalStateException("No such subject!");
    }

    @Override
    public Subject find(String key) {
        Optional<Subject> subject = super.all()
                .stream()
                .filter(s -> s.getTitle().equals(key))
                .findFirst();

        return subject.orElse(null);
    }

    @Override
    public Subject get(String key) throws IllegalStateException {
        Subject subject = find(key);
        if (subject != null) {
            return subject;
        }
        throw new IllegalStateException("Item with ID " + key + " not found for type Subject");
    }

    /**
     * Creates a new {@code List} holding {@code references} to all the {@link Subject}s
     * managed by this {@code SubjectList} and returns it.
     *
     * @return a new {@code List} holding {@code references} to all the {@link Subject}s
     * managed by this {@code SubjectList}.
     */
    public ArrayList<Subject> all() {
        return super.all();
    }

    /**
     * Returns detailed string representations of the contents of this subject list.
     *
     * @return detailed string representations of the contents of this subject list.
     */
    public String getFullDetail() {
        StringBuilder subjectStrings = new StringBuilder();
        int counter = 1;
        for (Subject subject : getItems()) {
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
        for (Subject subject : getItems()) {
            subjectStrings.append(counter);
            subjectStrings.append(". ");
            subjectStrings.append(subject.toString());
            counter += 1;
        }
        return subjectStrings.toString();
    }
}