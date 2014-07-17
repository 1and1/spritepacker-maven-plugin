package com.murphybob.spritepacker;

import java.awt.image.BufferedImage;

public class NamedImage {
    private final String name;
    private final BufferedImage image;

    /**
     * Create an ImageNode from the given file
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
        return "ImageNode{" +
               "name='" + name + '\'' +
               ", image=" + image +
               '}';
    }
}
