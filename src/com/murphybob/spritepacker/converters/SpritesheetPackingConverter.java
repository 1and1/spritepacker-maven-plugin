package com.murphybob.spritepacker.converters;

import com.murphybob.spritepacker.ImagePacking;
import com.murphybob.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * TODO mklein: document class purpose
 * <p/>
 *
 * @author mklein
 */
public class SpritesheetPackingConverter implements PackingConverter {

    private final File output;

    public SpritesheetPackingConverter(File output) {
        this.output = output;
    }

    /**
     * Save list of packed images
     *
     * @param imagePacking the result of the packing
     * @throws org.apache.maven.plugin.MojoExecutionException
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
        for (NamedImage imageNode : imagePacking.getImages()) {
            Point imagePosition = imagePacking.getPosition(imageNode);
            int x = imagePosition.x;
            int y = imagePosition.y;
            int width = imageNode.getWidth();
            int height = imageNode.getHeight();
            gfx.drawImage(imageNode.getImage(), x, y, x + width, y + height, 0, 0, width, height, null);
        }

        try {
            ImageIO.write(spritesheet, "png", output);
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't write spritesheet: " + output, e);
        }
    }

}
