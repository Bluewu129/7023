package examblock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Venue}s.
 */
public class VenueList {

    /** This instance's list of venues. */
    private final List<Venue> venues;

    /**
     * Constructs an empty list of {@link Venue}s.
     */
    public VenueList() {
        venues = new ArrayList<>();
    }

    /**
     * Adds a {@link Venue} to this list of {@link Venue}s.
     *
     * @param venue - the venue object being added to this list.
     */
    public void addVenue(Venue venue) {
        venues.add(venue);
    }

    /**
     * Removes a given {@link Venue} from the {@code VenueList}.
     *
     * @param venue the venue to remove from this list.
     */
    public void removeVenue(Venue venue) {
        venues.remove(venue);
    }

    /**
     * Get the first {@link Venue} with a matching {@code id}.
     *
     * @param id the identifier of the {@link Venue} to be found.
     * @return first {@link Venue} with a matching {@code id}, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *         find a matching venue as that indicates there is a misalignment of
     *         the executing state and the complete list of possible venues.
     */
    public Venue getVenue(String id) throws IllegalStateException {
        for (Venue venue : this.venues) {
            if (venue.venueId().equals(id)) {
                return venue;
            }
        }
        throw new IllegalStateException("No such venue!");
    }

    /**
     * Creates a new {@code List} holding {@code references} to all the {@link Venue}s
     * managed by the {@code VenueList} and returns it.
     *
     * @return a new {@code List} holding {@code references} to all the {@link Venue}s
     * managed by the {@code VenueList}.
     */
    public List<Venue> all() {
        return new ArrayList<>(this.venues);
    }

    /**
     * Allocates {@link Student}s to {@link Desk}s for every {@link Session} in every {@link Venue}.
     *
     * @param sessions the current set of exam sessions allocated to venues.
     * @param exams the current set of Year 12 Exams.
     * @param cohort all the Year 12 students.
     */
    public void allocateStudents(SessionList sessions, ExamList exams, StudentList cohort) {
        List<Session> sessionList;
        for (Venue venue : this.venues) {
            // get the list of sessions for this venue
            sessionList = sessions.forVenue(venue);
            for (Session session : sessionList) {
                session.allocateStudents(exams, cohort);
            }
        }
    }

    /**
     * Print the allocations of {@link Student}s to {@link Desk}s for every {@link Session}
     * in every {@link Venue}.
     *
     * @param sessions the current set of exam sessions allocated to venues.
     */
    public void printAllocations(SessionList sessions) {
        List<Session> sessionList;
        for (Venue venue : this.venues) {
            System.out.println(venue);
            // get the list of sessions for this venue
            sessionList = sessions.forVenue(venue);
            for (Session session : sessionList) {
                System.out.println(session);
                session.printDesks();
            }
            System.out.println("-".repeat(75));
        }
    }

    /**
     * Returns detailed string representations of the contents of this venue list.
     *
     * @return detailed string representations of the contents of this venue list.
     */
    public String getFullDetail() {

        StringBuilder venueStrings = new StringBuilder();
        int counter = 1;
        for (Venue venue : this.venues) {
            venueStrings.append(counter);
            venueStrings.append(". ");
            venueStrings.append(venue.venueId());
            venueStrings.append("\n");
            counter += 1;
        }
        return venueStrings + "\n";
    }

    /**
     * Returns a brief string representation of the contents of this venue list.
     *
     * @return a brief string representation of the contents of this venue list.
     */
    @Override
    public String toString() {

        StringBuilder venueStrings = new StringBuilder();
        int counter = 1;
        for (Venue venue : this.venues) {
            venueStrings.append(counter);
            venueStrings.append(". ");
            venueStrings.append(venue.toString());
            venueStrings.append("\n");
            counter += 1;
        }
        return venueStrings.toString();
    }
}
