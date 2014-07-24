package net.oneandone.maven.plugins.spritepacker;

import net.oneandone.maven.plugins.spritepacker.converters.CssPackingConverter;
import net.oneandone.maven.plugins.spritepacker.converters.JsonPackingConverter;
import net.oneandone.maven.plugins.spritepacker.converters.LessPackingConverter;
import net.oneandone.maven.plugins.spritepacker.converters.PackingConverter;
import net.oneandone.maven.plugins.spritepacker.converters.SpritesheetPackingConverter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Packs spritesheets from supplied images.
 *
 * @author Robert Murphy, mklein, ssiegler
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SpritePacker extends AbstractMojo {

    /**
     * Output spritesheet image file
     */
    @Parameter(required = true)
    File output;

    /**
     * Optional output JSON(P) description file containing coordinates and dimensions
     */
    @Parameter
    File json;

    /**
     * Optional variable for JSONP files
     * e.g.
     * { image: {...} }
     * becomes
     * jsonpVar = { image: {...} }
     */
    @Parameter
    String jsonpVar;

    /**
     * Optional output CSS file containing coordinates and dimensions, where each icon is saved as its own class.
     */
    @Parameter
    File css;

    /**
     * Optional CSS class prefix. Default value is "icon".
     */
    @Parameter
    String cssPrefix;

    /**
     * Optional output LESS file containing coordinates and dimensions, where each icon is saved as position and size mixins.
     */
    @Parameter
    File less;

    /**
     * Optional LESS namespace name.
     */
    @Parameter
    String lessNamespace;

    /**
     * The source directory containing the icons
     */
    @Parameter(required = true)
    File sourceDirectory;

    /**
     * List of files to include. Specified as fileset patterns which are relative to the source directory. Default is all files.
     */
    @Parameter
    String[] includes = new String[] { "**/*" };

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
     */
    @Parameter
    String[] excludes = new String[] { };

    /**
     * Optional transparent padding added between images in spritesheet.
     */
    @Parameter(defaultValue = "0")
    Integer padding;

    /**
     * Optionally force the sprite packer to always re-generate files regardless of whether new graphics were found.
     */
    @Parameter(defaultValue = "false")
    Boolean forceOverwrite;

    @Component
    BuildContext buildContext;

    /**
     * Execute the MOJO.
     *
     * @throws MojoExecutionException if something unexpected occurs.
     */
    public void execute() throws MojoExecutionException {

        long startTime = System.currentTimeMillis();

        List<File> inputs = scanPaths(sourceDirectory, includes, excludes);

        // Check if there are actually any inputs to do anything with
        if (inputs.size() == 0) {
            log("No source images found.");
            return;
        }

        // Load output files into an ArrayList
        List<File> outputs = Arrays.asList(output, json, css, less);

        // If force overwrite not specified, and the JSON file is not being created for the first time,
        // and the output files were modified more recently than the input files, return.
        if (!(forceOverwrite || isAnyInputNewerThanAnyOutput(inputs, outputs))) {
            log("No source images modified.");
            return;
        }

        log("Loading " + inputs.size() + " images from " + sourceDirectory.getAbsolutePath());

        // Load images defined in input array
        List<NamedImage> images = loadImages(inputs);

        log("Packing images...");

        // Add packing information
        ImagePacking imagePacking = PackGrowing.fit(images, padding);

        Path outputPath = fileToPath(output);
        Path jsonPath = fileToPath(json);
        Path cssPath = fileToPath(css);
        Path lessPath = fileToPath(less);

        List<PackingConverter> consumers = Arrays.asList(new SpritesheetPackingConverter(outputPath),
                                                         new JsonPackingConverter(jsonPath, jsonpVar),
                                                         new CssPackingConverter(cssPath, cssPrefix),
                                                         new LessPackingConverter(lessPath, lessNamespace));

        for (PackingConverter consumer : consumers) {
            consumer.convert(images, imagePacking, getLog());
        }

        long took = System.currentTimeMillis() - startTime;
        log("Done - took " + took + "ms!");

    }

    /**
     * Convert a file to a path, or return null if the file is null
     *
     * @param file  the file to convert to a path
     * @return      the path that was converted from a file
     */
    private Path fileToPath(File file) {
        return (file == null) ? null : file.toPath();
    }

    /**
     * Check if any input is newer than any output. This also takes output files into account that do not yet exist,
     * as they have a lastModified time of 0L and are thus automatically "older" than any input files.
     *
     * @param inputs    the list of input files
     * @param outputs   the list of output files
     * @return          whether any input file was newer than any output file
     */
    private boolean isAnyInputNewerThanAnyOutput(List<File> inputs, List<File> outputs) {
        assert inputs != null && !inputs.isEmpty();
        assert outputs != null && !outputs.isEmpty();

        long newestInput = 0L;
        for (File input : inputs) {
            if (input != null) {
                newestInput = Math.max(input.lastModified(), newestInput);
            }
        }

        long oldestOutput = newestInput;
        for (File output : outputs) {
            if (output != null) {
                oldestOutput = Math.min(output.lastModified(), oldestOutput);
            }
        }

        return newestInput >= oldestOutput;
    }

    /**
     * Create a list of files within a source directory, including subdirectories,
     * that match the includes and excludes criteria
     *
     * @param sourceDirectory the source directory
     * @param includes        criterion for files to include
     * @param excludes        criterion for files to exclude
     * @return                list of matching files
     */
    private List<File> scanPaths(File sourceDirectory, String[] includes, String[] excludes) {
        Scanner scanner = buildContext.newScanner(sourceDirectory, true);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();
        String[] fileNames = scanner.getIncludedFiles();
        List<File> paths = new ArrayList<>(fileNames.length);
        for (String fileName : fileNames) {
            paths.add(new File(sourceDirectory, fileName));
        }
        return paths;
    }

    /**
     * Load list of image files as a list of NamedImages
     *
     * @param imageFiles the image files to load
     * @return           the list of loaded NamedImages
     * @throws MojoExecutionException
     */
    private List<NamedImage> loadImages(List<File> imageFiles) throws MojoExecutionException {
        List<NamedImage> images = new ArrayList<>(imageFiles.size());
        for (File f : imageFiles) {
            try {
                String basename = FileUtils.removeExtension(f.getName());
                images.add(new NamedImage(ImageIO.read(f), basename));
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to open file: " + f.getAbsolutePath(), e);
            }
        }
        return images;
    }

    public void log(Object message) {
        getLog().info(message.toString());
    }

}
