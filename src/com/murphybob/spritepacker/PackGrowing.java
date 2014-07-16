package com.murphybob.spritepacker;

import java.util.List;

public class PackGrowing {

    private Node root;
    private int padding = 0;

    /*
     * Creates a PackGrowing instance with the given number of padding pixels
     *
     * @param	padding	padding in pixels
     */
    public PackGrowing(int padding) {
        this.padding = padding;
    }

    /*
     * Adds Node node property to each element in an array of ImageNodes according to how it could be packed into a larger rectangle
     *
     * @param	images	An ArrayList of ImageNodes to be packed. This should be sorted largest to smallest max side for best results.
     */
    public Node fit(List<ImageNode> images) {

        root = new Node(padding, padding, images.get(0).getWidth(), images.get(0).getHeight());

        for (ImageNode imageNode : images) {
            final int width = imageNode.getWidth();
            final int height = imageNode.getHeight();

            final Node availableNode = findNode(root, width, height);
            final Node newNode = (availableNode == null) ? growNode(width, height) : splitNode(availableNode, width, height);

            imageNode.setNode(newNode);
        }

        root.setWidth(root.getWidth() + padding * 2);
        root.setHeight(root.getHeight() + padding * 2);

        return root;
    }

    // Find a place for this node
    private Node findNode(Node nodeIn, int w, int h) {
        if (nodeIn.isUsed()) {
            Node temp = findNode(nodeIn.getRight(), w, h);
            if (temp != null) {
                nodeIn = temp;
                return nodeIn;
            } else {
                return findNode(nodeIn.getDown(), w, h);
            }
        } else if (w <= nodeIn.getWidth() && h <= nodeIn.getHeight()) {
            return nodeIn;
        } else {
            return null;
        }
    }

    // Split a node into a node of size w x h and return the remaining space to the pool
    private Node splitNode(Node nodeIn, int width, int height) {
        nodeIn.setUsed(true);
        nodeIn.setDown(new Node(nodeIn.getX(), nodeIn.getY() + height + padding, nodeIn.getWidth(), nodeIn.getHeight() - height - padding));
        nodeIn.setRight(new Node(nodeIn.getX() + width + padding, nodeIn.getY(), nodeIn.getWidth() - width - padding, height));
        return nodeIn;
    }

    // Grow the size of the available space to add another block
    private Node growNode(int width, int height) {
        boolean canGrowDown = (width <= root.getWidth());
        boolean canGrowRight = (height <= root.getHeight());

        boolean shouldGrowRight = canGrowRight && (root.getHeight() >= (root.getWidth() + width)); // attempt to keep square-ish by growing right when height is much greater than width
        boolean shouldGrowDown = canGrowDown && (root.getWidth() >= (root.getHeight() + height)); // attempt to keep square-ish by growing down  when width  is much greater than height

        if (shouldGrowRight) {
            return growRight(width, height);
        } else if (shouldGrowDown) {
            return growDown(width, height);
        } else if (canGrowRight) {
            return growRight(width, height);
        } else if (canGrowDown) {
            return growDown(width, height);
        } else {
            return null; // need to ensure sensible root starting size to avoid this happening
        }
    }

    // Grow right
    private Node growRight(int width, int height) {
        Node newRoot = new Node(root.getX(), root.getY(), root.getWidth() + width + padding, root.getHeight());
        newRoot.setUsed(true);
        newRoot.setDown(root);
        newRoot.setRight(new Node(root.getWidth() + root.getX() + padding, root.getY(), width, root.getHeight()));
        root = newRoot;

        Node availableNode = findNode(root, width, height);
        if (availableNode != null) {
            return splitNode(availableNode, width, height);
        } else {
            return null;
        }
    }

    // Grow down
    private Node growDown(int width, int height) {
        Node newRoot = new Node(root.getX(), root.getY(), root.getWidth(), root.getHeight() + height + padding);
        newRoot.setUsed(true);
        newRoot.setDown(new Node(root.getX(), root.getY() + root.getHeight() + padding, root.getWidth(), height));
        newRoot.setRight(root);
        root = newRoot;

        Node availableNode = findNode(root, width, height);
        if (availableNode != null) {
            return splitNode(availableNode, width, height);
        } else {
            return null;
        }
    }

}
