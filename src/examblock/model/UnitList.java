package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection object for holding and managing {@link Subject} {@link Unit}s.
 */
public class UnitList extends ListManager<Unit> implements StreamManager {

    /**
     * Constructs a UnitList with registry.
     *
     * @param registry - registry
     */
    public UnitList(Registry registry) {
        super(Unit::new, registry, Unit.class);
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key - the text used to identify the item
     * @return the item if found or null
     */
    @Override
    public Unit find(String key) {
        for (Unit unit : super.all()) {
            if (unit.getId().equals(key)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key - the text used to identify the item
     * @return the item if found
     * @throws IllegalStateException - if no item is found
     */
    @Override
    public Unit get(String key) throws IllegalStateException {
        Unit unit = find(key);
        if (unit == null) {
            throw new IllegalStateException("No unit found with key: " + key);
        }
        return unit;
    }

    /**
     * Get the first Unit with a matching Subject and unitId.
     *
     * @param subjectTitle the title of the parent Subject of the Unit to be found.
     * @param unitId the unit identifier of the Subject Unit to be found.
     * @return first Unit with a matching subject title and unitId,
     *               if it exists.
     * @throws IllegalStateException - throw an IllegalStateException if it can't
     *         find a matching unit as that indicates there is a misalignment of
     *         the executing state and the complete list of possible units.
     */
    public Unit getUnit(String subjectTitle, Character unitId) throws IllegalStateException {
        for (Unit unit : super.all()) {
            if (unit.getSubject().getTitle().equals(subjectTitle) && unit.id().equals(unitId)) {
                return unit;
            }
        }
        throw new IllegalStateException("No such unit!");
    }

    /**
     * Returns detailed string representations of the contents of this unit list.
     *
     * @return detailed string representations of the contents of this unit list.
     */
    public String getFullDetail() {
        StringBuilder unitStrings = new StringBuilder();
        for (Unit unit : super.all()) {
            unitStrings.append(unit.getFullDetail());
            unitStrings.append("\n");
        }
        return unitStrings + "\n";
    }

    /**
     * Returns a string representation of the contents of the unit list
     *
     * @return a string representation of the contents of the unit list
     */
    @Override
    public String toString() {
        StringBuilder unitStrings = new StringBuilder();
        for (Unit unit : super.all()) {
            unitStrings.append(unit.toString());
            unitStrings.append("\n");
        }
        return unitStrings.toString();
    }
}