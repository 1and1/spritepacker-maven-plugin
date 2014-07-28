package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Unit tests for the JsonPackingConverter.
 */
public class JsonPackingConverterTest {
    private static final int HEIGHT = 100;
    private static final int WIDTH = 333;

    private static final String INDENT = "  ";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Rule
    public
    ErrorCollector errorCollector = new ErrorCollector();

    private List<NamedImage> images;
    private ImagePacking packing;
    private Map<String, Object> outputMap;

    @Before
    public void before() throws Exception {
        images = Arrays.asList(
                createImage(33, 1, "First"),
                createImage(1, 25, "Middle"),
                createImage(13, 7, "Last"));
        Map<NamedImage, Point> positions = new HashMap<>();
        positions.put(images.get(0), new Point(90, 11));
        positions.put(images.get(1), new Point(13, 10));
        positions.put(images.get(2), new Point(0, 0));
        packing = new ImagePacking(new Dimension(WIDTH, HEIGHT), positions);
        outputMap = new HashMap<>();
    }

    @Test
    public void outputMapIsCorrect() throws Exception {
        Map<String, Object> outputMap = new JsonPackingConverter(null, null).buildOutputMap(images, packing);

        errorCollector.checkThat(outputMap.keySet(), contains("First", "Middle", "Last"));
        Object first = outputMap.get("First");
        errorCollector.checkThat(first, instanceOf(Map.class));
        @SuppressWarnings("unchecked")
        Map<String, ?> firstProps = (Map<String, ?>) first;
        errorCollector.checkThat(firstProps.size(), is(6));
        errorCollector.checkThat(firstProps, allOf(hasEntry("x", "-90px"),
                                                   hasEntry("y", "-11px"),
                                                   hasEntry("w", "33px"),
                                                   hasEntry("h", "1px"),
                                                   hasEntry("xy", "-90px -11px")));
        errorCollector.checkThat(((Map) first).get("n"), instanceOf(Map.class));
        @SuppressWarnings("unchecked")
        Map<String, ?> firstNumbers = (Map<String, ?>) firstProps.get("n");
        errorCollector.checkThat(firstNumbers.size(), is(4));
        errorCollector.checkThat(firstNumbers, allOf(hasEntry("x", 90),
                                                     hasEntry("y", 11),
                                                     hasEntry("w", 33),
                                                     hasEntry("h", 1)));
    }

    @Test(expected = MojoExecutionException.class)
    public void mapsExceptionFromJsonMapper() throws Exception {
        JsonPackingConverter converter = stubOutputMap(null);
        outputMap.put("invalid", new Object());
        converter.createOutput(images, packing, mock(Log.class));
    }

    @Test
    public void createOutputWithoutJsonpVar() throws Exception {
        JsonPackingConverter converter = stubOutputMap(null);

        String output = converter.createOutput(images, packing, mock(Log.class));

        assertThat(output, is("{" + LINE_SEPARATOR
                              + INDENT + "\"some key\" : {" + LINE_SEPARATOR
                              + INDENT + INDENT + "\"number\" : 7," + LINE_SEPARATOR
                              + INDENT + INDENT + "\"key\" : \"value\"" + LINE_SEPARATOR
                              + INDENT + "}," + LINE_SEPARATOR
                              + INDENT + "\"another key\" : \"a string value\"," + LINE_SEPARATOR
                              + INDENT + "\"third time is a charm\" : 123456789" + LINE_SEPARATOR
                              + "}"));
    }

    @Test
    public void createOutputWithJsonpVar() throws Exception {
        String jsonpVar = "  a name which is currently not validated and can contain strænge Σymbols";
        JsonPackingConverter converter = stubOutputMap(jsonpVar);

        String output = converter.createOutput(images, packing, mock(Log.class));

        assertThat(output, is(jsonpVar + " = {" + LINE_SEPARATOR
                              + INDENT + "\"some key\" : {" + LINE_SEPARATOR
                              + INDENT + INDENT + "\"number\" : 7," + LINE_SEPARATOR
                              + INDENT + INDENT + "\"key\" : \"value\"" + LINE_SEPARATOR
                              + INDENT + "}," + LINE_SEPARATOR
                              + INDENT + "\"another key\" : \"a string value\"," + LINE_SEPARATOR
                              + INDENT + "\"third time is a charm\" : 123456789" + LINE_SEPARATOR
                              + "}"));
    }

    private JsonPackingConverter stubOutputMap(String jsonpVar) {
        JsonPackingConverter converter = spy(new JsonPackingConverter(null, jsonpVar));
        HashMap<String, Object> subMap = new HashMap<>();
        outputMap.put("some key", subMap);
        subMap.put("key", "value");
        subMap.put("number", 7);
        outputMap.put("another key", "a string value");
        outputMap.put("third time is a charm", 123456789L);
        doReturn(outputMap).when(converter).buildOutputMap(anyListOf(NamedImage.class), any(ImagePacking.class));
        return converter;
    }

    private Matcher<Map<? extends String, ?>> hasEntry(String key, Object value) {
        return Matchers.hasEntry(key, value);
    }

    private NamedImage createImage(int width, int height, String name) {
        return new NamedImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), name);
    }
}