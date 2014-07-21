package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Converts ImagePacking to a spritesheet PNG file.
 *
 * @author Robert Murphy, mklein
 */
public class SpritesheetPackingConverter implements PackingConverter {

    private final File output;

    /**
     * Create a spritesheet converter with output file output.
     *
     * @param output
     */
    public SpritesheetPackingConverter(File output) {
        this.output = output;
    }

    /**
     * Convert ImagePacking to a spritesheet image and save as a PNG file.
     *
     * @param imagePacking  the ImagePacking to convert
     * @param log           the log object to use
     * @throws MojoExecutionException
     */
    @Override
    public void convert(ImagePacking imagePacking, Log log) throws MojoExecutionException {
        if (output == null) {
            throw new MojoExecutionException("No spritesheet specified.");
        }

        log.info("Saving spritesheet...");

        if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Couldn't create target directory: " + output.getParentFile());
        }

        BufferedImage spritesheet = new BufferedImage(imagePacking.getWidth(), imagePacking.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = spritesheet.createGraphics();
        for (NamedImage image : imagePacking.getImages()) {
            Point imagePosition = imagePacking.getPosition(image);
            int x = imagePosition.x;
            int y = imagePosition.y;
            int width = image.getWidth();
            int height = image.getHeight();
            gfx.drawImage(image.getImage(), x, y, x + width, y + height, 0, 0, width, height, null);
        }

        try {
            ImageIO.write(spritesheet, "png", output);
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't write spritesheet: " + output, e);
        }
    }

}
