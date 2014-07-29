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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the LessPackingConverter
 *
 * @author mklein
 */
@RunWith(Theories.class)
public class LessPackingConverterTest {
    @DataPoints
    public static final String[] names = { "", ".", "abc", "0815-test", "&9.test" };

    public static final String NAMESPACE = "test";
    private static final int NUM_IMAGES = 500;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private Log log;
    private ImagePacking packing;
    private List<NamedImage> imageList;
    private Map<String, NamedImage> imageMap;

    @Before
    public void before() throws Exception {
        log = mock(Log.class);
        initImages();
        packing = getImagePacking(imageList);
    }

    private void initImages() {
        imageList = new ArrayList<>(NUM_IMAGES);
        imageMap = new HashMap<>(NUM_IMAGES);
        for (int i = 0; i < NUM_IMAGES; i++) {
            String imageName = "img" + i;
            NamedImage image = new NamedImage(new BufferedImage(10 + i, 600 - i, BufferedImage.TYPE_INT_ARGB), imageName);
            imageList.add(image);
            imageMap.put(AbstractTextConverter.sanitize(imageName), image);
        }
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
    public void testOutputWithNamespace() throws Exception {
        testOutput(NAMESPACE);
    }

    @Test
    public void testOutputWithoutNamespace() throws Exception {
        testOutput(null);
    }

    private void testOutput(String namespace) throws Exception {
        LessPackingConverter converter = new LessPackingConverter(null, namespace);

        String output = converter.createOutput(imageList, packing, log);

        Map<NamedImage, Point> positions = new HashMap<>(NUM_IMAGES);
        List<NamedImage> sizes = new ArrayList<>(NUM_IMAGES);
        boolean namespaceDefined = false;
        boolean createDefined = false;
        boolean namespaceEnded = false;

        try (Scanner scanner = new Scanner(output)) {
            while (scanner.hasNextLine()) {
                String trimmed = scanner.nextLine().trim();
                char firstChar = trimmed.charAt(0);

                // skip over comments
                if (!(firstChar == '.' || firstChar == '#' || firstChar == '}')) {
                    continue;
                }

                // # signifies namespace
                if (firstChar == '#') {
                    errorCollector.checkThat("Namespace definition is expected", namespace, not(isEmptyOrNullString()));
                    errorCollector.checkThat("Namespace syntax is correct", trimmed, is("#" + namespace + "{"));
                    errorCollector.checkThat("Namespace only defined once", namespaceDefined, is(false));
                    namespaceDefined = true;
                    continue;
                }

                // if no comment and no namespace -> start of mixin definitions

                if (!createDefined) {
                    errorCollector.checkThat("Namespace was already defined if specified", StringUtils.isNotEmpty(namespace), is(namespaceDefined));
                    errorCollector.checkThat("Create mixin is first", trimmed, is(".create(@name){.pos(@name);.size(@name);}"));
                    createDefined = true;
                    continue;
                }

                // } signifies end of namespace
                if (firstChar == '}') {
                    errorCollector.checkThat("Namespace was specified", namespace, not(isEmptyOrNullString()));
                    errorCollector.checkThat("Namespace was defined", namespaceDefined, is(true));
                    errorCollector.checkThat("Namespace only ended once", namespaceEnded, is(false));
                    namespaceEnded = true;
                    continue;
                }

                errorCollector.checkThat("The namespace hasn't ended yet", namespaceEnded, is(false));

                errorCollector.checkThat("A valid position or size mixin was found", addNameToPositionMap(trimmed, positions) || addNameToSizeList(trimmed, sizes), is(true));
            }
        }

        errorCollector.checkThat("The correct number of position mixins was defined", positions.size(), is(NUM_IMAGES));
        errorCollector.checkThat("The correct number of size mixins was defined", sizes.size(), is(NUM_IMAGES));

        errorCollector.checkThat("All image positions were defined once", imageList, everyItem(isIn(positions.keySet())));
        errorCollector.checkThat("All image sizes were defined once", imageList, everyItem(isIn(sizes)));
    }

    private boolean addNameToPositionMap(String trimmed, Map<NamedImage, Point> positions) {
        if (trimmed.startsWith(".pos")) {
            String name = getNameFromMixin(trimmed);
            NamedImage image = imageMap.get(name);
            errorCollector.checkThat("Valid image name was defined", image, is(notNullValue()));
            Point position = packing.getPosition(image);
            errorCollector.checkThat("Position mixin defined correctly", trimmed,
                                     is(".pos(" + name + "){background-position:" +
                                        AbstractTextConverter.intToPixel(-position.x) + " " + AbstractTextConverter.intToPixel(-position.y) + ";}"));
            errorCollector.checkThat("Image position was not defined twice", positions.get(image), is(nullValue()));
            positions.put(image, position);
            return true;
        }
        return false;
    }

    private boolean addNameToSizeList(String trimmed, List<NamedImage> sizes) {
        if (trimmed.startsWith(".size")) {
            String name = getNameFromMixin(trimmed);
            NamedImage image = imageMap.get(name);
            errorCollector.checkThat("Valid image name was defined", image, is(notNullValue()));
            errorCollector.checkThat("Size mixin defined correctly", trimmed,
                                     is(".size(" + name + "){width:"+AbstractTextConverter.intToPixel(image.getWidth())+
                                        ";height:" + AbstractTextConverter.intToPixel(image.getHeight()) + ";}"));
            errorCollector.checkThat("Image size was not defined twice", sizes.contains(image), is(false));
            sizes.add(image);
            return true;
        }
        return false;
    }

    private String getNameFromMixin(String mixin) {
        return mixin.substring(mixin.indexOf('(')+1, mixin.indexOf(')'));
    }

    @Theory
    public void constructorEnsuresNamespaceValidity(String namespace) {
        errorCollector.checkThat(new LessPackingConverter(null, namespace).lessNamespace,
                                 is(AbstractTextConverter.fixFirstChar(AbstractTextConverter.sanitize(namespace))));
    }

    @Theory
    public void testLessImageNameSanitization(String name) throws Exception{
        List<NamedImage> imageList = Arrays.asList(new NamedImage(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB), name));
        ImagePacking packing = getImagePacking(imageList);
        String output = new LessPackingConverter(null, null).createOutput(imageList, packing, log);
        errorCollector.checkThat(getNameFromMixin(output.substring(output.lastIndexOf('.'))),
                                 is(AbstractTextConverter.sanitize(name)));
    }

}
