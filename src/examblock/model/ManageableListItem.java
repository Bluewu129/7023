package examblock.model;

/**
 * Interface for items managed by {@link ListManager}. All items must implement this interface
 * to be stored in a {@link ListManager}, ensuring they provide a unique identifier and
 * methods for persistence and display.
 */
public interface ManageableListItem {

    /**
     * Returns a string containing enough information to reconstruct the item during
     * deserialization (e.g., streaming in from disk).
     *
     * @return a string representation of the object's state
     */
    String getFullDetail();

    /**
     * Returns an array of values suitable for display in a view model, such as a
     * {@code JTable}. This method provides basic column data. This interface method should
     * provide a default implementation that returns an empty array.
     *
     * @return an array of objects for insertion into a table
     */
    default Object[] toTableRow() {
        return new Object[]{};
    }

    /**
     * Returns an extended array of values for display in a view model, such as a
     * {@code JTable}. This method is optional and provides additional column data. This
     * interface method should provide a default implementation that returns an empty array.
     *
     * @return an array of objects for insertion into a table
     */
    default Object[] toLongTableRow() {
        return new Object[]{};
    }

    /**
     * Returns a string that uniquely identifies this object. Used by {@link Registry}
     * for lookup and ensuring uniqueness. This interface method should provide a default
     * implementation that returns an empty string.
     *
     * @return the unique identifying string, or an empty string if not applicable
     */
    default String getId() {
        return "";
    }
}