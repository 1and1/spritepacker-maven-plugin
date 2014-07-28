package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.InOrder;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

/**
 * Unit tests for the CssPackingConverter
 *
 * @author mklein
 */
public class CssPackingConverterTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private Log log;
    private ImagePacking packing;
    private List<NamedImage> imageList;
    private static final String PREFIX = "test";

    @Before
    public void before() throws Exception {
        log = mock(Log.class);
        imageList = getImageList();
        packing = getImagePacking(imageList);
    }

    private List<NamedImage> getImageList() {
        List<NamedImage> imageList = new ArrayList<>(500);
        for (int i=0; i<500; i++) {
            NamedImage image = new NamedImage(new BufferedImage(10+i, 600-i, BufferedImage.TYPE_INT_ARGB), "img"+i);
            imageList.add(image);
        }
        return imageList;
    }

    private ImagePacking getImagePacking (List<NamedImage> imageList) {
        Map<NamedImage, Point> positionMap = new HashMap<NamedImage, Point>();
        for (NamedImage image : imageList) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            positionMap.put(image, new Point(random.nextInt(0, 1023), random.nextInt(0, 1023)));
        }
        return new ImagePacking(new Dimension(1024,1024), positionMap);
    }

    @Test
    public void testOutputWithPrefix() throws Exception {
        testOutput(PREFIX);
    }

    @Test
    public void testOutputWithoutPrefix() throws Exception {
        testOutput(null);
    }

    private void testOutput(String prefix) throws Exception {
        CssPackingConverter converter = new CssPackingConverter(null, prefix);
        int imageNumber = 0;

        String output = converter.createOutput(imageList, packing, log);
        for (String line : output.split("\n")) {
            if (line.trim().charAt(0) != '.') {
                continue;
            }
            NamedImage image = imageList.get(imageNumber);

            Point position = packing.getPosition(image);
            String trimmed = line.trim();

            if (prefix == null || prefix.equals("")) {
                checkLineStartsWithoutPrefix(image.getName(), trimmed);
            } else {
                checkLineStartWithPrefix(prefix, image.getName(), trimmed);
            }

            checkLineContents(trimmed, image, converter);

            imageNumber++;
        }

        errorCollector.checkThat("500 icon classes were created", imageNumber, is(500));
    }

    private void checkLineStartsWithoutPrefix(String imageName, String trimmed) {
        errorCollector.checkThat("Line starts with \"." + imageName + "{\"",
                                 trimmed, startsWith("." + imageName + "{"));
    }

    private void checkLineStartWithPrefix(String prefix, String imageName, String trimmed) {
        errorCollector.checkThat("Line starts with \"." + prefix + "-" + imageName + "{\"",
                                 trimmed, startsWith("." + prefix + "-" + imageName + "{"));
    }

    private void checkLineContents(String trimmed, NamedImage image, CssPackingConverter converter) {
        Point position = packing.getPosition(image);

        errorCollector.checkThat("Open bracket occurs once", StringUtils.countMatches(trimmed, "{"), is(1));
        errorCollector.checkThat("Close bracket occurs once at end of line", trimmed.indexOf('}'), is(trimmed.length()-1));
        errorCollector.checkThat("Background position is correct", trimmed, containsString("background-position:" +
                                   converter.intToPixel(-position.x) + " " + converter.intToPixel(-position.y) + ";"));
        errorCollector.checkThat("Width is correct",
                                 trimmed, containsString("width:" + converter.intToPixel(image.getWidth())));
        errorCollector.checkThat("Height is correct",
                                 trimmed, containsString("height:" + converter.intToPixel(image.getHeight())));
    }

    @Test
    public void testGetCssClassNameWithoutPrefix() {
        CssPackingConverter converter = spy(new CssPackingConverter(null, null));

        String imageName = "validName";

        String returnedClassName = converter.getCssClassName(imageName);
        errorCollector.checkThat("Expected class name is returned", returnedClassName, is(imageName));

        // check that sanitize and fixFirstChar are called, in that order
        InOrder order = inOrder(converter);
        order.verify(converter, times(1)).sanitize(imageName);
        order.verify(converter, times(1)).fixFirstChar(imageName);

    }

    @Test
    public void testGetCssClassNameWithPrefix() {
        CssPackingConverter converter = spy(new CssPackingConverter(null, PREFIX));

        String imageName = "validName";
        String expectedClassName = PREFIX + "-" + imageName;

        String returnedClassName = converter.getCssClassName(imageName);
        errorCollector.checkThat("Expected class name is returned", returnedClassName, is(expectedClassName));

        // check that the image name was then sanitized but the first character was not fixed
        InOrder order = inOrder(converter);
        order.verify(converter, times(1)).sanitize(imageName);
        order.verify(converter, never()).fixFirstChar(any(String.class));
    }

}
