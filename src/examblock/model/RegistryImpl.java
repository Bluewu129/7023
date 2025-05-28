package examblock.model;

import java.util.*;

/**
 * Implementation of the {@link Registry} interface, managing shared instances of
 * {@link ManageableListItem} objects (e.g., {@code Exam}, {@code Subject}). Stores
 * items in a map for fast lookup by ID and a list of IDs for ordering. Ensures
 * uniqueness by throwing an exception for duplicate IDs.
 */
public class RegistryImpl implements Registry {
    /**
     * Map storing items by type and ID for fast lookup. Keyed by the item's class
     * (e.g., {@code Exam.class}), with a nested map of ID to item.
     */
    private final Map<Class<? extends ManageableListItem>,
            Map<String, ? extends ManageableListItem>> registries;

    /**
     * Map storing ordered lists of item IDs by type, maintaining insertion order.
     * Keyed by the item's class (e.g., {@code Exam.class}).
     */
    private final Map<Class<? extends ManageableListItem>,
            List<String>> orderedIds;

    /**
     * Constructs a new, empty registry.
     */
    public RegistryImpl() {
        registries = new HashMap<>();
        orderedIds = new HashMap<>();
    }

    /**
     * Retrieves or creates a map for the specified type, used for ID-based lookups.
     *
     * @param type the class of the items
     * @param <T>  the type of the items, extending {@link ManageableListItem}
     * @return the map of ID to item for the type
     */
    private <T extends ManageableListItem> Map<String, T> getOrCreateRegistry(Class<T> type) {
        return (Map<String, T>) registries.computeIfAbsent(type, k -> new HashMap<>());
    }

    /**
     * Retrieves or creates an ordered list of IDs for the specified type.
     *
     * @param type the class of the items
     * @param <T>  the type of the items, extending {@link ManageableListItem}
     * @return the ordered list of item IDs for the type
     */
    private <T extends ManageableListItem> List<String> getOrCreateOrderedIds(Class<T> type) {
        return (List<String>) orderedIds.computeIfAbsent(type, k -> new ArrayList<>());
    }

    /**
     * Checks if an item with the given ID exists for the specified type.
     *
     * @param id   the unique identifier to check
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @return {@code true} if an item with the ID exists, {@code false} otherwise
     */
    public <T extends ManageableListItem> boolean contains(String id, Class<T> type) {
        return getOrCreateRegistry(type).containsKey(id);
    }

    /**
     * Adds an item to the registry for the specified type, storing it by its
     * {@link ManageableListItem#getId()} and appending its ID to the ordered list.
     * If the item is already registered (same ID and instance), it is not re-added.
     *
     * @param item the item to add
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @throws IllegalStateException if a different item with the same ID exists
     * @throws NullPointerException  if the item's ID is null
     */
    @Override
    public <T extends ManageableListItem> void add(T item, Class<T> type) {
        Map<String, T> registry = getOrCreateRegistry(type);
        List<String> ids = getOrCreateOrderedIds(type);
        String id = Objects.requireNonNull(item.getId(), "Item ID cannot be null");

        if (registry.containsKey(id)) {
            T existing = registry.get(id);
            if (existing == item) {
                return; // Already registered
            }
            throw new IllegalStateException("Item with ID " + id + " already exists for type "
                    + type.getSimpleName());
        }
        registry.put(id, item);
        if (!ids.contains(id)) {
            ids.add(id);
        }
    }

    /**
     * Finds an item by its unique ID for the specified type. Unlike {@code find()},
     * get will throw a RuntimeException if the item is not found
     *
     * @param id   the unique identifier of the item
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @return the item if found, or {@code throw new RuntimeException} if not found
     */
    @Override
    public <T extends ManageableListItem> T get(String id, Class<T> type) throws RuntimeException {
        if (find(id, type) == null) {
            throw new RuntimeException("Item with ID " + id + " not found for type "
                    + type.getSimpleName());
        }

        return find(id, type);
    }

    /**
     * Finds an item by its unique ID for the specified type.
     *
     * @param id   the unique identifier of the item
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @return the item if found, or {@code null} if not found
     */
    @Override
    public <T extends ManageableListItem> T find(String id, Class<T> type) {
        return getOrCreateRegistry(type).get(id);
    }

    /**
     * Removes an item by its unique ID for the specified type, updating both the
     * ID-based map and the ordered ID list.
     *
     * @param id   the unique identifier of the item
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     */
    @Override
    public <T extends ManageableListItem> void remove(String id, Class<T> type) {
        Map<String, T> registry = getOrCreateRegistry(type);
        if (registry.remove(id) != null) {
            getOrCreateOrderedIds(type).remove(id);
        }
    }

    /**
     * Removes all items of the specified type from the registry, clearing both the
     * ID-based map and the ordered ID list for that type.
     *
     * @param type the class of the items to remove
     * @param <T>  the type of the items, extending {@link ManageableListItem}
     */
    @Override
    public <T extends ManageableListItem> void removeAll(Class<T> type) {
        registries.remove(type);
        orderedIds.remove(type);
    }

    /**
     * Returns a synthesized list of all items for the specified type, in the order
     * defined by the stored IDs. The returned list is a new instance, so modifying
     * it does not affect the registry.
     *
     * @param type the class of the items
     * @param <T>  the type of the items, extending {@link ManageableListItem}
     * @return a new list containing all items of the specified type
     */
    @Override
    public <T extends ManageableListItem> List<T> getAll(Class<T> type) {
        List<String> ids = getOrCreateOrderedIds(type);
        Map<String, T> registry = getOrCreateRegistry(type);
        List<T> items = new ArrayList<>(ids.size());
        for (String id : ids) {
            T item = registry.get(id);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Retrieves an item at the specified index in the ordered list for the given type.
     *
     * @param row  the index of the item (0-based)
     * @param type the class of the item
     * @param <T>  the type of the item, extending {@link ManageableListItem}
     * @return the item at the index, or {@code null} if the index is invalid
     */
    @Override
    public <T extends ManageableListItem> T getAt(int row, Class<T> type) {
        List<String> ids = getOrCreateOrderedIds(type);
        if (row >= 0 && row < ids.size()) {
            String id = ids.get(row);
            return getOrCreateRegistry(type).get(id);
        }
        return null;
    }

    /**
     * Clears all items from the registry, removing all types and their associated data.
     */
    @Override
    public void clear() {
        registries.clear();
        orderedIds.clear();
    }

    /**
     * Returns the number of items stored for the specified type.
     *
     * @param type the class of the items
     * @param <T>  the type of the items, extending {@link ManageableListItem}
     * @return the number of items
     */
    @Override
    public <T extends ManageableListItem> int count(Class<T> type) {
        return getOrCreateRegistry(type).size();
    }
}