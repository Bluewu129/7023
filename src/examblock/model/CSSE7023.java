package examblock.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Utility functions to ease the workload and provide consistency
 */
public class CSSE7023 {

    /**
     * The value returned by getVersionNumber() if Cancel pressed or an empty string was returned
     * The value shall be 0xBAD.C0FFEE (2989.12648430)
     */
    public static final double BAD_VERSION = 2989.12648430;

    /**
     * A quick method to see if a version is bad. We need this because floating point can round off
     * in nasty ways
     *
     * @param version the version to check
     * @return true if the version is bad, otherwise false
     */
    public static boolean isBadVersion(double version) {
        return String.format("%.8f", version).equals(String.format("%.8f", BAD_VERSION));
    }

    /**
     * Convenience helper - splits a key/value string like "Rooms: R17" into a String array
     * containing "Rooms" and "R17". All string returned will be trimmed of leading
     * and trailing spaces
     *
     * @param keyValue a single string representation of a key/value pair, with the key
     *                 and value separated by a colon ':' character.
     * @return String[] containing
     */
    public static String[] keyValuePair(String keyValue) {

        int index = keyValue.indexOf(":");
        if (index != -1) {
            String[] result = new String[2];
            result[0] = keyValue.substring(0, index).trim();
            result[1] = keyValue.substring(index + 1).trim();
            return result;
        }
        return null;
    }

    /**
     * Convenience helper - splits a key/values string like "Rooms: R1 R2" into a String array
     * containing Room and all the values following it. Multiple values are separated
     * from each other with {@param sep} characters. All string returned will be trimmed of leading
     * and trailing spaces
     *
     * @param keyValues a single string representation of a key/values pair
     * @param sep       the separator between consecutive values. Cannot be a colon ":" or comma ","
     * @return String[] containing
     */
    public static String[] keyValuePair(String keyValues, String sep) {
        ArrayList<String> result = new ArrayList<>();
        String[] keyValuesSplit = keyValuePair(keyValues);
        assert keyValuesSplit != null;
        result.add(keyValuesSplit[0].trim());

        String[] values = keyValuesSplit[1].split(sep);

        Arrays.stream(values).filter(s -> !s.isEmpty()).map(String::trim).forEach(result::add);
        return result.toArray(new String[0]);
    }

    /**
     * Convenience helper method to read the next non-blank, non-comment string from the reader
     * and return trimmed string.
     *
     * @param br        an already opened BufferedReader
     * @param sneakPeek true to peek ahead at the next line without removing it, so it can be read
     *                  again later
     * @return the next valid line of text, or null if no characters were read
     */
    public static String getLine(BufferedReader br, boolean sneakPeek) {

        String line = "";

        try {

            if (sneakPeek) {
                br.mark(1024);   // Should be enough
            }

            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {

                    if (sneakPeek) {
                        br.reset();
                    }

                    return line;
                }
            }
        } catch (IOException e) {
            // Return below
        }

        return null;
    }

    /**
     * Convenience helper method to read the next non-blank, non-comment string from the reader
     * and return trimmed string.
     *
     * @param br an already opened BufferedReader
     * @return the next string in the stream, or null if no characters were read
     */
    public static String getLine(BufferedReader br) {

        return getLine(br, false);
    }

    /**
     * Try to convert the input into an integer. If it can't, throw a RuntimeException with message
     *
     * @param str                 The string holding the number to be converted
     * @param errMessageOnFailure The string to use as the RuntimeException message
     * @return the int value of str
     * @throws RuntimeException if the number can't be converted
     */
    public static int toInt(String str, String errMessageOnFailure) throws RuntimeException {

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new RuntimeException(errMessageOnFailure);
        }
    }

    /**
     * Try to convert the input into a long. If it can't, throw a RuntimeException with message
     *
     * @param str                 The string holding the number to be converted
     * @param errMessageOnFailure The string to use as the RuntimeException message
     * @return the long value of str
     * @throws RuntimeException if the number can't be converted
     */
    public static long toLong(String str, String errMessageOnFailure) throws RuntimeException {

        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new RuntimeException(errMessageOnFailure);
        }
    }

    /**
     * Try to convert the input into a double. If it can't, throw a RuntimeException with message
     *
     * @param str                 The string holding the number to be converted
     * @param errMessageOnFailure The string to use as the RuntimeException message
     * @return the double value of str
     * @throws RuntimeException if the number can't be converted
     */
    public static double toDouble(String str, String errMessageOnFailure) throws RuntimeException {

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new RuntimeException(errMessageOnFailure);
        }
    }

    /**
     * Try to convert the input into a boolean. If it can't, throw a RuntimeException with message
     *
     * @param str                 The string holding the number to be converted
     * @param errMessageOnFailure The string to use as the RuntimeException message
     * @return true or false
     * @throws RuntimeException if the number can't be converted
     */
    public static boolean toBoolean(String str, String errMessageOnFailure)
            throws RuntimeException {

        try {
            return Boolean.parseBoolean(str);
        } catch (NumberFormatException e) {
            throw new RuntimeException(errMessageOnFailure);
        }
    }

    /**
     * Try to convert the input into a date object. If it can't, throw a RuntimeException with
     * message
     *
     * @param str                 The string holding the number to be converted
     * @param errMessageOnFailure The string to use as the RuntimeException message
     * @return the LocalDate value of str
     * @throws RuntimeException if the number can't be converted
     */
    public static LocalDate toLocalDate(String str, String errMessageOnFailure)
            throws RuntimeException {

        try {
            String[] dateBits = str.split("-");
            return LocalDate.of(Integer.parseInt(dateBits[0]),
                    Integer.parseInt(dateBits[1]), Integer.parseInt(dateBits[2]));
        } catch (NumberFormatException e) {
            throw new RuntimeException(errMessageOnFailure);
        }
    }

    /**
     * Try to convert the input into a time object. If it can't, throw a RuntimeException with
     * message
     *
     * @param str                 The string holding the number to be converted
     * @param errMessageOnFailure The string to use as the RuntimeException message
     * @return the LocalTime value of str
     * @throws RuntimeException if the number can't be converted
     */
    public static LocalTime toLocalTime(String str, String errMessageOnFailure)
            throws RuntimeException {

        try {
            String[] timeBits = str.split(":");
            if (timeBits.length == 2) {
                return LocalTime.of(Integer.parseInt(timeBits[0]), Integer.parseInt(timeBits[1]));
            } else if (timeBits.length == 3) {
                return LocalTime.of(Integer.parseInt(timeBits[0]), Integer.parseInt(timeBits[1]),
                        Integer.parseInt(timeBits[2]));
            } else {
                throw new RuntimeException("Too many (or too few) time component parts in " + str);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(errMessageOnFailure);
        }
    }

    /**
     * File type to save/load by default
     */
    public enum FileType {
        /**
         * Exam Block Data File
         */
        EBD,

        /**
         * Exam Block Finalise Report File
         */
        EFR,

        /**
         * Text File
         */
        TXT;

        /**
         * Get the file extension for this file type
         *
         * @return the file extension, or an empty string if none is applicable.
         */
        public String getExtension() {
            switch (this) {
                case EBD:
                    return "ebd";
                case EFR:
                    return "efr";
                case TXT:
                    return "txt";
                default:
                    return "";
            }
        }
    }
}
