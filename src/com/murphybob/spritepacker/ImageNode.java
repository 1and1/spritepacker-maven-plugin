package com.murphybob.spritepacker;

import org.apache.maven.plugin.MojoExecutionException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageNode {
    private File file;
    private BufferedImage image;
    private int width;
    private int height;
    private Node node;

    public ImageNode(File file) throws MojoExecutionException {
        this.file = file;
        try {
            this.image = ImageIO.read(file);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to open file: " + file.getPath(), e);
        }
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public File getFile() {
        return file;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
