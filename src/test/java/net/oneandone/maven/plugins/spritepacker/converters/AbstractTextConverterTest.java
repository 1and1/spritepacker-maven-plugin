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
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the AbstractTextConverter
 */
public class AbstractTextConverterTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private String output;
    private MojoExecutionException exception;
    private Path file;
    private String type;
    private Log log;

    @Before
    public void before() throws Exception {
        log = mock(Log.class);

        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        file = fileSystem.getPath("/output");

        output = "This are not the things you are looking for.\nLook, over there, a three headed monkey!";
    }

    @Test
    public void testIntToPixel() throws Exception {
        AbstractTextConverter abstractTextConverter = createConverter();

        errorCollector.checkThat(abstractTextConverter.intToPixel(0), is("0"));
        errorCollector.checkThat(abstractTextConverter.intToPixel(2), is("2px"));
        errorCollector.checkThat(abstractTextConverter.intToPixel(-32), is("-32px"));
    }

    @Test
    public void doNothingOnNullFile() throws Exception {
        file = null;
        exception = new MojoExecutionException("This should not be thrown");

        convert();
    }

    @Test(expected = MojoExecutionException.class)
    public void throwsExceptionWhenFileIsNotWritable() throws Exception {
        Files.createDirectory(file);

        convert();
    }

    @Test
    public void convertWritesOutputToFile() throws Exception {
        convert();

        errorCollector.checkThat(Files.readAllLines(file, StandardCharsets.UTF_8), is(Arrays.asList(output.split("\\n"))));
    }

    private AbstractTextConverter createConverter() {
        return new AbstractTextConverter(file, type) {
            @Override
            protected String createOutput(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException {
                if (exception == null) {
                    return output;
                } else {
                    throw exception;
                }
            }
        };
    }

    private void convert() throws MojoExecutionException {
        createConverter().convert(null, null, log);
    }
}