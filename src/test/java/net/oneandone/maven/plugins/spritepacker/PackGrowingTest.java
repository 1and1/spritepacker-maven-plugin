package net.oneandone.maven.plugins.spritepacker;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for PackGrowing
 *
 * @author ssiegler
 */
public class PackGrowingTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testMappingWithoutImages() throws Exception {
        ImagePacking fit = PackGrowing.fit(Collections.<NamedImage>emptyList(), 3);
        errorCollector.checkThat(fit.getHeight(), is(0));
        errorCollector.checkThat(fit.getWidth(), is(0));
    }

    @Test
    public void testMappingForSingleImage() throws Exception {
        int padding = 5;
        NamedImage image = new NamedImage(new BufferedImage(30, 20, BufferedImage.TYPE_INT_ARGB), "Bild");
        ImagePacking fit = PackGrowing.fit(Collections.singletonList(image), padding);
        errorCollector.checkThat(fit.getHeight(), is(image.getHeight() + 2 * padding));
        errorCollector.checkThat(fit.getWidth(), is(image.getWidth() + 2 * padding));
        errorCollector.checkThat(fit.getPosition(image), is(new Point(padding, padding)));
    }

    @Test
    public void testMappingForTwoImagesInARow() throws Exception {
        int padding = 8;
        int width = 43;
        int height = 34;
        List<NamedImage> images = Arrays.asList(
                new NamedImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "Bild1"),
                new NamedImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "Bild2"));
        ImagePacking fit = PackGrowing.fit(images, padding);
        errorCollector.checkThat(fit.getHeight(), is(height + 2 * padding));
        errorCollector.checkThat(fit.getWidth(), is(2 * width + 3 * padding));
        errorCollector.checkThat(fit.getPosition(images.get(0)), is(new Point(padding, padding)));
        errorCollector.checkThat(fit.getPosition(images.get(1)), is(new Point(2 * padding + width, padding)));
    }

    @Test
    public void testGettingWider() throws Exception {
        int padding = 8;
        int firstWidth = 3;
        int secondWidth = 13;
        int height = 34;
        List<NamedImage> images = Arrays.asList(
                new NamedImage(new BufferedImage(firstWidth, height, BufferedImage.TYPE_INT_ARGB), "Bild1"),
                new NamedImage(new BufferedImage(secondWidth, height, BufferedImage.TYPE_INT_ARGB), "Bild2"));
        ImagePacking fit = PackGrowing.fit(images, padding);
        errorCollector.checkThat(fit.getHeight(), is(height + 2 * padding));
        errorCollector.checkThat(fit.getWidth(), is(firstWidth + secondWidth + 3 * padding));
        errorCollector.checkThat(fit.getPosition(images.get(0)), is(new Point(padding, padding)));
        errorCollector.checkThat(fit.getPosition(images.get(1)), is(new Point(2 * padding + firstWidth, padding)));
    }

    @Test
    public void testMappingForTwoImagesInARowAndShouldGrowRight() throws Exception {
        int padding = 8;
        int width = 43;
        int firstHeight = 200;
        int secondHeight = 34;
        List<NamedImage> images = Arrays.asList(
                new NamedImage(new BufferedImage(width, firstHeight, BufferedImage.TYPE_INT_ARGB), "Bild1"),
                new NamedImage(new BufferedImage(width, secondHeight, BufferedImage.TYPE_INT_ARGB), "Bild2"));
        ImagePacking fit = PackGrowing.fit(images, padding);
        errorCollector.checkThat(fit.getHeight(), is(firstHeight + 2 * padding));
        errorCollector.checkThat(fit.getWidth(), is(2 * width + 3 * padding));
        errorCollector.checkThat(fit.getPosition(images.get(0)), is(new Point(padding, padding)));
        errorCollector.checkThat(fit.getPosition(images.get(1)), is(new Point(2 * padding + width, padding)));
    }

    @Test
    public void testMappingForTwoImagesInAColumn() throws Exception {
        int padding = 8;
        int width = 43;
        List<NamedImage> images = Arrays.asList(
                new NamedImage(new BufferedImage(width, 34, BufferedImage.TYPE_INT_ARGB), "Bild1"),
                new NamedImage(new BufferedImage(width, 35, BufferedImage.TYPE_INT_ARGB), "Bild2"));
        ImagePacking fit = PackGrowing.fit(images, padding);
        errorCollector.checkThat(fit.getHeight(), is(images.get(0).getHeight() + images.get(1).getHeight() + 3 * padding));
        errorCollector.checkThat(fit.getWidth(), is(width + 2 * padding));
        errorCollector.checkThat(fit.getPosition(images.get(0)), is(new Point(padding, padding)));
        errorCollector.checkThat(fit.getPosition(images.get(1)), is(new Point(padding, 2 * padding + images.get(0).getHeight())));
    }

    @Test
    public void testMappingForManyImages() throws Exception {
        int size = 500;
        List<NamedImage> images = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            images.add(new NamedImage(new BufferedImage(random.nextInt(1, 1024), random.nextInt(1, 1024), BufferedImage.TYPE_INT_ARGB), "Bild" + i));
        }

        ImagePacking fit = PackGrowing.fit(images, 2);
        Rectangle boundingBox = new Rectangle(fit.getWidth(), fit.getHeight());

        List<Rectangle> rects = new ArrayList<>(size);

        for (NamedImage image : images) {
            Point position = fit.getPosition(image);
            errorCollector.checkThat(position, is(notNullValue()));

            rects.add(new Rectangle(position.x, position.y, image.getWidth(), image.getHeight()));
            errorCollector.checkThat(boundingBox.contains(position), is(true));
        }

        int overlappingCount = 0;
        for (int i = 0; i < rects.size(); i++) {
            for (int j = 0; j < rects.size(); j++) {
                if (i != j) {
                    if (rects.get(i).contains(rects.get(j).getLocation())) {
                        overlappingCount++;
                    }
                }
            }
        }

        errorCollector.checkThat("Overlapping images", overlappingCount, is(0));
    }

    @Test
    public void cannotGrowDownIfWiderThanRoot() throws Exception {
        PackGrowing packGrowing = new PackGrowing(Collections.<NamedImage>emptyList(), 0);
        packGrowing.setRoot(new Node(0, 0, 0, 0));
        assertThat(packGrowing.growDown(1, 0, 0), is(nullValue()));
    }

    @Test
    public void cannotGrowRightIfHigherThanRoot() throws Exception {
        PackGrowing packGrowing = new PackGrowing(Collections.<NamedImage>emptyList(), 0);
        packGrowing.setRoot(new Node(0, 0, 0, 0));
        assertThat(packGrowing.growRight(0, 1, 0), is(nullValue()));
    }

    @Test
    public void cannotGrowIfHigherAndWiderThanRoot() throws Exception {
        PackGrowing packGrowing = new PackGrowing(Collections.<NamedImage>emptyList(), 0);
        packGrowing.setRoot(new Node(0, 0, 0, 0));
        assertThat(packGrowing.growNode(1, 1, 0), is(nullValue()));
    }
}