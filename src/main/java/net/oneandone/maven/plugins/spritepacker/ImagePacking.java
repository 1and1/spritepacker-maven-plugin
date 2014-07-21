package net.oneandone.maven.plugins.spritepacker;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Holds the result of the PackGrowing. Contains a Map of images to positions
 * as well as the outer dimensions of the spritesheet.
 *
 * @author ssiegler
 */
public class ImagePacking {
    private final Map<NamedImage, Point> positionMap;
    private final int width;
    private final int height;

    /**
     * Create an ImagePacking with the given dimension and position map data.
     *
     * @param dimension     the outer dimensions of the spritesheet
     * @param positionMap   a map of images to positions
     */
    public ImagePacking(Dimension dimension, Map<NamedImage, Point> positionMap) {
        width = dimension.width;
        height = dimension.height;
        this.positionMap = Collections.unmodifiableMap(positionMap);
    }

    /**
     * Get the position of the specified image within the spritesheet.
     *
     * @param image the image to get the position of
     * @return      the position of the given image
     */
    public Point getPosition(NamedImage image) {
        return positionMap.get(image);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "ImagePacking{" +
               "positionMap=" + positionMap +
               ", width=" + width +
               ", height=" + height +
               '}';
    }

    /**
     * Get a collection of all images that were packed
     *
     * @return  a collection of all images that were packed
     */
    public Collection<NamedImage> getImages() {
        return positionMap.keySet();
    }
}
