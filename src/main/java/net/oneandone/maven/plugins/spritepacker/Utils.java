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
     * Check if any input is newer than any output. This also takes output files into account that do not yet exist,
     * as they have a lastModified time of 0L and are thus automatically "older" than any input files.
     *
     * @param inputs    the list of input files, must not be null
     * @param outputs   the list of output files, must not be null
     * @return          whether any input file was newer than any output file
     */
    public static boolean shouldWriteOutput(List<Path> inputs, List<Path> outputs) throws IOException {
        Objects.requireNonNull(inputs);
        Objects.requireNonNull(outputs);

        boolean hasNoInput = true;
        long newestInput = 0L;
        for (Path input : inputs) {
            if (input != null && Files.exists(input)) {
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
