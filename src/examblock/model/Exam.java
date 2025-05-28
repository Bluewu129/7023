package examblock.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * An object describing a single Year 12 Exam.
 */
public class Exam {

    /**
     * An enum for the ExamType (INTERNAL or EXTERNAL).
     */
    public enum ExamType {
        /** Internal assessment, conducted by the school. */
        INTERNAL,
        /** External assessment, conducted by the QCAA. */
        EXTERNAL;
    }

    /** The Subject this exam is for. */
    private Subject subject;
    /** The type of exam being conducted INTERNAL or EXTERNAL. */
    private ExamType examType;
    /** An optional paper number (null or 1 or 2) for this exam. */
    private Character paper;
    /** An optional subtitle e.g. "Technology Free" for this exam. */
    private String subtitle;
    /** An optional unit ID if only one unit is applicable for this exam. */
    private Character unit;
    /** The date of this exam. */
    private LocalDate examDate;
    /** The start time for this exam. */
    private LocalTime examTime;

    /**
     * Constructs an {@code Exam} with minimal details.
     *
     * @param subject the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param day the integer day of the date of the exam, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of the exam, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of the exam,
     *             which must be 2025 or greater.
     * @param hour the 2-digit integer hour of the start of the exam window,
     *             in 24-hour time, which must be between 7 and 17.
     * @param minute the integer minute of the start of the exam window,
     *               which must be between 0 - 59 inclusive.
     */
    public Exam(Subject subject, ExamType examType,
                int day, int month, int year, int hour, int minute) {
        this(subject, examType, '\0', "", '\0', day, month, year, hour, minute);
    }

    /**
     * Constructs an {@code Exam} with the optional unit specified.
     *
     * @param subject the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param unit an optional unit ID if only one unit is applicable.
     * @param day the integer day of the date of the exam, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of the exam, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of the exam,
     *             which must be 2025 or greater.
     * @param hour the 2-digit integer hour of the start of the exam window,
     *             in 24-hour time, which must be between 7 and 17.
     * @param minute the integer minute of the start of the exam window,
     *               which must be between 0 - 59 inclusive.
     */
    public Exam(Subject subject, ExamType examType, Character unit,
                int day, int month, int year, int hour, int minute) {
        this(subject, examType, '\0', "", unit, day, month, year, hour, minute);
    }

    /**
     * Constructs an {@code Exam} with paper number and subtitle but no unit specified.
     *
     * @param subject the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param paper an optional paper number (null or 1 or 2).
     * @param subtitle an optional subtitle e.g. "Technology Free".
     * @param day the integer day of the date of the exam, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of the exam, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of the exam,
     *             which must be 2025 or greater.
     * @param hour the 2-digit integer hour of the start of the exam window,
     *             in 24-hour time, which must be between 7 and 17.
     * @param minute the integer minute of the start of the exam window,
     *               which must be between 0 - 59 inclusive.
     */
    public Exam(Subject subject, ExamType examType, Character paper, String subtitle,
                int day, int month, int year, int hour, int minute) {
        this(subject, examType, paper, subtitle, '\0', day, month, year, hour, minute);
    }

    /**
     * Constructs an {@code Exam} with all optional details provided.
     *
     * @param subject the exam Subject.
     * @param examType one of INTERNAL or EXTERNAL.
     * @param paper an optional paper number (null or 1 or 2).
     * @param subtitle an optional subtitle e.g. "Technology Free".
     * @param unit an optional unit ID if only one unit is applicable.
     * @param day the integer day of the date of the exam, which must
     *            be a valid day for the month and year provided.
     * @param month the integer month of the date of the exam, which
     *              must be between 1 - 12 inclusive.
     * @param year the 4-digit integer year of the date of the exam,
     *             which must be 2025 or greater.
     * @param hour the 2-digit integer hour of the start of the exam window,
     *             in 24-hour time, which must be between 7 and 17.
     * @param minute the integer minute of the start of the exam window,
     *               which must be between 0 - 59 inclusive.
     */
    public Exam(Subject subject, ExamType examType, Character paper, String subtitle,
                Character unit, int day, int month, int year, int hour, int minute) {
        this.subject = subject;
        this.examType = examType;
        this.paper = paper;
        this.subtitle = subtitle;
        this.unit = unit;
        examDate = LocalDate.of(year, month, day);
        examTime = LocalTime.of(hour, minute);
    }

    /**
     * Gets the subject of the exam.
     *
     * @return subject of the exam
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Gets the full title of the exam.
     * Provides the exam type,
     * and then on a new line, the exam subject, and any paper identifier (if more than one),
     * and then on a new line, any subtitle (only if present).
     *
     * @return the full text title of the exam (type\n, subject, paper\n, subtitle)
     */
    public String getTitle() {
        StringBuilder title = new StringBuilder();
        title.append("Year 12 ");
        if (examType == ExamType.EXTERNAL) {
            title.append("External ");
        } else {
            title.append("Internal ");
        }
        title.append("Assessment\n");
        title.append(subject.getTitle());
        if (paper != '\0') {
            title.append(" Paper " + paper);
        }
        if (subtitle != "") {
            title.append("\n" + subtitle);
        }
        title.append("\n");
        return title.toString();
    }

    /**
     * Gets the short title of the exam with no subtitle.
     * Provides type, subject, and any paper identifier (if more than one), all on one line.
     *
     * @return text title of the exam (type, subject, paper)
     */
    public String getShortTitle() {
        StringBuilder title = new StringBuilder();
        title.append("Year 12 ");
        if (examType == ExamType.EXTERNAL) {
            title.append("External ");
        } else {
            title.append("Internal ");
        }
        title.append("Assessment ");
        title.append(subject.getTitle());
        if (paper != '\0') {
            title.append(" Paper " + paper);
        }
        return title.toString();
    }

    /**
     * Gets the date of this exam.
     *
     * @return the date of this exam.
     */
    public LocalDate getDate() {
        return examDate;
    }

    /**
     * Gets the start time of this exam's window.
     *
     * @return the start time of this exam's window.
     */
    public LocalTime getTime() {
        return examTime;
    }

    /**
     * Returns a detailed string representation of this exam.
     *
     * @return a detailed string representation of this exam.
     */
    public String getFullDetail() {
        return this.getSubject().toString().toUpperCase() + "\n" + this.getTitle();
    }

    /**
     * Returns a brief string representation of the exam.
     *
     * @return a brief string representation of the exam.
     */
    @Override
    public String toString() {
        return this.getShortTitle();
    }
}
