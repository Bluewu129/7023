package examblock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A collection object for holding and managing {@link Subject} {@link Unit}s.
 * Updated to extend ListManager for streaming support.
 */
public class UnitList extends ListManager<Unit> {

    /**
     * Constructs an empty list of {@link Unit}s.
     */
    public UnitList() {
        super(Unit::new, new RegistryImpl(), Unit.class);
    }

    /**
     * Constructs a UnitList with a specific registry.
     */
    public UnitList(Registry registry) {
        super(Unit::new, registry, Unit.class);
    }

    /**
     * Adds a {@link Unit} to this list of {@link Unit}s.
     */
    public void addUnit(Unit unit) {
        add(unit);
    }

    /**
     * Removes a given {@link Unit} from the {@code UnitList}.
     */
    public void removeUnit(Unit unit) {
        remove(unit);
    }

    /**
     * Get the first {@link Unit} with a matching {@link Subject} and {@code unitId}.
     */
    public Unit getUnit(String title, Character unitId) throws IllegalStateException {
        for (Unit unit : getItems()) {
            if (unit.getSubject().getTitle().equals(title) && unit.id().equals(unitId)) {
                return unit;
            }
        }
        throw new IllegalStateException("No such unit!");
    }

    @Override
    public Unit find(String key) {
        // Key format: "SubjectTitle:UnitId"
        String[] parts = key.split(":");
        if (parts.length == 2) {
            String subjectTitle = parts[0];
            Character unitId = parts[1].charAt(0);

            Optional<Unit> unit = super.all()
                    .stream()
                    .filter(u -> u.getSubject().getTitle().equals(subjectTitle) && u.id().equals(unitId))
                    .findFirst();

            return unit.orElse(null);
        }
        return null;
    }

    @Override
    public Unit get(String key) throws IllegalStateException {
        Unit unit = find(key);
        if (unit != null) {
            return unit;
        }
        throw new IllegalStateException("Item with ID " + key + " not found for type Unit");
    }

    /**
     * Creates a new {@code List} holding {@code references} to all the {@link Unit}s
     * managed by the {@code UnitList} and returns it.
     */
    public List<Unit> getAllUnits() {
        return super.all();
    }

    /**
     * Returns detailed string representations of the contents of this unit list.
     */
    public String getFullDetail() {
        StringBuilder unitStrings = new StringBuilder();
        for (Unit unit : getItems()) {
            unitStrings.append(unit.getFullDetail());
            unitStrings.append("\n");
        }
        return unitStrings + "\n";
    }

    /**
     * Returns a string representation of the contents of the unit list
     */
    @Override
    public String toString() {
        StringBuilder unitStrings = new StringBuilder();
        for (Unit unit : getItems()) {
            unitStrings.append(unit.toString());
            unitStrings.append("\n");
        }
        return unitStrings.toString();
    }
}
