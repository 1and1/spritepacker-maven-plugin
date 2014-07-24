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

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the AbstractTextConverter
 *
 * @autor ssiegler
 */
public class AbstractTextConverterTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private Path file;
    private Log log;
    private String output;

    @Before
    public void before() throws Exception {
        log = mock(Log.class);

        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        file = fileSystem.getPath("/output");

        output = "These are not the things you are looking for.\nLook, over there, a three headed monkey!";
    }

    @Test
    public void testIntToPixel() throws Exception {
        AbstractTextConverter abstractTextConverter = createConverter();

        errorCollector.checkThat(abstractTextConverter.intToPixel(0), is("0"));
        errorCollector.checkThat(abstractTextConverter.intToPixel(2), is("2px"));
        errorCollector.checkThat(abstractTextConverter.intToPixel(-32), is("-32px"));
    }

    @Test
    public void testSanitize() throws Exception {
        // TODO
    }

    @Test
    public void testFixFirstChar() throws Exception {
        // TODO
    }

    @Test
    public void doNothingOnNullFile() throws Exception {
        file = null;

        AbstractTextConverter converter = convert();

        verify(converter, never()).createOutput(anyListOf(NamedImage.class), any(ImagePacking.class), any(Log.class));
    }

    @Test(expected = MojoExecutionException.class)
    public void throwsExceptionWhenFileIsNotWritable() throws Exception {
        // Prevent file from being written to by creating a directory in its place.
        Files.createDirectory(file);

        convert();
    }

    @Test
    public void convertWritesOutputToFile() throws Exception {
        convert();

        errorCollector.checkThat(Files.readAllLines(file, StandardCharsets.UTF_8), is(Arrays.asList(output.split("\\n"))));
    }

    private AbstractTextConverter createConverter() {
        return spy(new AbstractTextConverter(file, null) {
            @Override
            protected String createOutput(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException {
                return output;
            }
        });
    }

    private AbstractTextConverter convert() throws MojoExecutionException {
        AbstractTextConverter converter = createConverter();
        converter.convert(null, null, log);
        return converter;
    }
}