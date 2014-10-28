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
import java.io.InputStream;
import java.nio.file.Files;
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
     * Optional output Less file containing coordinates and dimensions, where each icon is saved as position and size mixins.
     */
    @Parameter
    File less;

    /**
     * Optional Less namespace name.
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

    /**
     * Optionally skip the execution of the plugin.
     */
    @Parameter(defaultValue = "false")
    Boolean skip;

    @Component
    BuildContext buildContext;

    /**
     * Execute the MOJO.
     *
     * @throws MojoExecutionException if something unexpected occurs.
     */
    public void execute() throws MojoExecutionException {

        if (skip) {
            log("Execution of spritepacker was skipped.");
            return;
        }

        long startTime = System.currentTimeMillis();

        List<Path> inputs = scanPaths(sourceDirectory, includes, excludes);

        // Check if there are actually any inputs to do anything with
        if (inputs.isEmpty()) {
            log("No source images found.");
            return;
        }

        Path outputPath = fileToPath(output);
        Path jsonPath = fileToPath(json);
        Path cssPath = fileToPath(css);
        Path lessPath = fileToPath(less);

        // Load output files into an ArrayList
        List<Path> outputs = Arrays.asList(outputPath, jsonPath, cssPath, lessPath);

        // If force overwrite not specified, and the JSON file is not being created for the first time,
        // and the output files were modified more recently than the input files, return.
        try {
            if (!forceOverwrite && !Utils.shouldWriteOutput(inputs, outputs)) {
                log("No source images modified.");
                return;
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not check if output should be written.", e);
        }

        log("Loading " + inputs.size() + " images from " + sourceDirectory.getAbsolutePath());

        // Load images defined in input array
        List<NamedImage> images = loadImages(inputs);

        log("Packing images...");

        // Add packing information
        ImagePacking imagePacking = packImages(images);

        List<PackingConverter> converters = Arrays.asList(new SpritesheetPackingConverter(outputPath),
                                                          new JsonPackingConverter(jsonPath, jsonpVar),
                                                          new CssPackingConverter(cssPath, cssPrefix),
                                                          new LessPackingConverter(lessPath, lessNamespace));

        for (PackingConverter converter : converters) {
            executeConverter(images, imagePacking, converter);
        }

        long took = System.currentTimeMillis() - startTime;
        log("Done - took " + took + "ms!");

    }

    // Allow tests to stub or verify the packing
    protected ImagePacking packImages(List<NamedImage> images) {
        return PackGrowing.fit(images, padding);
    }

    // Allow tests to stub or verify converter execution
    protected void executeConverter(List<NamedImage> images, ImagePacking imagePacking, PackingConverter converter) throws MojoExecutionException {
        converter.convert(images, imagePacking, getLog());
    }

    /**
     * Convert a file to a path, or return null if the file is null
     *
     * @param file the file to convert to a path
     * @return the path that was converted from a file
     */
    protected Path fileToPath(File file) {
        return (file == null) ? null : file.toPath();
    }

    /**
     * Create a list of files within a source directory, including subdirectories,
     * that match the includes and excludes criteria
     *
     * @param sourceDirectory the source directory
     * @param includes        criterion for files to include
     * @param excludes        criterion for files to exclude
     * @return list of matching files
     */
    protected List<Path> scanPaths(File sourceDirectory, String[] includes, String[] excludes) {
        Scanner scanner = buildContext.newScanner(sourceDirectory, true);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();
        String[] fileNames = scanner.getIncludedFiles();

        // sort files by path and name
        Arrays.sort(fileNames);

        List<Path> paths = new ArrayList<>(fileNames.length);
        for (String fileName : fileNames) {
            paths.add(sourceDirectory.toPath().resolve(fileName));
        }
        return paths;
    }

    /**
     * Load list of image files as a list of NamedImages
     *
     * @param imageFiles the image files to load
     * @return the list of loaded NamedImages
     * @throws MojoExecutionException when any input image cannot be opened
     */
    protected List<NamedImage> loadImages(List<Path> imageFiles) throws MojoExecutionException {
        // Do not cache image data in temporary files.
        ImageIO.setUseCache(false);

        List<NamedImage> images = new ArrayList<>(imageFiles.size());
        for (Path f : imageFiles) {
            try (InputStream inputStream = Files.newInputStream(f)) {
                String basename = FileUtils.removeExtension(f.getFileName().toString());
                images.add(new NamedImage(ImageIO.read(inputStream), basename));
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to read image from file: " + f.toAbsolutePath(), e);
            }
        }
        return images;
    }

    public void log(Object message) {
        getLog().info(message.toString());
    }

}
