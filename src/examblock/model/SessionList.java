package examblock.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * A collection object for holding and managing Sessions.
 */
public class SessionList extends ListManager<Session> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public SessionList(Registry registry) {
        super(Session::new, registry, Session.class);
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key the text used to identify the item
     * @return the item, or
     * @throws IllegalStateException if not found
     */
    @Override
    public Session find(String key)
            throws IllegalStateException {
        Optional<Session> s = all()
                .stream()
                .filter(session -> session.getId().equals(key))
                .findFirst();

        if (s.isPresent()) {
            return s.get();
        }
        throw new IllegalStateException("Item with ID "
                + key + " not found for type Session");
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key the text used to identify the item
     * @return the item if found
     * @throws IllegalStateException if no item is found
     */
    @Override
    public Session get(String key)
            throws IllegalStateException {
        Session s = find(key);
        if (s != null) {
            return s;
        }
        throw new IllegalStateException("Item with ID "
                + key + " not found for type Session");
    }

    /**
     * Find the sessionNumber of a session at a particular time in a given Venue.
     * Return zero if no session exists at that time.
     *
     * @param venue - the venue object for the session we are looking for.
     * @param day the session date.
     * @param start the start time of the exam session.
     * @return the sessionNumber of a session at a particular time in a given Venue, else zero.
     */
    public int getSessionNumber(Venue venue, LocalDate day, LocalTime start) {
        for (Session session : this.getItems()) {
            if (session.getVenue().venueId().equals(venue.venueId())
                    && session.getDate().equals(day) && session.getTime().equals(start)) {
                return session.getSessionNumber();
            }
        }
        return 0;
    }

    /**
     * Get the Session with a matching Venue and sessionNumber.
     *
     * @param venue the Venue for which the session is to be found.
     * @param sessionNumber the sessionNumber of the Session you are looking for.
     * @return The first Session with a matching Venue and sessionNumber, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't find any such
     * session as that indicates there is a potential misalignment of the executing state and
     * the complete list of all sessions.
     */
    public Session getSession(Venue venue, int sessionNumber) throws IllegalStateException {
        for (Session session : this.getItems()) {
            if (session.getVenue().venueId().equals(venue.venueId())
                    && session.getSessionNumber() == sessionNumber) {
                return session;
            }
        }
        throw new IllegalStateException("No such session!");
    }

    /**
     * Get the Session with a matching Venue and Exam scheduled.
     *
     * @param venue the Venue for which the session is to be found.
     * @param exam (one of) the exam(s) that has been allocated to this session in this venue.
     * @return The first Session with a matching Venue and Exam, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't find any such
     * session as that indicates there is a potential misalignment of the executing state and
     * the complete list of all sessions.
     */
    public Session getSession(Venue venue, Exam exam) throws IllegalStateException {
        System.out.println(exam.getTitle());
        for (Session session : this.getItems()) {
            System.out.println(exam.getTitle());
            List<Exam> examList = session.getExams();
            System.out.println(exam.getTitle());
            System.out.println(examList);
            for (Exam check : examList) {
                System.out.println(check.getTitle());
                System.out.println(exam.getTitle());
                if (check.getTitle().equals(exam.getTitle())) {
                    return session;
                }
            }
        }
        throw new IllegalStateException("No such session!");
    }

    /**
     * Find or create this session and work out how many students in total.
     * Looks for an existing session or creates a new session (Venue and time) if not present
     * in the session list; and then determines (from each of the exams in the session)
     * what the total number of students will be in the session.
     *
     * @param venue the exam venue for the session.
     * @param exam the exam to be allocated to this session in this venue.
     * @param numberStudents the number of students to be allocated to this session.
     * @return The total number of students that will be in the session if numberStudents is added.
     */
    public int getSessionNewTotal(Venue venue, Exam exam, int numberStudents) {
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();
        boolean aara = venue.isAara();

        Session session;
        int sessionNumber = this.getSessionNumber(venue, day, start);
        if (sessionNumber == 0) {
            System.out.println("There is currently no exam session in that venue at that time.");
            System.out.println("Creating a session...");
            int nextSessionNumber = 1;
            for (Session s : this.getItems()) {
                if (s.getVenue().venueId().equals(venue.venueId())
                        && s.getSessionNumber() >= nextSessionNumber) {
                    nextSessionNumber = s.getSessionNumber() + 1;
                }
            }
            session = new Session(venue, nextSessionNumber, day, start, getRegistry());
            this.add(session);
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
        System.out.println("That\'s a total of " + totalStudents + " students.");
        return totalStudents;
    }

    /**
     * Find this session and work out how many students in total.
     * Unlike getSessionNewTotal(Venue, Exam, int), this method does not create a new session
     * if one does not already exist, it just looks for an existing session. If there is no
     * existing session, returns zero.
     *
     * @param venue - where the session might be
     * @param exam - the exam that might be scheduled in the session
     * @return the total number of students in the session, or zero if there is no session.
     */
    public int getExistingSessionTotal(Venue venue, Exam exam) {
        int total = 0;
        for (Session session : getItems()) {
            if (session.getVenue().venueId().equals(venue.venueId()) 
                    && session.getExams().contains(exam)) {
                total += session.countStudents();
            }
        }
        return total;
    }

    /**
     * Allocates an exam to an existing session (Venue and time).
     * Prints "(the title of the subject) exam added to the Identifier of the venue."
     *
     * @param venue the exam venue for the new session.
     * @param exam the exam to be allocated to this venue.
     */
    public void scheduleExam(Venue venue, Exam exam) {
        Subject subject = exam.getSubject();
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();
        int sessionNumber = this.getSessionNumber(venue, day, start);
        Session session = this.getSession(venue, sessionNumber);
        session.scheduleExam(exam);
        System.out.println(subject.getTitle() + " exam added to " + venue.venueId() + ".");
    }

    /**
     * Deallocates an exam from an existing session (Venue and time).
     * Prints "(the title of the subject) exam removed from (the Identifier of the venue)."
     *
     * @param venue - the exam venue for the new session.
     * @param exam - the exam to be allocated to this venue.
     */
    public void removeExam(Venue venue, Exam exam) {
        Subject subject = exam.getSubject();
        LocalDate day = exam.getDate();
        LocalTime start = exam.getTime();
        int sessionNumber = this.getSessionNumber(venue, day, start);
        try {
            Session session = this.getSession(venue, sessionNumber);
            session.removeExam(exam);
            System.out.println(subject.getTitle() + " exam removed from " + venue.venueId() + ".");
        } catch (IllegalStateException e) {
            System.out.println("No session found to remove exam from.");
        }
    }

    /**
     * Creates a new list holding references to those Sessions
     * for a given Venue in this SessionList.
     *
     * @param venue the exam venue for the list of sessions.
     * @return A new list holding references to all the sessions in this sessionList.
     */
    public List<Session> forVenue(Venue venue) {
        return getItems().stream()
                .filter(session -> session.getVenue().venueId().equals(venue.venueId()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Return a string containing enough information to rebuild an item
     *
     * @return the string representation of the object
     */
    public String getFullDetail() {
        StringBuilder sessionStrings = new StringBuilder();
        int counter = 1;
        for (Session session : this.getItems()) {
            sessionStrings.append(counter);
            sessionStrings.append(". ");
            sessionStrings.append(session.getFullDetail());
            sessionStrings.append("\n");
            counter += 1;
        }
        return sessionStrings + "\n";
    }
}