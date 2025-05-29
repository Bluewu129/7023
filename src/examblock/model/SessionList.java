package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Exam} {@link Session}s.
 * Keep it simple - just manage sessions, don't over-complicate it.
 */
public class SessionList {

    /** This instance's list of sessions. */
    private final List<Session> sessions;

    /**
     * Constructs a new empty SessionList.
     */
    public SessionList() {
        sessions = new ArrayList<>();
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
     */
    public int getExistingSessionTotal(Venue venue, Exam exam) {
        try {
            Session session = getSession(venue, exam);
            return session.countStudents();
        } catch (IllegalStateException e) {
            return 0; // No session exists yet
        }
    }

    /**
     * Find or create this session and work out how many students in total.
     */
    public int getSessionNewTotal(Venue venue, Exam exam, int numberStudents) {
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();

        Session session;
        int sessionNumber = this.getSessionNumber(venue, day, start);
        if (sessionNumber == 0) {
            System.out.println("There is currently no exam session in that venue at that time.");
            System.out.println("Creating a session...");
            session = new Session(venue, getNextSessionNumber(venue), day, start);
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
     * This method signature is expected by SessionHandler.
     */
    public void scheduleExam(Venue venue, Exam exam) {
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();
        int sessionNumber = this.getSessionNumber(venue, day, start);

        if (sessionNumber == 0) {
            // Create new session if none exists
            Session newSession = new Session(venue, getNextSessionNumber(venue), day, start);
            sessions.add(newSession);
            sessionNumber = newSession.getSessionNumber();
        }

        Session session = this.getSession(venue, sessionNumber);
        // Use a default student count since we don't have access to the full student list here
        int studentCount = 25; // This would normally be calculated from the actual enrollment
        session.scheduleExam(exam, studentCount);

        System.out.println(exam.getSubject().getTitle() + " exam added to " + venue.venueId() + ".");
    }

    /**
     * Creates a new list holding {@code references} to those {@link Session}s
     * for a given {@link Venue} in this {@code SessionList}.
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
     */
    public List<Session> all() {
        return new ArrayList<>(sessions);
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