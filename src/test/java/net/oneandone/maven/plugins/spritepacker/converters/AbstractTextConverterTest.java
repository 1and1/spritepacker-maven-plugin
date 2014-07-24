package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.List;

import static org.hamcrest.Matchers.is;

/**
 * Unit tests for the AbstractTextConverter
 */
public class AbstractTextConverterTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
    private AbstractTextConverter abstractTextConverter;

    @Before
    public void before() throws Exception {
        abstractTextConverter = new AbstractTextConverter(null, null) {

            @Override
            protected String createOutput(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException {
                return null;  // TODO
            }
        };

    }

    @Test
    public void testIntToPixel() throws Exception {
        errorCollector.checkThat(abstractTextConverter.intToPixel(0), is("0"));
        errorCollector.checkThat(abstractTextConverter.intToPixel(2), is("2px"));
        errorCollector.checkThat(abstractTextConverter.intToPixel(-32), is("-32px"));
    }
}