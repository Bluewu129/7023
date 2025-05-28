package examblock.model;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Factory interface for creating instances of items that implement {@link ManageableListItem}
 * and {@link StreamManager}.
 *
 * @param <T> the type of item to create
 */
public interface ItemFactory<T extends ManageableListItem & StreamManager> {
    /**
     * Creates a new item from the given BufferedReader, Registry, and index.
     *
     * @param br       the BufferedReader to read from
     * @param registry the Registry for item dependencies
     * @param index    the index of the item
     * @return a new item instance
     * @throws IOException if an I/O error occurs
     */
    T createItem(BufferedReader br, Registry registry, int index) throws IOException;
}