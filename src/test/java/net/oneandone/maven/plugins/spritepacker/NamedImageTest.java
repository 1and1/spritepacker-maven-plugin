package net.oneandone.maven.plugins.spritepacker;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the NamedImage data structure.
 */
public class NamedImageTest {

    @Test
    public void stringRepresentationContainsAllData() throws Exception {
        String string = new NamedImage(createImage(12, 23), "Named").toString();

        assertThat(string, allOf(containsString("12"),
                                 containsString("23"),
                                 containsString("Named")));
    }

    @Test(expected = NullPointerException.class)
    public void nullImage() throws Exception {
        new NamedImage(null, "but a name");
    }

    @Test(expected = NullPointerException.class)
    public void nullName() throws Exception {
        new NamedImage(createImage(2, 15), null);
    }

    @Test
    public void returnsImageHeight() throws Exception {
        assertThat(new NamedImage(createImage(42, 13), "an image").getWidth(), is(42));
    }

    @Test
    public void returnsImageWidth() throws Exception {
        assertThat(new NamedImage(createImage(4, 31), "another image").getHeight(), is(31));
    }

    @Test
    public void returnsName() throws Exception {
        String name = "uniquely named";
        assertThat(new NamedImage(createImage(3, 9), name).getName(), is(name));

    }

    @Test
    public void returnsSameImage() throws Exception {
        BufferedImage image = createImage(34, 87);
        assertThat(new NamedImage(image, "unnamed").getImage(), sameInstance(image));
    }

    protected BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
}