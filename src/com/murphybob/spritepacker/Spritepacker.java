package com.murphybob.spritepacker;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Packs spritesheets from supplied images.
 *
 * @author Robert Murphy
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SpritePacker extends AbstractMojo {

    private static long startTime = System.currentTimeMillis();

    /**
     * Output spritesheet image name
     */
    @Parameter(required = true)
    private File output;

    /**
     * Output json(p) description file containing coords and dimensions.
     */
    @Parameter
    private File json;

    /**
     * Optional padding variable for jsonp files
     * e.g.
     * { image: {...} }
     * becomes
     * jsonpVar = { image: {...} }
     */
    @Parameter
    private String jsonpVar;

    /**
     * The source directory containing the LESS sources.
     */
    @Parameter(required = true)
    private File sourceDirectory;

    /**
     * List of files to include. Specified as fileset patterns which are relative to the source directory. Default is all files.
     */
    @Parameter
    private String[] includes = new String[] { "**/*" };

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
     */
    @Parameter
    private String[] excludes = new String[] { };

    /**
     * Optional transparent padding added between images in spritesheet.
     */
    @Parameter(defaultValue = "0")
    private Integer padding;

    /**
     * Optionally force the sprite packer to always re-generate files regardless of whether new graphics were found.
     */
    @Parameter(defaultValue = "false")
    private Boolean forceOverwrite;

    @Component
    private BuildContext buildContext;

    /**
     * Execute the MOJO.
     *
     * @throws MojoExecutionException if something unexpected occurs.
     */
    public void execute() throws MojoExecutionException {

        // Load input files into an ArrayList
        List<File> inputs = toFileArray(sourceDirectory, includes, excludes);

        // Check if there are actually any inputs to do anything with
        if (inputs.size() == 0) {
            log("No source images found.");
            return;
        }

        // Load output files into an ArrayList
        List<File> outputs = Arrays.asList(output, json);

        // If force overwrite not specified, the JSON file is not being created for the first time,
        // and the output files were modified more recently than the input files, return.
        if (forceOverwrite == false &&
            (json == null || json.exists()) &&
            getLastModified(inputs) < getLastModified(outputs)) {

            log("No source images modified.");
            return;
        }

        log("Loading " + inputs.size() + " images...");

        // Load images defined in input array
        List<ImageNode> images = loadImages(inputs);

        log("Sorting images...");

        // Sort images to be largest (by max(width,height)) first
        sortImages(images);

        log("Packing images...");

        // Add packing information
        Node root = packImages(images, padding);

        log("Saving spritesheet...");

        // Put to a spritesheet and write to a file
        saveSpritesheet(images, root, output);

        if (json != null) {

            log("Saving JSON...");

            // Write json(p) data with image coords and dimensions to a file
            saveJSON(images, json, jsonpVar);

        }

        float took = secondsSinceStart();
        log("Done - took " + took + "s!");

    }

    private long getLastModified(List<File> files) {
        long modified = 0;
        for (File f : files) {
            if (f != null) {
                modified = Math.max(modified, f.lastModified());
            }
        }
        return modified;
    }

    private List<File> toFileArray(File sourceDirectory, String[] includes, String[] excludes) {
        Scanner scanner = buildContext.newScanner(sourceDirectory, true);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        List<File> fileArray = new ArrayList<File>();
        for (String f : files) {
            fileArray.add(new File(sourceDirectory, f));
        }
        return fileArray;
    }

    private List<ImageNode> loadImages(List<File> imageFiles) throws MojoExecutionException {
        List<ImageNode> images = new ArrayList<ImageNode>();
        for (File f : imageFiles) {
            images.add(new ImageNode(f));
        }
        return images;
    }

    private void sortImages(List<ImageNode> images) {
        // Sort by max width / height descending
        Collections.sort(images, new Comparator<ImageNode>() {
            @Override
            public int compare(ImageNode arg0, ImageNode arg1) {
                int max0 = Math.max(arg0.getWidth(), arg0.getHeight());
                int max1 = Math.max(arg1.getWidth(), arg1.getHeight());
                return max1 - max0;
            }
        });
    }

    private Node packImages(List<ImageNode> images, Integer padding) {
        PackGrowing packGrowing = new PackGrowing(padding);
        return packGrowing.fit(images);
    }

    private void saveSpritesheet(List<ImageNode> images, Node root, File output) throws MojoExecutionException {
        if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Couldn't create target directory: " + output.getParentFile());
        }

        BufferedImage spritesheet = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = spritesheet.createGraphics();
        for (ImageNode imageNode : images) {
            int x = imageNode.getNode().getX(),
                y = imageNode.getNode().getY(),
                width = imageNode.getWidth(),
                height = imageNode.getHeight();
            gfx.drawImage(imageNode.getImage(), x, y, x + width, y + height, 0, 0, width, height, null);
        }

        try {
            ImageIO.write(spritesheet, "png", output);
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't write spritesheet: " + output, e);
        }
    }

    private void saveJSON(List<ImageNode> images, File json, String jsonp_variable) throws MojoExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.INDENT_OUTPUT, true);

        Map<String, Object> map = new HashMap<String, Object>();
        for (ImageNode n : images) {

            Map<String, Object> props = new HashMap<String, Object>();
            int x = n.getNode().getX(),
                y = n.getNode().getY(),
                width = n.getWidth(),
                height = n.getHeight();

            String xStr = x == 0 ? "0" : "-" + x + "px",
                   yStr = y == 0 ? "0" : "-" + y + "px";

            props.put("x", xStr);
            props.put("y", yStr);
            props.put("w", "" + width + "px");
            props.put("h", "" + height + "px");
            props.put("xy", xStr + " " + yStr);

            Map<String, Integer> numbers = new HashMap<String, Integer>();
            numbers.put("x", x);
            numbers.put("y", y);
            numbers.put("w", width);
            numbers.put("h", height);

            props.put("n", numbers);

            map.put(fnWithoutExtension(n.getFile()), props);
        }

        // Generate json representation of map object
        String out = "";
        try {
            out = mapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't generate JSON data", e);
        }

        // If user has passed in a variable to wrap this in, append it to the front
        if (jsonp_variable != null) {
            out = jsonp_variable + " = " + out;
        }

        // Write it to the designated output file
        try {
            FileWriter fw = new FileWriter(json);
            fw.write(out);
            fw.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't write JSON: " + json, e);
        }
    }

    private static String fnWithoutExtension(File file) {
        String fn = file.getName();
        String[] parts = fn.split("\\.(?=[^\\.]+$)");
        if (parts.length > 0) {
            return parts[0];
        } else {
            return fn;
        }
    }

    private float secondsSinceStart() {
        float tookM = (System.currentTimeMillis() - startTime);
        return tookM / 1000;
    }

    public void log(Object message) {
        getLog().info(message.toString());
    }

}
