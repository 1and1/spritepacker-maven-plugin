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

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Unit tests for SpritesheetPackingConverter.
 */
public class SpritesheetPackingConverterTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private Log mock;
    private List<NamedImage> imageList;
    private Map<NamedImage, Point> positionMap;
    private int height;
    private int width;
    private FileSystem fileSystem;

    @Before
    public void before() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        mock = mock(Log.class);
        imageList = new ArrayList<>();
        positionMap = new IdentityHashMap<>();
        width = 1;
        height = 1;
    }

    @Test(expected = MojoExecutionException.class)
    public void throwsExceptionOnNullFile() throws Exception {
        SpritesheetPackingConverter converter = new SpritesheetPackingConverter(null);

        executeConvert(converter);
    }

    @Test(expected = MojoExecutionException.class)
    public void throwsExceptionWhenFileIsNotWritable() throws Exception {
        Path file = fileSystem.getPath("/outputFile");
        // Prevent file from being written to by creating a directory in its place.
        Files.createDirectory(file);

        SpritesheetPackingConverter converter = new SpritesheetPackingConverter(file);
        executeConvert(converter);
    }

    @Test(expected = MojoExecutionException.class)
    public void throwsExceptionWhenDirectoryCannotBeCreated() throws Exception {
        Path file = fileSystem.getPath("/outputDir/outputFile");
        // Prevent directory from being created by creating a file in its place.
        Files.createFile(file.getParent());
        SpritesheetPackingConverter converter = new SpritesheetPackingConverter(file);

        executeConvert(converter);
    }

    @Test
    public void writesToOutput() throws Exception {
        Path file = fileSystem.getPath("/outputFile");
        BufferedImage input = ImageIO.read(getClass().getResourceAsStream("/100px-Icon_subway.svg.png"));

        SpritesheetPackingConverter converter = spy(new SpritesheetPackingConverter(file));
        doReturn(input).when(converter).createSpritesheet(anyListOf(NamedImage.class), any(ImagePacking.class));

        executeConvert(converter);
        BufferedImage output = ImageIO.read(Files.newInputStream(file));

        assertImagesEqual(input, output);
    }

    @Test
    public void generatesSpritesheet() throws Exception {
        positionMap.put(loadImage("/100px-Icon_subway.svg-0-0.png"), new Point(0, 0));
        positionMap.put(loadImage("/100px-Icon_subway.svg-1-0.png"), new Point(33, 0));
        positionMap.put(loadImage("/100px-Icon_subway.svg-2-0.png"), new Point(66, 0));
        positionMap.put(loadImage("/100px-Icon_subway.svg-0-1.png"), new Point(0, 26));
        positionMap.put(loadImage("/100px-Icon_subway.svg-1-1.png"), new Point(33, 26));
        positionMap.put(loadImage("/100px-Icon_subway.svg-2-1.png"), new Point(66, 26));
        positionMap.put(loadImage("/100px-Icon_subway.svg-0-2.png"), new Point(0, 52));
        positionMap.put(loadImage("/100px-Icon_subway.svg-1-2.png"), new Point(33, 52));
        positionMap.put(loadImage("/100px-Icon_subway.svg-2-2.png"), new Point(66, 52));

        imageList.addAll(positionMap.keySet());
        width = 100;
        height = 78;

        BufferedImage spritesheet = new SpritesheetPackingConverter(null).createSpritesheet(imageList, createImagePacking());

        BufferedImage composition = ImageIO.read(getClass().getResourceAsStream("/100px-Icon_subway.svg.png"));

        assertImagesEqual(composition, spritesheet);
    }

    private void assertImagesEqual(BufferedImage image1, BufferedImage image2) {
        int width = image1.getWidth();
        int height = image1.getHeight();
        if (width != image2.getWidth() || height != image2.getHeight()) {
           fail("Image dimensions differ");
        }

        int differences = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (image1.getRGB(i, j) != image2.getRGB(i, j)) {
                    differences++;
                }
            }
        }

        assertThat("No differences", differences, is(0));
    }

    private NamedImage loadImage(String name) throws IOException {
        return new NamedImage(ImageIO.read(getClass().getResourceAsStream(name)), name);
    }

    private void executeConvert(SpritesheetPackingConverter converter) throws MojoExecutionException {
        converter.convert(imageList, createImagePacking(), mock);
    }

    private ImagePacking createImagePacking() {return new ImagePacking(new Dimension(width, height), positionMap);}
}