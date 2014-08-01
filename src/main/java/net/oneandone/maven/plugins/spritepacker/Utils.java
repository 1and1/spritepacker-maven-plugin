package net.oneandone.maven.plugins.spritepacker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Utility methods that do not fit into specialized classes.
 * <p/>
 *
 * @author ssiegler
 */
public class Utils {
    /**
     * Avoid instantiation of utility class
     */
    private Utils() {}

    /**
     * Check if any input is newer than any output.
     * If the an input has the exact same modification time as an output the input is considered to be newer.
     *
     * @param inputs    the list of input files, must not be null
     * @param outputs   the list of output files, must not be null
     * @return          whether any input file was newer than any output file
     */
    public static boolean shouldWriteOutput(List<Path> inputs, List<Path> outputs) throws IOException {
        Objects.requireNonNull(inputs);
        Objects.requireNonNull(outputs);

        boolean hasNoInput = true;
        long newestInput = Long.MIN_VALUE;
        for (Path input : inputs) {
            if (input != null && Files.exists(input)) {
                // Files.getLastModifiedTime(input) can be an implementation specific default when a time stamp to indicate the time of last modification is not supported by the file system
                newestInput = Math.max(Files.getLastModifiedTime(input).toMillis(), newestInput);
                hasNoInput = false;
            }
        }

        if (hasNoInput) {
            return false;
        }

        for (Path output : outputs) {
            if (output != null && (Files.notExists(output) || Files.getLastModifiedTime(output).toMillis() <= newestInput)) {
                return true;
            }
        }

        return false;
    }
}
