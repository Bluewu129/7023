package examblock.model;

import java.util.Optional;

/**
 * A collection object for holding and managing {@link Exam}s.
 * Updated to extend ListManager for streaming support.
 */
public class ExamList extends ListManager<Exam> {

    /**
     * Constructs an ExamList with a specific registry.
     */
    public ExamList(Registry registry) {
        super(Exam::new, registry, Exam.class);
    }

    /**
     * Get the first {@link Exam} with a matching short title.
     */
    public Exam byShortTitle(String shortTitle) throws IllegalStateException {
        for (Exam exam : getItems()) {
            if (exam.getShortTitle().equals(shortTitle)) {
                return exam;
            }
        }
        throw new IllegalStateException("No such exam!");
    }

    /**
     * Get the first {@link Exam} with a matching {@link Subject} {@code title}.
     */
    public Exam bySubjectTitle(String title) throws IllegalStateException {
        for (Exam exam : getItems()) {
            if (exam.getSubject().getTitle().equals(title)) {
                return exam;
            }
        }
        throw new IllegalStateException("No such exam!");
    }

    @Override
    public Exam find(String key) {
        // First try to find by subject title
        Optional<Exam> exam = super.all()
                .stream()
                .filter(e -> e.getSubject().getTitle().equals(key))
                .findFirst();

        if (exam.isPresent()) {
            return exam.get();
        }

        // If not found, try finding in registry by ID
        return getRegistry().find(key, Exam.class);
    }

    @Override
    public Exam get(String key) throws IllegalStateException {
        Exam exam = find(key);
        if (exam != null) {
            return exam;
        }
        throw new IllegalStateException("Item with ID " + key + " not found for type Exam");
    }

    /**
     * Returns detailed string representations of the contents of this exam list.
     */
    public String getFullDetail() {
        StringBuilder examStrings = new StringBuilder();
        examStrings.append("Exams:\n");
        int counter = 1;
        for (Exam exam : getItems()) {
            examStrings.append(counter);
            examStrings.append(". ");
            examStrings.append(exam.getFullDetail());
            examStrings.append("\n");
            counter += 1;
        }
        return examStrings.toString();
    }

    /**
     * Returns a string representation of the contents of the exam manager
     */
    @Override
    public String toString() {
        StringBuilder examStrings = new StringBuilder();
        int counter = 1;
        for (Exam exam : getItems()) {
            examStrings.append(counter);
            examStrings.append(". ");
            examStrings.append(exam.toString());
            examStrings.append("\n");
            counter += 1;
        }
        return examStrings.toString();
    }
}