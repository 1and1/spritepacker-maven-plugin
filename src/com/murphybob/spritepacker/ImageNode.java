package com.murphybob.spritepacker;

import java.awt.image.BufferedImage;

public class ImageNode {
    private final String name;
    private final BufferedImage image;

    private Node node;

    /**
     * Create an ImageNode from the given file
     *
     * @param image the image
     * @param name the name of the image
     */
    public ImageNode(BufferedImage image, String name) {
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
        return this.image.getWidth();
    }

    public int getHeight() {
        return this.image.getHeight();
    }

    public Node getNode() {
        return node;
    }

    public void setNode(final Node node) {
        this.node = node;
    }

    public String toString() {
        return name + "\n" + node;
    }
}
