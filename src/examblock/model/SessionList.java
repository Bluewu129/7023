package examblock.model;

import java.time.LocalDate;
import java.time.LocalTime;
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
     * @param day   the session date.
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
     * @param venue         the Venue for which the session is to be found.
     * @param sessionNumber the sessionNumber of the Session you are looking for.
     * @return The first Session with a matching Venue and sessionNumber, if it exists.
     * @throws IllegalStateException throw an IllegalStateException if it can't find any such
     *                               session as that indicates there is a potential misalignment of the executing state and
     *                               the complete list of all sessions.
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
}