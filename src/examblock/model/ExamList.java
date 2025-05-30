package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A collection object for holding and managing Exams.
 */
public class ExamList extends ListManager<Exam> implements StreamManager {

    /**
     * Constructs an empty list of Exams with the registry.
     *
     * @param registry - registry
     */
    public ExamList(Registry registry) {
        super(Exam::new, registry, Exam.class);
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key - the text used to identify the item
     * @return the item if found or null
     */
    @Override
    public Exam find(String key) {
        Optional<Exam> exam = all().stream()
                .filter(e -> e.getId().equals(key))
                .findFirst();

        return exam.orElse(null);
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key - the text used to identify the item
     * @return the item if found
     * @throws IllegalStateException - if no item is found
     */
    @Override
    public Exam get(String key) throws IllegalStateException {
        Exam exam = find(key);
        if (exam == null) {
            throw new IllegalStateException("No exam found with key: " + key);
        }
        return exam;
    }

    /**
     * Get the first Exam with a matching Subject title.
     *
     * @param title - the title of the Exam's Subject to be found.
     * @return first Exam with a matching Subject title, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *         find a matching exam as that indicates there is a misalignment of
     *         the executing state and the complete list of possible exams.
     */
    public Exam bySubjectTitle(String title) throws IllegalStateException {
        Optional<Exam> exam = all().stream()
                .filter(e -> e.getSubject().getTitle().equals(title))
                .findFirst();

        if (exam.isPresent()) {
            return exam.get();
        }
        throw new IllegalStateException("No such exam with subject title: " + title);
    }

    /**
     * Returns detailed string representations of the contents of this exam list.
     *
     * @return detailed string representations of the contents of this exam list.
     */
    public String getFullDetail() {
        StringBuilder examStrings = new StringBuilder();
        examStrings.append("Exams:\n");
        int counter = 1;
        for (Exam exam : all()) {
            examStrings.append(counter);
            examStrings.append(". ");
            examStrings.append(exam.getFullDetail());
            examStrings.append("\n");
            counter += 1;
        }
        return examStrings.toString();
    }

    /**
     * Finds an exam given it's short title
     *
     * @param shortTitle - the exam to find
     * @return the exam matching the short title, which looks like this:
     * "Year 12 Internal Assessment The Exam Name Paper N"
     * @throws IllegalStateException - if the exam can't be found
     */
    public Exam byShortTitle(String shortTitle) throws IllegalStateException {
        Optional<Exam> exam = all().stream()
                .filter(e -> e.getShortTitle().equals(shortTitle))
                .findFirst();

        if (exam.isPresent()) {
            return exam.get();
        }
        throw new IllegalStateException("No exam found with short title: " + shortTitle);
    }

    /**
     * Returns a string representation of the contents of the exam manager
     *
     * @return a string representation of the contents of the exam manager
     */
    @Override
    public String toString() {
        StringBuilder examStrings = new StringBuilder();
        int counter = 1;
        for (Exam exam : all()) {
            examStrings.append(counter);
            examStrings.append(". ");
            examStrings.append(exam.toString());
            examStrings.append("\n");
            counter += 1;
        }
        return examStrings.toString();
    }
}