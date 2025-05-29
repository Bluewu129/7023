package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Session}s.
 */
public class SessionList implements StreamManager {

    /** This instance's list of sessions. */
    private final List<Session> sessions;
    /** The registry for dependencies. */
    private final Registry registry;

    /**
     * Constructs a new empty SessionList.
     * As per specification - requires Registry parameter
     *
     * @param registry the registry for dependencies
     */
    public SessionList(Registry registry) {
        this.sessions = new ArrayList<>();
        this.registry = registry;
    }

    /**
     * Constructs a new empty SessionList without registry.
     * For backward compatibility
     */
    @Deprecated
    public SessionList() {
        this(null);
    }

    /**
     * Add the given {@link Session} to this {@code SessionList} for it to manage.
     *
     * @param session the given {@link Session} for this {@code SessionList} to manage.
     */
    public void add(Session session) {
        sessions.add(session);
    }

    /**
     * Remove the given {@link Session} from this {@code SessionList}.
     *
     * @param session the given {@link Session} from this {@code SessionList}.
     */
    public void remove(Session session) {
        sessions.remove(session);
    }

    /**
     * Find the sessionNumber of a session at a particular time in a given {@link Venue}.
     * Return zero if no session exists at that time.
     *
     * @param venue the venue to check
     * @param day the day to check
     * @param start the start time to check
     * @return the session number or 0 if not found
     */
    public int getSessionNumber(Venue venue, LocalDate day, LocalTime start) {
        for (Session session : sessions) {
            if (session.getVenue().venueId().equals(venue.venueId())
                    && session.getDate().equals(day) && session.getTime().equals(start)) {
                return session.getSessionNumber();
            }
        }
        return 0;
    }

    /**
     * Get the {@link Session} with a matching {@link Venue} and {@code sessionNumber}.
     *
     * @param venue the venue
     * @param sessionNumber the session number
     * @return the matching session
     * @throws IllegalStateException if no such session exists
     */
    public Session getSession(Venue venue, int sessionNumber) throws IllegalStateException {
        for (Session session : sessions) {
            if (session.getVenue().venueId().equals(venue.venueId())
                    && session.getSessionNumber() == sessionNumber) {
                return session;
            }
        }
        throw new IllegalStateException("No such session!");
    }

    /**
     * Get the {@link Session} with a matching {@link Venue} and {@link Exam} scheduled.
     *
     * @param venue the venue
     * @param exam the exam
     * @return the matching session
     * @throws IllegalStateException if no such session exists
     */
    public Session getSession(Venue venue, Exam exam) throws IllegalStateException {
        for (Session session : sessions) {
            List<Exam> examList = session.getExams();
            for (Exam check : examList) {
                if (check.getSubject().getTitle().equals(exam.getSubject().getTitle())) {
                    return session;
                }
            }
        }
        throw new IllegalStateException("No such session!");
    }

    /**
     * Gets the existing number of students already allocated to a session.
     * This method is needed by SessionHandler.
     *
     * @param venue the venue
     * @param exam the exam
     * @return the number of existing students
     */
    public int getExistingSessionTotal(Venue venue, Exam exam) {
        try {
            Session session = getSession(venue, exam);
            return session.countStudents();
        } catch (IllegalStateException e) {
            // No session exists yet, check by date/time
            LocalDate day = exam.getDate();
            LocalTime start = exam.getTime();

            for (Session session : sessions) {
                if (session.getVenue().venueId().equals(venue.venueId())
                        && session.getDate().equals(day)
                        && session.getTime().equals(start)) {
                    return session.countStudents();
                }
            }
            return 0;
        }
    }

    /**
     * Find or create this session and work out how many students in total.
     *
     * @param venue the venue
     * @param exam the exam
     * @param numberStudents the number of students to add
     * @return the total number of students after adding
     */
    public int getSessionNewTotal(Venue venue, Exam exam, int numberStudents) {
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();

        Session session;
        int sessionNumber = this.getSessionNumber(venue, day, start);
        if (sessionNumber == 0) {
            System.out.println("There is currently no exam session in that venue at that time.");
            System.out.println("Creating a session...");
            session = new Session(venue, getNextSessionNumber(venue), day, start, registry);
            sessions.add(session);
        }

        // Get the session (newly created or existing)
        sessionNumber = this.getSessionNumber(venue, day, start);
        session = this.getSession(venue, sessionNumber);

        int numberAlready = session.countStudents();
        int totalStudents = numberAlready + numberStudents;
        if (numberAlready > 0) {
            System.out.println("There are already " + numberAlready
                    + " students who will be taking an exam in that venue; ");
            System.out.println("along with the " + numberStudents + " students for this exam.");
        }
        System.out.println("That's a total of " + totalStudents + " students.");
        return totalStudents;
    }

    /**
     * Helper method to get the next available session number for a venue.
     *
     * @param venue the venue
     * @return the next available session number
     */
    private int getNextSessionNumber(Venue venue) {
        int nextSessionNumber = 1;
        for (Session session : sessions) {
            if (session.getVenue().venueId().equals(venue.venueId())
                    && session.getSessionNumber() >= nextSessionNumber) {
                nextSessionNumber = session.getSessionNumber() + 1;
            }
        }
        return nextSessionNumber;
    }

    /**
     * Allocates an exam to an existing session (Venue and time).
     *
     * @param venue the venue
     * @param exam the exam to schedule
     */
    public void scheduleExam(Venue venue, Exam exam) {
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();
        int sessionNumber = this.getSessionNumber(venue, day, start);

        if (sessionNumber == 0) {
            // Create new session if none exists
            Session newSession = new Session(venue, getNextSessionNumber(venue), day, start, registry);
            sessions.add(newSession);
            sessionNumber = newSession.getSessionNumber();
        }

        Session session = this.getSession(venue, sessionNumber);
        // Use a default student count since we don't have access to the full student list here
        int studentCount = session.getStudentCountForExam(exam);
        session.scheduleExam(exam, studentCount);

        System.out.println(exam.getSubject().getTitle() + " exam added to " + venue.venueId() + ".");
    }

    /**
     * Removes an exam from a session.
     *
     * @param venue the venue
     * @param exam the exam to remove
     */
    public void removeExam(Venue venue, Exam exam) {
        try {
            Session session = getSession(venue, exam);
            session.removeExam(exam);

            // If session now has no exams, remove it
            if (session.getExams().isEmpty()) {
                sessions.remove(session);
            }
        } catch (IllegalStateException e) {
            // Session doesn't exist, nothing to remove
        }
    }

    /**
     * Creates a new list holding {@code references} to those {@link Session}s
     * for a given {@link Venue} in this {@code SessionList}.
     *
     * @param venue the venue to filter by
     * @return list of sessions for the venue
     */
    public List<Session> forVenue(Venue venue) {
        List<Session> sessionList = new ArrayList<>();
        for (Session session : sessions) {
            if (session.getVenue().venueId().equals(venue.venueId())) {
                sessionList.add(session);
            }
        }
        return sessionList;
    }

    /**
     * Creates a new list holding {@code references} to all the {@link Session}s
     * in this {@code SessionList}.
     *
     * @return all sessions
     */
    public List<Session> all() {
        return new ArrayList<>(sessions);
    }

    /**
     * Find a session by its ID.
     *
     * @param key the session ID
     * @return the session or null if not found
     */
    public Session find(String key) {
        for (Session session : sessions) {
            if (session.getId().equals(key)) {
                return session;
            }
        }
        return null;
    }

    /**
     * Get a session by its ID.
     *
     * @param key the session ID
     * @return the session
     * @throws IllegalStateException if not found
     */
    public Session get(String key) throws IllegalStateException {
        Session session = find(key);
        if (session == null) {
            throw new IllegalStateException("Session not found: " + key);
        }
        return session;
    }

    /**
     * Get detailed information about all sessions.
     *
     * @return detailed string representation
     */
    public String getFullDetail() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sessions: ").append(sessions.size()).append("\n");
        for (Session session : sessions) {
            sb.append("\n").append(session.getFullDetail());
        }
        return sb.toString();
    }

    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        bw.write("[Sessions: " + sessions.size() + "]" + System.lineSeparator());
        int index = 1;
        for (Session session : sessions) {
            session.streamOut(bw, index++);
        }
    }

    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        sessions.clear();

        String line = CSSE7023.getLine(br);
        if (line == null || !line.startsWith("[Sessions:")) {
            throw new RuntimeException("Expected [Sessions: N] but got: " + line);
        }

        // Parse session count
        String[] parts = line.substring(1, line.length() - 1).split(": ");
        int sessionCount = Integer.parseInt(parts[1]);

        // Read each session
        for (int i = 1; i <= sessionCount; i++) {
            Session session = new Session(br, registry, i);
            sessions.add(session);
        }
    }

    /**
     * Simple toString for debugging.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SessionList with ").append(sessions.size()).append(" sessions:\n");
        for (Session session : sessions) {
            sb.append("  ").append(session.toString()).append("\n");
        }
        return sb.toString();
    }
}