package com.murphybob.spritepacker;

import java.util.List;

public class PackGrowing {

    private Node root;
    private int padding = 0;

    /**
     * Creates a PackGrowing instance with the given number of padding pixels
     *
     * @param	padding	padding in pixels
     */
    public PackGrowing(int padding) {
        this.padding = padding;
    }

    /**
     * Adds Node node property to each element in a list of ImageNodes according to how it could be packed into a larger rectangle
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

    /**
     * Find an available unused node of the given width and height, starting from nodeIn
     *
     * @param nodeIn    the node from which to start the find operation
     * @param width     width of the node
     * @param height    height of the node
     * @return          found node, or null if no available node was found
     */
    private Node findNode(Node nodeIn, int width, int height) {
        if (nodeIn.isUsed()) {
            Node rightAvailable = findNode(nodeIn.getRight(), width, height);
            return (rightAvailable != null) ? rightAvailable : findNode(nodeIn.getDown(), width, height);
        }

        if (width <= nodeIn.getWidth() && height <= nodeIn.getHeight()) {
            return nodeIn;
        }

        return null;
    }

    /**
     * Split a node into a node of size width x height and return the remaining space to the pool
     *
     * @param nodeIn    the node to split
     * @param width     width
     * @param height    height
     * @return          the split node
     */
    private Node splitNode(Node nodeIn, int width, int height) {
        nodeIn.setUsed(true);
        nodeIn.setDown(new Node(nodeIn.getX(), nodeIn.getY() + height + padding, nodeIn.getWidth(), nodeIn.getHeight() - height - padding));
        nodeIn.setRight(new Node(nodeIn.getX() + width + padding, nodeIn.getY(), nodeIn.getWidth() - width - padding, height));
        return nodeIn;
    }

    /**
     * Grow the size of the available space to add another block, and return new available node
     *
     * @param width     width needed
     * @param height    height needed
     * @return          new available node
     */
    private Node growNode(int width, int height) {
        boolean canGrowDown = (width <= root.getWidth());
        boolean canGrowRight = (height <= root.getHeight());

        // attempt to keep square-ish by growing right when height is much greater than width
        boolean shouldGrowRight = canGrowRight && (root.getHeight() >= (root.getWidth() + width));
        // attempt to keep square-ish by growing down when width is much greater than height
        boolean shouldGrowDown = canGrowDown && (root.getWidth() >= (root.getHeight() + height));

        if (shouldGrowRight) {
            return growRight(width, height);
        } else if (shouldGrowDown) {
            return growDown(width, height);
        } else if (canGrowRight) {
            return growRight(width, height);
        } else if (canGrowDown) {
            return growDown(width, height);
        }

        // need to ensure sensible root starting size to avoid this happening
        return null;
    }

    /**
     * Grow the root node right, and return new available node
     *
     * @param width     width needed
     * @param height    height needed
     * @return          new available node
     */
    private Node growRight(int width, int height) {
        Node newRoot = new Node(root.getX(), root.getY(), root.getWidth() + width + padding, root.getHeight());
        newRoot.setUsed(true);
        newRoot.setDown(root);
        newRoot.setRight(new Node(root.getWidth() + root.getX() + padding, root.getY(), width, root.getHeight()));
        root = newRoot;

        Node availableNode = findNode(root, width, height);
        if (availableNode != null) {
            return splitNode(availableNode, width, height);
        }

        return null;
    }

    /**
     * Grow the root node down and return new available node
     *
     * @param width     width needed
     * @param height    height needed
     * @return          new available node
     */
    private Node growDown(int width, int height) {
        Node newRoot = new Node(root.getX(), root.getY(), root.getWidth(), root.getHeight() + height + padding);
        newRoot.setUsed(true);
        newRoot.setDown(new Node(root.getX(), root.getY() + root.getHeight() + padding, root.getWidth(), height));
        newRoot.setRight(root);
        root = newRoot;

        Node availableNode = findNode(root, width, height);
        if (availableNode != null) {
            return splitNode(availableNode, width, height);
        }

        return null;
    }

}
