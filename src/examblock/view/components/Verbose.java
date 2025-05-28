package examblock.view.components;

/**
 * Global access to the verbose flag for the application.
 */
public class Verbose {

    /**
     * Flag to control whether or not to print verbose output.
     */
    private static boolean verbose = false;

    /**
     * Returns the current verbose flag.
     * @return true if verbose, otherwise false
     */
    public static boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the verbose flag.
     * @param v true to enable verbose mode, otherwise false to disable it.
     */
    public static void setVerbose(boolean v) {
        verbose = v;
    }
}
