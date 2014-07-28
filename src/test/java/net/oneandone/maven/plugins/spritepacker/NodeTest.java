package net.oneandone.maven.plugins.spritepacker;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the node data structure.
 */
public class NodeTest {
    private static final int X = 14;
    private static final int Y = 44;
    private static final int WIDTH = 12;
    private static final int HEIGHT = 81;
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
    private Node node;

    @Before
    public void before() throws Exception {
        node = new Node(X, Y, WIDTH, HEIGHT);
    }

    @Test
    public void stringRepresentationContainsAllData() throws Exception {
        errorCollector.checkThat(node.toString(), allOf(containsString(String.valueOf(X)),
                                                        containsString(String.valueOf(Y)),
                                                        containsString(String.valueOf(WIDTH)),
                                                        containsString(String.valueOf(HEIGHT)),
                                                        containsString("false")));

        node.setUsed(true);

        errorCollector.checkThat(node.toString(), allOf(containsString(String.valueOf(X)),
                                                        containsString(String.valueOf(Y)),
                                                        containsString(String.valueOf(WIDTH)),
                                                        containsString(String.valueOf(HEIGHT)),
                                                        containsString("true")));
    }

    @Test
    public void initialValues() throws Exception {
        errorCollector.checkThat(node.getDown(), is(nullValue()));
        errorCollector.checkThat(node.getRight(), is(nullValue()));
        errorCollector.checkThat(node.isUsed(), is(false));
    }

    @Test
    public void canUpdateUsed() throws Exception {
        node.setUsed(true);

        assertThat(node.isUsed(), is(true));
    }

    @Test
    public void canUpdateDown() throws Exception {
        Node right = node.getRight();
        Node down = new Node(0, 0, 0, 0);
        node.setDown(down);

        assertThat(node.getDown(), is(sameInstance(down)));
        assertThat(node.getRight(), is(sameInstance(right)));
    }

    @Test
    public void canUpdateRight() throws Exception {
        Node down = node.getDown();
        Node right = new Node(0, 0, 0, 0);
        node.setRight(right);

        assertThat(node.getRight(), is(sameInstance(right)));
        assertThat(node.getDown(), is(sameInstance(down)));
    }

    @Test
    public void returnsHeight() throws Exception {
        assertThat(node.getHeight(), is(HEIGHT));
    }

    @Test
    public void returnsWidth() throws Exception {
        assertThat(node.getWidth(), is(WIDTH));
    }

    @Test
    public void returnsY() throws Exception {
        assertThat(node.getY(), is(Y));
    }

    @Test
    public void returnsX() throws Exception {
        assertThat(node.getX(), is(X));
    }
}