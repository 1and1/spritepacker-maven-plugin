package net.oneandone.maven.plugins.spritepacker.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.awt.image.BufferedImage;

/**
 * Matcher to check if two images are the same.
 * <p/>
 *
 * @author ssiegler
 */
public class ImageMatcher extends TypeSafeDiagnosingMatcher<BufferedImage> {
    private final BufferedImage expected;

    /**
     * Creates a matcher against the given image
     *
     * @param expected the image to check against
     */
    public ImageMatcher(BufferedImage expected) {
        this.expected = expected;
    }

    public static Matcher<BufferedImage> eqImage(BufferedImage expected) {
        return new ImageMatcher(expected);
    }

    @Override
    protected boolean matchesSafely(BufferedImage actual, Description mismatchDescription) {
        int expectedWidth = expected.getWidth();
        int expectedHeight = expected.getHeight();
        int actualWidth = actual.getWidth();
        int actualHeight = actual.getHeight();
        if (expectedWidth != actualWidth || expectedHeight != actualHeight) {
            mismatchDescription.appendText(" has width ").appendValue(actualWidth).appendText("  and height ").appendValue(actualHeight);
            return false;
        }

        int differences = 0;
        for (int i = 0; i < expectedWidth; i++) {
            for (int j = 0; j < expectedHeight; j++) {
                if (expected.getRGB(i, j) != actual.getRGB(i, j)) {
                    differences++;
                }
            }
        }

        mismatchDescription.appendText("differs in ").appendValue(differences).appendText(" pixels");
        return differences == 0;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(" an image with width ").appendValue(expected.getWidth()).appendText(" and height ").appendValue(expected.getHeight());
    }
}
