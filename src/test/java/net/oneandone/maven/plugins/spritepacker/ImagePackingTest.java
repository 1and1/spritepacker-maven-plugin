package net.oneandone.maven.plugins.spritepacker;

import org.junit.Test;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the ImagePacking data structure
 */
public class ImagePackingTest {
    @Test(expected = NullPointerException.class)
    public void nullDimension() throws Exception {
        new ImagePacking(null, Collections.<NamedImage, Point>emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void nullPositions() throws Exception {
        new ImagePacking(new Dimension(0,0), null);
    }

    @Test
    public void allDataInStringRepresentation() throws Exception {
        int width = 42;
        int height = 23;
        Map<NamedImage, Point> positions = new HashMap<>();
        positions.put(createNamedImage(2, 3, "nameless"), new Point(47, 11));

        String string = createImagePacking(width, height, positions).toString();

        assertThat(string, allOf(containsString(positions.toString()),
                                 containsString(String.valueOf(width)),
                                 containsString(String.valueOf(height))));
    }

    @Test
    public void returnsImagePosition() throws Exception {
        Map<NamedImage, Point> map = new HashMap<>();
        NamedImage namedImage = createNamedImage(2, 4, "no name");
        map.put(namedImage, new Point(9, 4));
        ImagePacking imagePacking = createImagePacking(100, 100, map);

        assertThat(imagePacking.getPosition(namedImage), is(new Point(9, 4)));
    }

    @Test
    public void noPositionForUnknownImage() throws Exception {
        Map<NamedImage, Point> map = new HashMap<>();
        NamedImage namedImage = createNamedImage(3, 4, "no name");
        map.put(namedImage, new Point(42, 51));
        ImagePacking imagePacking = createImagePacking(100, 100, map);

        assertThat(imagePacking.getPosition(createNamedImage(2, 2, "stranger in the night")), is(nullValue()));
    }

    @Test
    public void returnsHeight() throws Exception {
        assertThat(createImagePacking(12, 13, Collections.<NamedImage, Point>emptyMap()).getHeight(), is(13));
    }

    @Test
    public void returnsWidth() throws Exception {
        assertThat(createImagePacking(19, 22, Collections.<NamedImage, Point>emptyMap()).getWidth(), is(19));
    }

    protected NamedImage createNamedImage(int width, int height, String name) {
        return new NamedImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), name);
    }

    protected ImagePacking createImagePacking(int width, int height, Map<NamedImage, Point> positions) {
        return new ImagePacking(new Dimension(width, height), positions);
    }
}