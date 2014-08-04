package net.oneandone.maven.plugins.spritepacker.converters;

import com.google.common.base.Splitter;
import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An abstract PackingConverter that saves the output of the subclass's implemented createOutput method to a text file.
 *
 * @author mklein
 */
public abstract class AbstractTextConverter implements PackingConverter {
    // Identifiers can contain any unicode letters (\p{L}), numbers (\p{N}), underscore and hyphen, so "ω⓪-⑨_A3" is a valid identifier.
    public static final Pattern CHARS_NOT_ALLOWED_IN_IDENTIFIERS = Pattern.compile("[^\\p{L}\\p{N}_-]");
    private static final Splitter LINE_SPLITTER = Splitter.onPattern("\\r?\\n");
    private final Path file;
    private final String type;

    /**
     * Create an AbstractTextConverter that saves to the specified file, and specify the type of output file for logging purposes.
     * @param file  the output file to write to
     * @param type  the type of file to convert to, for logging purposes
     */
    protected AbstractTextConverter(Path file, String type) {
        this.file = file;
        this.type = type;
    }

    /**
     * Convert the specified ImagePacking to a text file.
     *
     * @param imageList     the list of images
     * @param imagePacking  the ImagePacking to convert
     * @param log           the log object to use
     * @throws MojoExecutionException
     */
    @Override
    public void convert(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException {
        if (file == null) {
            log.info("No " + type + " output file specified.");
            return;
        }

        log.info("Generating " + type + " output...");

        String output = createOutput(imageList, imagePacking, log);
        try {
            log.info("Saving " + type + " to file " + file.toAbsolutePath());
            // Ensure that all line endings use the system specific line separator.
            Files.write(file, LINE_SPLITTER.split(output), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't write to file " + file.toAbsolutePath(), e);
        }
    }

    /**
     * Create output text as String based on an ImagePacking.
     *
     * @param imageList     the list of images
     * @param imagePacking  the ImagePacking to convert
     * @param log           the log object to use
     * @return              String containing the text file contents
     * @throws MojoExecutionException
     */
    protected abstract String createOutput(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException;

    /**
     * Convert an integer into a String pixel value, leaving off the "px" for values of 0.
     *
     * @param i     the int value to convert
     * @return      the pixel value
     */
    protected static String intToPixel(int i) {
        return i == 0 ? "0" : i + "px";
    }

    /**
     * Sanitize name for use in CSS or Less by removing all characters that are not letters, numbers
     * hyphens or underscores.
     *
     * @param name  the name to be sanitized
     * @return      sanitized name without special characters
     */
    protected static String sanitize(String name) {
        return (name == null) ? null : CHARS_NOT_ALLOWED_IN_IDENTIFIERS.matcher(name).replaceAll("");
    }

    /**
     * Fix names by checking if the first character is a letter or an underscore.
     * If not, insert an underscore at the beginning of the name in order to make it valid for
     * use in CSS or Less.
     *
     * @param name  the name to fix
     * @return      valid name without number or hyphen as first character
     */
    protected static String fixFirstChar(String name) {
        boolean firstCharIsAllowed = name == null || name.isEmpty() || name.charAt(0) == '_' || Character.isLetter(name.charAt(0));
        return firstCharIsAllowed ? name : "_" + name;
    }
}
