package net.oneandone.maven.plugins.spritepacker;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;

/**
 * Unit tests for the utility methods provided by Utils.
 */
public class UtilsTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
    private Path inputDir;
    private Path outputDir;

    @Before
    public void before() throws Exception {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        inputDir = fileSystem.getPath("/inputs");
        outputDir = fileSystem.getPath("/outputs");
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);
    }

    @Test(expected = NullPointerException.class)
    public void shouldWriteOutputWithNullInputs() throws Exception {
        Utils.shouldWriteOutput(null, Collections.<Path>emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void shouldWriteOutputWithNullOutputs() throws Exception {
        Utils.shouldWriteOutput(Collections.<Path>emptyList(), null);
    }

    @Test
    public void noOutputWrittenWhenNoInputFileExists() throws Exception {
        errorCollector.checkThat(Utils.shouldWriteOutput(Arrays.asList(inputDir.resolve("doesNotExist")), Arrays.asList(outputDir.resolve(""))), is(false));
    }

    @Test
    public void noOutputWrittenWhenNoInputFilesSpecified() throws Exception {
        errorCollector.checkThat(Utils.shouldWriteOutput(Arrays.<Path>asList(null, null), Arrays.asList(outputDir.resolve(""))), is(false));
        errorCollector.checkThat(Utils.shouldWriteOutput(Collections.<Path>emptyList(), Arrays.asList(outputDir.resolve(""))), is(false));
    }

    @Test
    public void noOutputWrittenWhenNoOutputFilesSpecified() throws Exception {
        errorCollector.checkThat(Utils.shouldWriteOutput(Arrays.asList(inputDir.resolve("doesNotExist")), Arrays.<Path>asList(null, null)), is(false));
        errorCollector.checkThat(Utils.shouldWriteOutput(Arrays.asList(inputDir.resolve("doesNotExist")), Collections.<Path>emptyList()), is(false));
    }

    @Test
    public void outputWrittenWhenAnyOutputFileDoesNotExist() throws Exception {
        Path exists = inputDir.resolve("exists");
        Files.createFile(exists);
        errorCollector.checkThat(Utils.shouldWriteOutput(Arrays.asList(exists), Arrays.asList(outputDir.resolve("doesNotExist"))), is(true));
    }

    @Test
    public void outputWrittenWhenAnyInputIsNewerThanAnyOutput() throws Exception {
        List<Path> inputs = Arrays.asList(inputDir.resolve("first"), inputDir.resolve("second"), inputDir.resolve("last"));
        List<Path> outputs = Arrays.asList(outputDir.resolve("first"), outputDir.resolve("second"), outputDir.resolve("last"));
        for (Path input : inputs) {
            Files.createFile(input);
            Files.setLastModifiedTime(input, FileTime.from(1000, TimeUnit.DAYS));
        }
        for (Path output : outputs) {
            Files.createFile(output);
            Files.setLastModifiedTime(output, FileTime.from(2000, TimeUnit.DAYS));
        }
        Files.setLastModifiedTime(inputs.get(0), FileTime.from(1500, TimeUnit.DAYS));
        Files.setLastModifiedTime(outputs.get(1), FileTime.from(1200, TimeUnit.DAYS));
        errorCollector.checkThat(Utils.shouldWriteOutput(inputs, outputs), is(true));
    }

    @Test
    public void outputWrittenWhenNewestInputIsAsOldAsOldestOutput() throws Exception {
        List<Path> inputs = Arrays.asList(inputDir.resolve("first"), inputDir.resolve("second"), inputDir.resolve("last"));
        List<Path> outputs = Arrays.asList(outputDir.resolve("first"), outputDir.resolve("second"), outputDir.resolve("last"));
        for (Path input : inputs) {
            Files.createFile(input);
            Files.setLastModifiedTime(input, FileTime.from(1000, TimeUnit.DAYS));
        }
        for (Path output : outputs) {
            Files.createFile(output);
            Files.setLastModifiedTime(output, FileTime.from(2000, TimeUnit.DAYS));
        }
        Files.setLastModifiedTime(inputs.get(0), FileTime.from(1500, TimeUnit.DAYS));
        Files.setLastModifiedTime(outputs.get(1), FileTime.from(1500, TimeUnit.DAYS));
        errorCollector.checkThat(Utils.shouldWriteOutput(inputs, outputs), is(true));
    }

    @Test
    public void noOutputWrittenWhenAllOutputsAreNewerThanAllInputs() throws Exception {
        List<Path> inputs = Arrays.asList(inputDir.resolve("first"), inputDir.resolve("second"), inputDir.resolve("last"));
        List<Path> outputs = Arrays.asList(outputDir.resolve("first"), outputDir.resolve("second"), outputDir.resolve("last"));
        for (Path input : inputs) {
            Files.createFile(input);
            Files.setLastModifiedTime(input, FileTime.from(7, TimeUnit.DAYS));
        }
        for (Path output : outputs) {
            Files.createFile(output);
            Files.setLastModifiedTime(output, FileTime.from(12, TimeUnit.DAYS));
        }
        errorCollector.checkThat(Utils.shouldWriteOutput(inputs, outputs), is(false));
    }
}
