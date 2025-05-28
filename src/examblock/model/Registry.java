package examblock.model;

import java.util.List;

/**
 * Interface for a registry that manages shared instances of {@link ManageableListItem}
 * objects, such as {@code Exam}, {@code Session}, or {@code Student}. Provides methods
 * to add, find, remove, and list items by type, ensuring a single source of truth for
 * shared data across the application. Supports dependency injection by allowing classes
 * like {@code Student} to query items without owning them.
 */
public interface Registry {

    /**
     * Checks if an item with the given ID exists for the specified type.
     *
     * @param id   the unique identifier to check
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @return {@code true} if an item with the ID exists, {@code false} otherwise
     */
    <T extends ManageableListItem> boolean contains(String id, Class<T> type);

    /**
     * Adds an item to the registry for the specified type. The item is stored with its
     * unique {@link ManageableListItem#getId()} for fast lookup and added to an ordered
     * list for iteration.
     *
     * @param item the item to add
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     */
    <T extends ManageableListItem> void add(T item, Class<T> type);

    /**
     * Finds an item by its unique ID for the specified type.
     *
     * @param id   the unique identifier of the item
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @return the item if found, or {@code null} if not found
     */
    <T extends ManageableListItem> T find(String id, Class<T> type);

    /**
     * Finds an item by its unique ID for the specified type. Unlike {@code find()},
     * get will throw a RuntimeException if the item is not found
     *
     * @param id   the unique identifier of the item
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @return the item if found, or {@code throw new RuntimeException} if not found
     */
    <T extends ManageableListItem> T get(String id, Class<T> type);

    /**
     * Removes an item by its unique ID for the specified type.
     *
     * @param id   the unique identifier of the item
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     */
    <T extends ManageableListItem> void remove(String id, Class<T> type);

    /**
     * Removes all items of the specified type from the registry, clearing both the
     * ID-based map and the ordered ID list for that type.
     *
     * @param type the class of the items to remove
     * @param <T>  the type of the items, extending {@link ManageableListItem}
     */
    <T extends ManageableListItem> void removeAll(Class<T> type);

    /**
     * Returns a copy of the ordered list of all items for the specified type. The returned
     * list is read-only in structure (adding/removing items does not affect the registry),
     * but item fields can be modified, affecting all references.
     *
     * @param type the class of the items
     * @param <T>  the type of the items, extending {@link ManageableListItem}
     * @return a new list containing all items of the specified type
     */
    <T extends ManageableListItem> List<T> getAll(Class<T> type);

    /**
     * Retrieves an item at the specified index in the ordered list for the given type.
     *
     * @param row  the index of the item (0-based)
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @return the item at the index, or {@code null} if the index is invalid
     */
    <T extends ManageableListItem> T getAt(int row, Class<T> type);

    /**
     * Clears all items from the registry, removing all types and their associated data.
     */
    void clear();

    /**
     * Returns the number of items stored for the specified type.
     *
     * @param type the class of the items
     * @param <T>  the type of the items, extending {@link ManageableListItem}
     * @return the number of items
     */
    <T extends ManageableListItem> int count(Class<T> type);
}
