package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A collection object for holding and managing Venues.
 */
public class VenueList extends ListManager<Venue> {

    /**
     * constructor
     *
     * @param registry registry
     */
    public VenueList(Registry registry) {
        super(Venue:: new, registry, Venue.class);
    }

    /**
     * Find an item using some identifying text
     *
     * @param key - text used to uniquely identify an item
     * @return the item, or
     * @throws IllegalStateException - if not found
     */
    @Override
    public Venue get(String key) throws IllegalStateException {
        Venue v = find(key);
        if (v != null) {
            return v;
        }
        throw new IllegalStateException("Item with ID " + key + " not found for type Venue");
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key the text used to identify the item
     * @return the item if found or null
     */
    @Override
    public Venue find(String key) throws IllegalStateException {
        Optional<Venue> v = all()
                .stream()
                .filter(venue -> venue.getId().equals(key))
                .findFirst();
        if (v.isPresent()) {
            return v.get();
        }
        throw new IllegalStateException("Item with ID "
                + key + " not found for type Venue");

    }

    /**
     * Adds a Venue to this list of Venues.
     *
     * @param venue - the venue object being added to this list.
     */
    public void addVenue(Venue venue) {
        super.add(venue);
    }

    /**
     * Removes a given Venue from the VenueList.
     *
     * @param venue - the venue to remove from this list.
     */
    public void removeVenue(Venue venue) {
        super.remove(venue);
    }

    /**
     * Get the first Venue with a matching id.
     *
     * @param id - the identifier of the Venue to be found.
     * @return first Venue with a matching id, if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *                               find a matching venue as that indicates there is a misalignment
     *                               of the executing state and the complete list of possible items.
     */
    public Venue getVenue(String id) throws IllegalStateException {
        for (Venue venue : this.getItems()) {
            if (venue.getId().equals(id)) {
                return venue;
            }
        }
        throw new IllegalStateException("No such venue!");
    }

    /**
     * Allocates Students to Desks for every Session in every Venue.
     *
     * @param sessions - the current set of exam sessions allocated to items.
     * @param exams - the current set of Year 12 Exams.
     * @param cohort - all the Year 12 students.
     */
    public void allocateStudents(SessionList sessions, ExamList exams,
                                 StudentList cohort) {
        if (Verbose.isVerbose()) {
            System.out.println("Allocating students to venues...");
        }

        List<Session> sessionList;
        for (Venue venue : getItems()) {
            sessionList = sessions.forVenue(venue);
            for (Session session : sessionList) {
                session.allocateStudents(exams, cohort);
            }
        }

        if (Verbose.isVerbose()) {
            System.out.println("Venue allocation completed.");
        }
    }

    /**
     * Print the allocations of Students to Desks for every Session in every Venue.
     *
     * @param sessions - the current set of exam sessions allocated to items.
     */
    public void printAllocations(SessionList sessions) {
        StringBuilder sb = new StringBuilder();
        writeAllocations(sb, sessions);
        System.out.print(sb.toString());
    }

    /**
     * Write the allocations of Students to Desks for every Session in every Venue to a String.
     * This version prints to the console.
     *
     * @param sb - the StringBuilder to write to
     * @param sessions - the current set of exam sessions allocated to items.
     */
    public void writeAllocations(StringBuilder sb, SessionList sessions) {
        sb.append("Venue Allocations:\n");
        sb.append("=".repeat(60)).append("\n");

        if (sessions != null && sessions.size() > 0) {
            for (Session session : sessions.all()) {
                Venue venue = session.getVenue();
                if (venue != null) {
                    sb.append("Venue: ").append(venue.venueId());
                    sb.append(", Session Number: ").append(session.getSessionNumber());
                    sb.append(", Day: ").append(session.getDate());
                    sb.append(", Start: ").append(session.getTime()).append("\n");
                    sb.append("\n");

                    session.printDesks(sb);

                    sb.append("\n");
                    sb.append("-".repeat(60)).append("\n");
                }
            }
        } else {
            sb.append("No sessions allocated.\n");
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
        for (Venue venue : this.getItems()) {
            venueStrings.append(counter);
            venueStrings.append(". ");
            venueStrings.append(venue.getFullDetail());
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
        for (Venue venue : this.getItems()) {
            venueStrings.append(counter);
            venueStrings.append(". ");
            venueStrings.append(venue.toString());
            venueStrings.append("\n");
            counter += 1;
        }
        return venueStrings.toString();
    }
}