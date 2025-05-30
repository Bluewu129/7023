package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing Sessions.
 */
public class SessionList implements StreamManager {

    /**
     * This instance's list of sessions.
     */
    private final List<Session> sessions;
    /**
     * The registry for dependencies.
     */
    private final Registry registry;

    /**
     * Constructs a new SessionList.
     * Constructor requires Registry parameter as per specification.
     */
    public SessionList(Registry registry) {
        this.sessions = new ArrayList<>();
        this.registry = registry;
    }

    /**
     * Add the given Session to this SessionList for it to manage.
     * Method signature matches ListManager.add specification.
     */
    public void add(ManageableListItem session) {
        if (session instanceof Session) {
            sessions.add((Session) session);
        }
    }

    /**
     * Remove the given Session from this SessionList.
     * Method signature matches ListManager.remove specification.
     */
    public void remove(ManageableListItem session) {
        if (session instanceof Session) {
            sessions.remove((Session) session);
        }
    }

    /**
     * Creates a new List holding references to all the Sessions
     * in this SessionList.
     * Return type matches ListManager.all specification.
     */
    public ArrayList<Session> all() {
        return new ArrayList<>(sessions);
    }

    /**
     * Returns the number of items in the list.
     * Required method as per specification.
     */
    public int size() {
        return sessions.size();
    }

    /**
     * Removes all items from the list.
     * Required method as per specification.
     */
    public void clear() {
        sessions.clear();
    }

    /**
     * Replaces the contents of this list with the provided items.
     * Required method as per specification.
     */
    public void addAll(ArrayList<Session> source) {
        sessions.clear();
        if (source != null) {
            sessions.addAll(source);
        }
    }

    /**
     * Returns a copy of the ordered list of all items.
     * Required method as per specification.
     */
    public List<Session> getItems() {
        return new ArrayList<>(sessions);
    }

    /**
     * Find a session by its ID.
     * Method signature matches specification: ManageableListItem return type, no exceptions.
     */
    public Session find(String key) throws IllegalStateException {
        for (Session session : sessions) {
            if (session.getId().equals(key)) {
                return session;
            }
        }
        return null;
    }

    /**
     * Get a session by its ID.
     * Method signature matches specification: ManageableListItem return type, no exceptions.
     */
    public Session get(String key) {
        for (Session session : sessions) {
            if (session.getId().equals(key)) {
                return session;
            }
        }
        return null;
    }

    /**
     * Find the sessionNumber of a session at a particular time in a given Venue.
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
     * Get the Session with a matching Venue and sessionNumber.
     * Fixed to throw IllegalStateException as required.
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
     * Get the Session with a matching Venue and Exam scheduled.
     * Fixed to throw IllegalStateException as required.
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
     */
    public void scheduleExam(Venue venue, Exam exam) {
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();
        int sessionNumber = this.getSessionNumber(venue, day, start);

        if (sessionNumber == 0) {
            Session newSession = new Session(venue, getNextSessionNumber(venue), day, start, registry);
            sessions.add(newSession);
            sessionNumber = newSession.getSessionNumber();
        }

        Session session = this.getSession(venue, sessionNumber);
        session.scheduleExam(exam);

        System.out.println(exam.getSubject().getTitle() + " exam added to " + venue.venueId() + ".");
    }

    /**
     * Removes an exam from a session.
     */
    public void removeExam(Venue venue, Exam exam) {
        try {
            Session session = getSession(venue, exam);
            session.removeExam(exam);

            if (session.getExams().isEmpty()) {
                sessions.remove(session);
            }
        } catch (IllegalStateException e) {
            // Session doesn't exist, nothing to remove
        }
    }

    /**
     * Creates a new list holding references to those Sessions
     * for a given Venue in this SessionList.
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
     * Get detailed information about all sessions.
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

        String[] parts = line.substring(1, line.length() - 1).split(": ");
        int sessionCount = Integer.parseInt(parts[1]);

        for (int i = 1; i <= sessionCount; i++) {
            Session session = new Session(br, registry, i);
            sessions.add(session);
        }
    }
}