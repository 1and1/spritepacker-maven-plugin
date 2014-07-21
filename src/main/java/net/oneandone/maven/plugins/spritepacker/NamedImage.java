package net.oneandone.maven.plugins.spritepacker;

import java.awt.image.BufferedImage;

/**
 * A class to hold a BufferedImage along with its name.
 *
 * @author Robert Murphy, mklein
 */
public class NamedImage {
    private final String name;
    private final BufferedImage image;

    /**
     * Create a NamedImage that contains an image and a name.
     *
     * @param image the image
     * @param name the name of the image
     */
    public NamedImage(BufferedImage image, String name) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public String toString() {
        return "NamedImage{" +
               "name='" + name + '\'' +
               ", image=" + image +
               '}';
    }
}
