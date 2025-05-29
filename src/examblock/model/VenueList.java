package examblock.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A collection object for holding and managing {@link Venue}s.
 * Updated to extend ListManager for streaming support.
 */
public class VenueList extends ListManager<Venue> {

    /**
     * Constructs an empty list of {@link Venue}s.
     */
    public VenueList() {
        super(new VenueFactory(), new RegistryImpl(), Venue.class);
    }

    /**
     * Constructs a VenueList with a specific registry.
     *
     * @param registry the registry to use
     */
    public VenueList(Registry registry) {
        super(new VenueFactory(), registry, Venue.class);
    }

    /**
     * Factory for creating Venue instances.
     */
    private static class VenueFactory implements ItemFactory<Venue> {
        @Override
        public Venue createItem(BufferedReader br, Registry registry, int index) throws IOException {
            return new Venue(br, registry, index);
        }
    }

    /**
     * Adds a {@link Venue} to this list of {@link Venue}s.
     *
     * @param venue - the venue object being added to this list.
     */
    public void addVenue(Venue venue) {
        add(venue);
    }

    /**
     * Removes a given {@link Venue} from the {@code VenueList}.
     *
     * @param venue the venue to remove from this list.
     */
    public void removeVenue(Venue venue) {
        remove(venue);
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
        Venue venue = find(id);
        if (venue != null) {
            return venue;
        }
        throw new IllegalStateException("No such venue!");
    }

    @Override
    public Venue find(String key) {
        Optional<Venue> venue = super.all()
                .stream()
                .filter(v -> v.getId().equals(key))
                .findFirst();

        return venue.orElse(null);
    }

    @Override
    public Venue get(String key) throws IllegalStateException {
        Venue venue = find(key);
        if (venue != null) {
            return venue;
        }
        throw new IllegalStateException("Item with ID " + key + " not found for type Venue");
    }

    /**
     * Creates a new {@code List} holding {@code references} to all the {@link Venue}s
     * managed by the {@code VenueList} and returns it.
     *
     * @return a new {@code List} holding {@code references} to all the {@link Venue}s
     * managed by the {@code VenueList}.
     */
    public ArrayList<Venue> all() {
        return super.all();
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
        for (Venue venue : getItems()) {
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
        for (Venue venue : getItems()) {
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
     * Write the allocations of {@link Student}s to {@link Desk}s for every {@link Session}
     * in every {@link Venue} to a StringBuilder.
     *
     * @param sb the StringBuilder to write to
     * @param sessions the current set of exam sessions allocated to venues.
     */
    public void writeAllocations(StringBuilder sb, SessionList sessions) {
        List<Session> sessionList;
        for (Venue venue : getItems()) {
            sb.append(venue).append(System.lineSeparator());
            // get the list of sessions for this venue
            sessionList = sessions.forVenue(venue);
            for (Session session : sessionList) {
                sb.append(session).append(System.lineSeparator());
                // Write desk information instead of printing
                writeDesks(sb, session);
            }
            sb.append("-".repeat(75)).append(System.lineSeparator());
        }
    }

    /**
     * Helper method to write desk information to StringBuilder.
     */
    private void writeDesks(StringBuilder sb, Session session) {
        // This would need access to session's desk information
        // For now, we'll add a placeholder
        sb.append("Desk allocations for session ").append(session.getSessionNumber())
                .append(System.lineSeparator());
    }

    /**
     * Returns detailed string representations of the contents of this venue list.
     *
     * @return detailed string representations of the contents of this venue list.
     */
    public String getFullDetail() {
        StringBuilder venueStrings = new StringBuilder();
        int counter = 1;
        for (Venue venue : getItems()) {
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
        for (Venue venue : getItems()) {
            venueStrings.append(counter);
            venueStrings.append(". ");
            venueStrings.append(venue.toString());
            venueStrings.append("\n");
            counter += 1;
        }
        return venueStrings.toString();
    }
}