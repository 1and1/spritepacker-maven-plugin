package net.oneandone.maven.plugins.spritepacker;

import org.codehaus.plexus.util.Scanner;
import org.junit.Test;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SpritePacker
 */
public class SpritePackerTest {
    @Test
    // TODO PORTPHNX-6438 ssiegler 22.07.2014 write tests
    public void name() throws Exception {
        SpritePacker spritePacker = new SpritePacker();
        BuildContext buildContext = mock(BuildContext.class);
        spritePacker.buildContext = buildContext;
        Scanner scanner = mock(Scanner.class);
        when(buildContext.newScanner(any(File.class))).thenReturn(scanner);
        when(buildContext.newScanner(any(File.class), anyBoolean())).thenReturn(scanner);

        when(scanner.getIncludedFiles()).thenReturn(new String[] {});

        spritePacker.execute();
    }
}