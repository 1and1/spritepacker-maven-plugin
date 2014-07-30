package net.oneandone.maven.plugins.spritepacker;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.oneandone.maven.plugins.spritepacker.converters.PackingConverter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.Scanner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.oneandone.maven.plugins.spritepacker.matchers.ImageMatcher.eqImage;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SpritePacker
 */
public class SpritePackerTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void scanPathsResolvesScannerResultsFromSourceDirectory() throws Exception {
        SpritePacker spritePacker = new SpritePacker();
        BuildContext buildContext = mock(BuildContext.class);
        spritePacker.buildContext = buildContext;
        Scanner scanner = mock(Scanner.class);
        when(buildContext.newScanner(any(File.class))).thenReturn(scanner);
        when(buildContext.newScanner(any(File.class), anyBoolean())).thenReturn(scanner);

        String[] fileNames = { "first.png",
                               "red" + File.separator + "file.png",
                               "mixed" + File.separator + "test" + File.separator + ".dot" };
        when(scanner.getIncludedFiles()).thenReturn(fileNames);
        File sourceDirectory = mock(File.class);
        Path sourcePath = mock(Path.class);
        List<Path> resultPaths = Arrays.asList(mock(Path.class), mock(Path.class), mock(Path.class));
        when(sourcePath.resolve(fileNames[0])).thenReturn(resultPaths.get(0));
        when(sourcePath.resolve(fileNames[1])).thenReturn(resultPaths.get(1));
        when(sourcePath.resolve(fileNames[2])).thenReturn(resultPaths.get(2));
        when(sourceDirectory.toPath()).thenReturn(sourcePath);
        String[] excludes = new String[] { "1234", "**/test/*", ".*" };
        String[] includes = new String[] { "**/*.png", "*.jpg", "inc/**" };

        assertThat(spritePacker.scanPaths(sourceDirectory, includes, excludes), is(resultPaths));
        verify(scanner).setIncludes(eq(includes));
        verify(scanner).setExcludes(eq(excludes));
    }

    @Test
    public void logOnInfo() throws Exception {
        SpritePacker spritePacker = spy(new SpritePacker());
        Log log = mock(Log.class);
        doReturn(log).when(spritePacker).getLog();
        Object message = new Object();
        spritePacker.log(message);
        verify(log).info(message.toString());
    }

    @Test
    public void loadNoImages() throws Exception {
        assertThat(new SpritePacker().loadImages(Collections.<Path>emptyList()), is(empty()));
    }

    @Test(expected = MojoExecutionException.class)
    public void loadImagesWrapsIOException() throws Exception {
        new SpritePacker().loadImages(Arrays.asList(Jimfs.newFileSystem(Configuration.unix()).getPath("tmp")));
    }

    @Test
    public void loadSingleImage() throws Exception {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        String resourceName = "/100px-Icon_subway.svg.png";
        Path path = fileSystem.getPath(resourceName);
        Files.copy(getClass().getResourceAsStream(resourceName), path);

        List<NamedImage> images = new SpritePacker().loadImages(Arrays.asList(path));
        errorCollector.checkThat(images, hasSize(1));
        NamedImage namedImage = images.get(0);
        errorCollector.checkThat(namedImage.getWidth(), is(100));
        errorCollector.checkThat(namedImage.getHeight(), is(78));
        errorCollector.checkThat(namedImage.getName(), is(resourceName.substring(1, resourceName.length() - 4)));
        errorCollector.checkThat(namedImage.getImage(), is(eqImage(ImageIO.read(getClass().getResourceAsStream(resourceName)))));
    }

    @Test
    public void executeWithNoInputsDoesNothing() throws Exception {
        SpritePacker spritePacker = spy(new SpritePacker());
        doReturn(Collections.<Path>emptyList()).when(spritePacker).scanPaths(any(File.class), any(String[].class), any(String[].class));

        spritePacker.execute();
        verify(spritePacker, never()).loadImages(anyListOf(Path.class));
        verify(spritePacker, never()).executeConverter(anyListOf(NamedImage.class), any(ImagePacking.class), any(PackingConverter.class));
    }

    @Test
    public void executeWithNonExistingInput() throws Exception {
        SpritePacker spritePacker = spy(new SpritePacker());
        spritePacker.forceOverwrite = Boolean.FALSE;
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path input = fileSystem.getPath("input");
        doReturn(Arrays.asList(input)).when(spritePacker).scanPaths(any(File.class), any(String[].class), any(String[].class));

        spritePacker.execute();
        verify(spritePacker, never()).loadImages(anyListOf(Path.class));
        verify(spritePacker, never()).executeConverter(anyListOf(NamedImage.class), any(ImagePacking.class), any(PackingConverter.class));
    }

    @Test
    public void executeWithNonExistingInputButForceOverwrite() throws Exception {
        SpritePacker spritePacker = spy(new SpritePacker());
        spritePacker.forceOverwrite = Boolean.TRUE;
        spritePacker.sourceDirectory = mock(File.class);
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        List<Path> inputs = Arrays.asList(fileSystem.getPath("input"));
        doReturn(inputs).when(spritePacker).scanPaths(any(File.class), any(String[].class), any(String[].class));

        try {
            spritePacker.execute();
            fail("Expected exception not thrown");
        } catch (MojoExecutionException e) {
            verify(spritePacker).loadImages(inputs);
        }
    }
}