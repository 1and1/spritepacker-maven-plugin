package com.murphybob.spritepacker;

public class Node {

    private final int x;
    private final int y;
    private int width;
    private int height;
    private Node down;
    private Node right;
    private boolean used;

    /**
     * Create a new Node with the given x position, y position, width and height
     *
     * @param xPos      x position
     * @param yPos      y position
     * @param width     width
     * @param height    height
     */
    public Node(int xPos, int yPos, int width, int height) {
        this.x = xPos;
        this.y = yPos;
        this.width = width;
        this.height = height;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Node getDown() {
        return down;
    }

    public void setDown(Node down) {
        this.down = down;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String toString() {
        return "X,Y,W,H,U: " + x + "," + y + "," + width + "," + height + "," + used;
    }

}
