package examblock.model;

import examblock.view.components.Verbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract class to manage lists of items implementing {@link ManageableListItem}.
 * Maintains its own list of items, using the {@link Registry} for ID-based lookup
 * and validation. Items are assumed to register themselves with the registry upon
 * creation, so this class manages list membership independently.
 *
 * @param <T> the type of items under management, extending {@link ManageableListItem}
 */
public abstract class ListManager<T extends ManageableListItem & StreamManager>
        implements StreamManager {

    /**
     * Create a new ManageableListItem for the given type.
     */
    private final ItemFactory<T> factory;

    /**
     * The managed list of items.
     */
    private final List<T> items;

    /**
     * The registry for item lookup and validation.
     */
    private final Registry registry;

    /**
     * The type of items managed by this list, used for registry operations.
     */
    private final Class<T> itemType;

    /**
     * Constructs a new {@code ListManager} with an empty item list.
     *
     * @param factory a ManageableListItem for the given type
     * @param registry the registry for item lookup
     * @param itemType the class of the items (e.g., {@code Exam.class})
     * @throws NullPointerException if {@code registry} or {@code itemType} is null
     */
    ListManager(ItemFactory<T> factory, Registry registry, Class<T> itemType) {
        this.factory = Objects.requireNonNull(factory, "Faactory cannot be null");
        this.registry = Objects.requireNonNull(registry, "Registry cannot be null");
        this.itemType = Objects.requireNonNull(itemType, "Item type cannot be null");
        this.items = new ArrayList<>();
    }

    /**
     * Gets the managed list of items.
     *
     * @return the list of items
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * Adds an item to the list if it is registered in the {@link Registry}. The item
     * is assumed to have registered itself upon creation.
     *
     * @param item the item to add
     * @throws IllegalStateException if the item is not registered
     * @throws NullPointerException  if the item is null
     */
    public void add(T item) {
        Objects.requireNonNull(item, "Item cannot be null");
        T registeredItem = registry.find(item.getId(), itemType);
        if (registeredItem != item) {
            throw new IllegalStateException("Item with ID " + item.getId()
                    + " is not registered for type " + itemType.getSimpleName());
        }
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    /**
     * Removes an item from the list.
     *
     * @param item the item to remove
     */
    public void remove(T item) {
        items.remove(item);
    }

    /**
     * Removes all items from the list and the {@link Registry}.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Returns the number of items in the list.
     *
     * @return the size of the list
     */
    public int size() {
        return items.size();
    }

    /**
     * Returns a copy of the list of all managed items.
     *
     * @return a new {@code ArrayList} containing all items
     */
    public ArrayList<T> all() {
        return new ArrayList<>(items);
    }

    /**
     * Replaces the contents of this list with the provided items, ensuring all are registered.
     *
     * @param source the list of items to add
     * @throws IllegalStateException if any item is not registered
     */
    public void addAll(ArrayList<T> source) {
        clear();
        if (source != null) {
            for (T item : source) {
                add(item);
            }
        }
    }

    /**
     * Reads data from disk, populating the list by constructing items of the appropriate
     * type. Items register themselves with the {@link Registry} during construction.
     *
     * @param br       reader, already opened
     * @param registry the global registry (ignored, uses own)
     * @param nthItem  position in the stream
     * @throws IOException      if a stream error occurs
     * @throws RuntimeException if the file format is invalid
     */
    @SuppressWarnings("unchecked")
    @Override
    public void streamIn(BufferedReader br, Registry registry, int nthItem)
            throws IOException, RuntimeException {
        clear();

        String listClassName = this.getClass().getSimpleName();
        String className = listClassName.substring(0, listClassName.length() - "List".length());
        String classNames = className + "s";

        String line = CSSE7023.getLine(br);

        if (Objects.requireNonNull(line).charAt(0) != '['
                || line.charAt(line.length() - 1) != ']') {
            throw new RuntimeException("Invalid file format reading " + classNames);
        }

        String[] tokens = CSSE7023.keyValuePair(line.substring(1, line.length() - 1));
        assert tokens != null;
        if (!tokens[0].equals(classNames)) {
            throw new RuntimeException("Invalid file format - looking for " + classNames
                    + ", found " + tokens[0]);
        }

        int itemsCount = CSSE7023.toInt(tokens[1], "Number format exception parsing "
                + classNames + " header");

        if (Verbose.isVerbose()) {
            System.out.println("\nLoading " + itemsCount + " " + classNames.toLowerCase()
                    + "...\n");
        }
        int loadedCount = 0;
        for (int i = 1; i <= itemsCount; i++) {
            T item = factory.createItem(br, registry, i);
            if (item.getId() == null) {
                if (Verbose.isVerbose()) {
                    System.out.println("Skipped " + className + " #" + i + " with null ID");
                }
                continue;
            }
            items.add(item);
            loadedCount++;
        }

        if (Verbose.isVerbose()) {
            System.out.println();
        }

        System.out.println("Loaded " + loadedCount + " " + classNames.toLowerCase());
    }

    /**
     * Writes the list's contents to the provided BufferedWriter.
     * Outputs a header with the pluralized class name and size, followed by each item's
     * streamed output, and a trailing newline.
     *
     * @param bw      the BufferedWriter to write to
     * @param nthItem the index of the item (ignored in this implementation)
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void streamOut(BufferedWriter bw, int nthItem) throws IOException {
        String listClassName = this.getClass().getSimpleName();
        String className = listClassName.substring(0, listClassName.length() - "List".length());
        String classNames = className + "s";

        bw.write("[" + classNames + ": " + size() + "]\n");
        int index = 1;
        for (T item : all()) {
            item.streamOut(bw, index++);
        }
        bw.write(System.lineSeparator());

        if (Verbose.isVerbose()) {
            System.out.println("Wrote " + size() + " " + classNames.toLowerCase()
                    + " successfully");
        }
    }

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key the text used to identify the item
     * @return the item if found or null otherwise
     */
    public abstract T find(String key);

    /**
     * Finds an item by a key (e.g., ID).
     *
     * @param key the text used to identify the item
     * @return the item if found
     * @throws IllegalStateException if no item is found
     */
    public abstract T get(String key) throws IllegalStateException;

    /**
     * access the stored registry object
     *
     * @return the registry
     */
    protected Registry getRegistry() {
        return registry;
    }
}