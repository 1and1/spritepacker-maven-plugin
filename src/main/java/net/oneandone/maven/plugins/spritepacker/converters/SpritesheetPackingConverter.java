package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Converts ImagePacking to a spritesheet PNG file.
 *
 * @author Robert Murphy, mklein
 */
public class SpritesheetPackingConverter implements PackingConverter {

    private final Path output;

    /**
     * Create a spritesheet converter with output file output.
     *
     * @param output the path to the output file
     */
    public SpritesheetPackingConverter(Path output) {
        this.output = output;
    }

    /**
     * Convert ImagePacking to a spritesheet image and save as a PNG file.
     *
     *
     * @param imageList     the list of images
     * @param imagePacking  the ImagePacking to convert
     * @param log           the log object to use
     * @throws MojoExecutionException
     */
    @Override
    public void convert(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException {
        if (output == null) {
            throw new MojoExecutionException("No spritesheet specified.");
        }

        try {
            Files.createDirectories(output.getParent());
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't create target directory: " + output.getParent(), e);
        }

        log.info("Generating spritesheet...");

        BufferedImage spritesheet = createSpritesheet(imageList, imagePacking);

        log.info("Saving spritesheet to file " + output.toAbsolutePath());

        try {
            ImageIO.write(spritesheet, "png", Files.newOutputStream(output));
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't write spritesheet " + output.toAbsolutePath(), e);
        }
    }

    protected BufferedImage createSpritesheet(List<NamedImage> imageList, ImagePacking imagePacking) {
        BufferedImage spritesheet = new BufferedImage(imagePacking.getWidth(), imagePacking.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = spritesheet.createGraphics();
        gfx.getComposite();
        gfx.setComposite(AlphaComposite.Src);
        for (NamedImage image : imageList) {
            Point imagePosition = imagePacking.getPosition(image);
            int x = imagePosition.x;
            int y = imagePosition.y;
            int width = image.getWidth();
            int height = image.getHeight();
            gfx.drawImage(image.getImage(), x, y, x + width, y + height, 0, 0, width, height, null);
        }
        return spritesheet;
    }

}
