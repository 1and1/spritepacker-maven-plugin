package com.murphybob.spritepacker;

import org.apache.maven.plugin.MojoExecutionException;

import java.awt.image.BufferedImage;
import java.io.File;

public class ImageNode {
    private final File file;
    private final BufferedImage image;

    private Node node;

    /**
     * Create an ImageNode from the given file
     *
     * @param file  the image file path
     * @param image the image
     * @throws MojoExecutionException
     */
    public ImageNode(File file, BufferedImage image) throws MojoExecutionException {
        this.file = file;
        this.image = image;
    }

    public File getFile() {
        return file;
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
        return file.getPath() + "\n" + node;
    }

}
