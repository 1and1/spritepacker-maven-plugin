package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * An abstract PackingConverter that saves the output of the subclass's implemented createOutput method to a text file.
 *
 * @author mklein
 */
public abstract class AbstractTextConverter implements PackingConverter {
    private final File file;
    private final String type;

    /**
     * Create an AbstractTextConverter that saves to the specified file, and specify the type of output file for logging purposes.
     *
     * @param file  the output file to write to
     * @param type  the type of file to convert to, for logging purposes
     */
    protected AbstractTextConverter(File file, String type) {
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
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file.toPath(), Charset.forName("UTF-8"))) {
                log.info("Saving " + type + " to file " + file.getAbsolutePath());
                bufferedWriter.write(output);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't write to file " + file.getAbsolutePath(), e);
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
}
