package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the CssPackingConverter
 *
 * @author mklein
 */
@RunWith(Theories.class)
public class CssPackingConverterTest {
    @DataPoints
    public static final String[] names = { null, "", ".", "abc", "0815-test", "&9.test" };

    private static final String PREFIX = "test";

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private Log log;
    private ImagePacking packing;
    private List<NamedImage> imageList;

    @Before
    public void before() throws Exception {
        log = mock(Log.class);
        imageList = getImageList();
        packing = getImagePacking(imageList);
    }

    private List<NamedImage> getImageList() {
        List<NamedImage> imageList = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            NamedImage image = new NamedImage(new BufferedImage(10 + i, 600 - i, BufferedImage.TYPE_INT_ARGB), "img" + i);
            imageList.add(image);
        }
        return imageList;
    }

    private ImagePacking getImagePacking(List<NamedImage> imageList) {
        Map<NamedImage, Point> positionMap = new HashMap<>();
        for (NamedImage image : imageList) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            positionMap.put(image, new Point(random.nextInt(0, 1023), random.nextInt(0, 1023)));
        }
        return new ImagePacking(new Dimension(1024, 1024), positionMap);
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
        try (Scanner scanner = new Scanner(output)) {
            while (scanner.hasNextLine()) {
                String trimmed = scanner.nextLine().trim();
                if (trimmed.charAt(0) != '.') {
                    continue;
                }
                NamedImage image = imageList.get(imageNumber);

                if (prefix == null || prefix.equals("")) {
                    checkLineStartsWithoutPrefix(image.getName(), trimmed);
                } else {
                    checkLineStartWithPrefix(prefix, image.getName(), trimmed);
                }

                checkLineContents(trimmed, image);

                imageNumber++;
            }
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

    private void checkLineContents(String trimmed, NamedImage image) {
        Point position = packing.getPosition(image);

        errorCollector.checkThat("Open bracket occurs once", StringUtils.countMatches(trimmed, "{"), is(1));
        errorCollector.checkThat("Close bracket occurs once at end of line", trimmed.indexOf('}'), is(trimmed.length() - 1));
        errorCollector.checkThat("Background position is correct", trimmed, containsString("background-position:" +
                                                                                           AbstractTextConverter.intToPixel(-position.x) + " " + AbstractTextConverter.intToPixel(-position.y) + ";"));
        errorCollector.checkThat("Width is correct",
                                 trimmed, containsString("width:" + AbstractTextConverter.intToPixel(image.getWidth())));
        errorCollector.checkThat("Height is correct",
                                 trimmed, containsString("height:" + AbstractTextConverter.intToPixel(image.getHeight())));
    }

    @Theory
    public void constructorEnsuresPrefixValidity(String name) throws Exception {
        errorCollector.checkThat(new CssPackingConverter(null, name).cssPrefix, is(AbstractTextConverter.fixFirstChar(AbstractTextConverter.sanitize(name))));
    }

    @Theory
    public void testGetCssClassNameWithoutPrefix(String name) {
        String returnedClassName = CssPackingConverter.getCssClassName(null, name);
        errorCollector.checkThat("Expected class name is returned", returnedClassName, is(AbstractTextConverter.fixFirstChar(AbstractTextConverter.sanitize(name))));
    }

    @Theory
    public void testGetCssClassNameWithPrefix(String name) {
        String returnedClassName = CssPackingConverter.getCssClassName(PREFIX, name);
        errorCollector.checkThat("Expected class name is returned", returnedClassName, is(PREFIX + "-" + AbstractTextConverter.sanitize(name)));
    }

}
