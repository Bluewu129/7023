package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Subject}s.
 */
public class SubjectList extends ListManager<Subject> implements StreamManager {

    /**
     * Constructs a SubjectList with registry.
     *
     * @param registry - registry
     */
    public SubjectList(Registry registry) {
        super(Subject:: new, registry, Subject.class);
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key - the text used to identify the item
     * @return the item if found or null
     */
    @Override
    public Subject find(String key) {
        for (Subject subject : super.all()) {
            if (subject.getId().equals(key)) {
                return subject;
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
    public Subject get(String key) throws IllegalStateException {
        Subject subject = find(key);
        if (subject == null) {
            throw new IllegalStateException("No subject found with key: " + key);
        }
        return subject;
    }

    /**
     * Get the first Subject with a matching title.
     *
     * @param title the title of the Subject to be found.
     * @return The first Subject with a matching title, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't
     *         find a matching subject as that indicates there is a misalignment of
     *         the executing state and the complete list of possible subjects.
     */
    public Subject byTitle(String title) throws IllegalStateException {
        for (Subject subject : super.all()) {
            if (subject.getTitle().equals(title)) {
                return subject;
            }
        }
        throw new IllegalStateException("No such subject!");
    }

    /**
     * Returns detailed string representations of the contents of this subject list.
     *
     * @return detailed string representations of the contents of this subject list.
     */
    public String getFullDetail() {

        StringBuilder subjectStrings = new StringBuilder();
        int counter = 1;
        for (Subject subject : super.all()) {
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
        for (Subject subject : super.all()) {
            subjectStrings.append(counter);
            subjectStrings.append(". ");
            subjectStrings.append(subject.toString());
            counter += 1;
        }
        return subjectStrings.toString();
    }
}