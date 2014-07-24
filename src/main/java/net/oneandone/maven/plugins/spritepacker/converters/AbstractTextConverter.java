package net.oneandone.maven.plugins.spritepacker.converters;

import com.google.common.base.Strings;
import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * An abstract PackingConverter that saves the output of the subclass's implemented createOutput method to a text file.
 *
 * @author mklein
 */
public abstract class AbstractTextConverter implements PackingConverter {
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
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file, Charset.forName("UTF-8"))) {
                log.info("Saving " + type + " to file " + file.toAbsolutePath());
                bufferedWriter.write(output);
            }
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
    protected String intToPixel(int i) {
        return i == 0 ? "0" : i + "px";
    }

    /**
     * Sanitize name for use in CSS or LESS by removing all characters that are not letters, numbers
     * hyphens or underscores.
     *
     * @param name  the name to be sanitized
     * @return      sanitized name without special characters
     */
    protected String sanitize(String name) {
        return (name == null) ? null : name.replaceAll("[^\\p{L}\\p{N}-_]", "");
    }

    /**
     * Fix names by checking if the first character is a number or hyphen.
     * If so, insert an underscore at the beginning of the name in order to make it valid for
     * use in CSS or LESS.
     *
     * @param name  the name to fix
     * @return      valid name without number or hyphen as first character
     */
    protected String fixFirstChar(String name) {
        return (Strings.isNullOrEmpty(name) || name.substring(0,1).matches("[^0-9-]")) ? name : "_" + name;
    }
}
