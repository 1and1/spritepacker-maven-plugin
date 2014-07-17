package com.murphybob.spritepacker;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Holds the result of the packing.
 * <p/>
 *
 * @author ssiegler
 */
public class ImagePacking {
    private final Map<NamedImage, Point> positionMap;
    private final int width;
    private final int height;

    public ImagePacking(Dimension dimension, Map<NamedImage, Point> positionMap) {
        width = dimension.width;
        height = dimension.height;
        this.positionMap = Collections.unmodifiableMap(positionMap);
    }

    public Point getPosition(NamedImage imageNode) {
        return positionMap.get(imageNode);
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

    public Collection<NamedImage> getImages() {
        return positionMap.keySet();
    }
}
