package net.oneandone.maven.plugins.spritepacker;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * An algorithm for packing images into a spritesheet
 *
 * @author Robert Murphy
 */
public class PackGrowing {

    private static final Comparator<NamedImage> IMAGE_ORDER = new Comparator<NamedImage>() {
        @Override
        public int compare(NamedImage arg0, NamedImage arg1) {
            int max0 = Math.max(arg0.getWidth(), arg0.getHeight());
            int max1 = Math.max(arg1.getWidth(), arg1.getHeight());
            return max1 - max0;
        }
    };

    private final List<NamedImage> images;
    private final int padding;

    private final Map<NamedImage, Point> positionMap;
    private Node root;

    /**
     * Creates a new PackGrowing for the given images and padding.
     *
     * @param images  the list of images to be packed
     * @param padding the amount of padding to put between sprites, in pixels
     */
    private PackGrowing(List<NamedImage> images, int padding) {
        this.images = new ArrayList<>(images);
        this.padding = padding;
        positionMap = new IdentityHashMap<>();
    }

    /**
     * Fit the images and return an ImagePacking containing the outer dimensions
     * of the spritesheet and a Map of images to positions.
     *
     * @param images    the list of images to be packed
     * @param padding   the amount of padding to put between sprites, in pixels
     * @return          the resulting ImagePacking, containing dimension and image position data
     */
    public static ImagePacking fit(List<NamedImage> images, int padding) {
        PackGrowing packGrowing = new PackGrowing(images, padding);
        Dimension dimension = packGrowing.fit();
        return new ImagePacking(dimension, packGrowing.positionMap);
    }

    /**
     * Fits the images and return the outer dimensions of the resulting spritesheet.
     *
     * @return  the outer dimensions of the spritesheet after fitting the images
     */
    private Dimension fit() {
        // sort the images, without modifying the sort order of the original image list
        sortImages(images);

        root = new Node(padding, this.padding, images.get(0).getWidth(), images.get(0).getHeight());

        for (NamedImage image : images) {
            final int width = image.getWidth();
            final int height = image.getHeight();

            final Node availableNode = findNode(root, width, height);
            final Node newNode = (availableNode == null) ? growNode(width, height, padding) : splitNode(availableNode, width, height, padding);

            positionMap.put(image, new Point(newNode.getX(), newNode.getY()));
        }

        return new Dimension(root.getWidth() + padding * 2, root.getHeight() + padding * 2);
    }

    /**
     * Sort images by max width / height descending
     *
     * @param images list of images to sort
     */
    private void sortImages(List<NamedImage> images) {
        // Sort by max width / height descending
        Collections.sort(images, IMAGE_ORDER);
    }

    /**
     * Find an available unused node of the given width and height, starting from nodeIn
     *
     * @param nodeIn the node from which to start the find operation
     * @param width  width of the node
     * @param height height of the node
     * @return       found node, or null if no available node was found
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
     * @param nodeIn the node to split
     * @param width  width
     * @param height height
     * @return       the split node
     */
    private Node splitNode(Node nodeIn, int width, int height, int padding) {
        nodeIn.setUsed(true);
        nodeIn.setDown(new Node(nodeIn.getX(), nodeIn.getY() + height + padding, nodeIn.getWidth(), nodeIn.getHeight() - height - padding));
        nodeIn.setRight(new Node(nodeIn.getX() + width + padding, nodeIn.getY(), nodeIn.getWidth() - width - padding, height));
        return nodeIn;
    }

    /**
     * Grow the size of the available space to add another block, and return new available node
     *
     * @param width  width needed
     * @param height height needed
     * @return       new available node
     */
    private Node growNode(int width, int height, int padding) {
        boolean canGrowDown = width <= root.getWidth();
        boolean canGrowRight = height <= root.getHeight();

        // attempt to keep square-ish by growing right when height is much greater than width
        boolean shouldGrowRight = canGrowRight && root.getHeight() >= root.getWidth() + width;
        // attempt to keep square-ish by growing down when width is much greater than height
        boolean shouldGrowDown = canGrowDown && root.getWidth() >= root.getHeight() + height;

        if (shouldGrowRight) {
            return growRight(width, height, padding);
        } else if (shouldGrowDown) {
            return growDown(width, height, padding);
        } else if (canGrowRight) {
            return growRight(width, height, padding);
        } else if (canGrowDown) {
            return growDown(width, height, padding);
        }

        // need to ensure sensible root starting size to avoid this happening
        return null;
    }

    /**
     * Grow the root node right, and return new available node
     *
     * @param width  width needed
     * @param height height needed
     * @return       new available node
     */
    private Node growRight(int width, int height, int padding) {
        Node newRoot = new Node(root.getX(), root.getY(), root.getWidth() + width + padding, root.getHeight());
        newRoot.setUsed(true);
        newRoot.setDown(root);
        newRoot.setRight(new Node(root.getWidth() + root.getX() + padding, root.getY(), width, root.getHeight()));
        root = newRoot;

        Node availableNode = findNode(root, width, height);
        if (availableNode != null) {
            return splitNode(availableNode, width, height, padding);
        }

        return null;
    }

    /**
     * Grow the root node down and return new available node
     *
     * @param width  width needed
     * @param height height needed
     * @return       new available node
     */
    private Node growDown(int width, int height, int padding) {
        Node newRoot = new Node(root.getX(), root.getY(), root.getWidth(), root.getHeight() + height + padding);
        newRoot.setUsed(true);
        newRoot.setDown(new Node(root.getX(), root.getY() + root.getHeight() + padding, root.getWidth(), height));
        newRoot.setRight(root);
        root = newRoot;

        Node availableNode = findNode(root, width, height);
        if (availableNode != null) {
            return splitNode(availableNode, width, height, padding);
        }

        return null;
    }

}
