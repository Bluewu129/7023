package examblock.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * This class provides an interface to stream data to and from disk. It use replaces the
 * static data in the Catalogue class in A1
 */
public interface StreamManager {

    /**
     * Used to write data to the disk.<br>
     * <br>
     * The format of the text written to the stream must be matched exactly by streamIn, so it
     * is very important to format the output as described.<br>
     * <br>
     * For example<br>
     * <br>
     *
     * @param bw      writer, already opened. Your data should be written at the current
     *                file position
     * @param nthItem a number representing this item's position in the stream. Used for sanity
     *                checks
     * @throws IOException on any stream related issues
     */
    void streamOut(BufferedWriter bw, int nthItem) throws IOException;

    /**
     * Used to read data from the disk. IOExceptions and RuntimeExceptions must be allowed
     * to propagate out to the calling method, which co-ordinates the streaming. Any other
     * exceptions should be converted to RuntimeExceptions and rethrown.<br>
     * <br>
     * For the format of the text in the input stream, refer to the {@code streamOut} documentation.
     *
     * @param br         reader, already opened
     * @param registry the global object registry
     * @param nthItem    a number representing this item's position in the stream. Used for sanity
     *                   checks
     * @throws IOException      on any stream related issues
     * @throws RuntimeException on any logic related issues
     */
    void streamIn(BufferedReader br, Registry registry,
                  int nthItem)
            throws IOException, RuntimeException;
}
