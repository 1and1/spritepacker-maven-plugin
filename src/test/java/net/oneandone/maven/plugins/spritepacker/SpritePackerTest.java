package net.oneandone.maven.plugins.spritepacker;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.Scanner;
import org.junit.Test;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SpritePacker
 */
public class SpritePackerTest {
    @Test
    public void name() throws Exception {
        SpritePacker spritePacker = new SpritePacker();
        BuildContext buildContext = mock(BuildContext.class);
        spritePacker.buildContext = buildContext;
        Scanner scanner = mock(Scanner.class);
        when(buildContext.newScanner(any(File.class))).thenReturn(scanner);
        when(buildContext.newScanner(any(File.class), anyBoolean())).thenReturn(scanner);

        when(scanner.getIncludedFiles()).thenReturn(new String[] {});

        spritePacker.execute();
        // TODO PORTPHNX-6438 ssiegler 28.07.2014 verify assumptions
    }

    @Test
    public void loadNoImages() throws Exception {
        assertThat(new SpritePacker().loadImages(Collections.<Path>emptyList()), is(empty()));
    }

    @Test(expected = MojoExecutionException.class)
    public void loadImagesWrapsIOException() throws Exception {
        new SpritePacker().loadImages(Arrays.asList(Jimfs.newFileSystem(Configuration.unix()).getPath("tmp")));
    }
}