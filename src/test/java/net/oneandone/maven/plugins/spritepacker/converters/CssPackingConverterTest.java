package net.oneandone.maven.plugins.spritepacker.converters;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.awt.Dimension;
import java.awt.Point;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for the CssPackingConverter
 *
 * @author mklein
 */
public class CssPackingConverterTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private MojoExecutionException exception;
    private Path file;
    private Log log;
    private ImagePacking packing;
    private List<NamedImage> imageList;

    @Before
    public void before() throws Exception {
        log = mock(Log.class);

        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        file = fileSystem.getPath("/output");

        imageList = getImageList();
        packing = getImagePacking(imageList);
    }

    private List<NamedImage> getImageList() {
        List<NamedImage> imageList = new ArrayList<>(500);
        for (int i=0; i<500; i++) {
            NamedImage image = new NamedImage(null, "img"+i);
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
    public void testOutputWithPrefix() {
        // TODO
    }

    @Test
    public void testOutputWithoutPrefix() {
        // TODO
    }

    @Test
    public void testGetCssClassName() {
        // TODO
    }

}
