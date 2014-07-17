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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Output spritesheet image name
     */
    @Parameter(required = true)
    private File output;

    /**
     * Output json(p) description file containing coordinates and dimensions.
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

        long startTime = System.currentTimeMillis();

        // Load input files into an ArrayList
        List<File> inputs = toFileList(sourceDirectory, includes, excludes);

        // Check if there are actually any inputs to do anything with
        if (inputs.size() == 0) {
            log("No source images found.");
            return;
        }

        // Load output files into an ArrayList
        List<File> outputs = Arrays.asList(output, json);

        // If force overwrite not specified, and the JSON file is not being created for the first time,
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

        log("Packing images...");

        // Add packing information
        Dimension dim = packImages(images, padding);

        log("Saving spritesheet...");

        // Put to a spritesheet and write to a file
        saveSpritesheet(images, dim, output);

        if (json != null) {

            log("Saving JSON...");

            // Write json(p) data with image coords and dimensions to a file
            saveJSON(images, json, jsonpVar);

        }

        float took = (System.currentTimeMillis() - startTime) / 1000;
        log("Done - took " + took + "s!");

    }

    /**
     * Get the most recent modified date from a list of files
     *
     * @param files     the list of files
     * @return          the most recent modified date
     */
    private long getLastModified(List<File> files) {
        long modified = 0;
        for (File f : files) {
            if (f != null) {
                modified = Math.max(modified, f.lastModified());
            }
        }
        return modified;
    }

    /**
     * Create a list of files within a source directory, including subdirectories,
     * that match the includes and excludes criteria
     *
     * @param sourceDirectory   the source directory
     * @param includes          criterion for files to include
     * @param excludes          criterion for files to exclude
     * @return                  list of matching files
     */
    private List<File> toFileList(File sourceDirectory, String[] includes, String[] excludes) {
        Scanner scanner = buildContext.newScanner(sourceDirectory, true);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        List<File> fileArray = new ArrayList<File>(files.length);
        for (String f : files) {
            fileArray.add(new File(sourceDirectory, f));
        }
        return fileArray;
    }

    /**
     * Load list of image files as a list of ImageNodes
     *
     * @param imageFiles    the image files to load
     * @return              the list of loaded ImageNodes
     * @throws MojoExecutionException
     */
    private List<ImageNode> loadImages(List<File> imageFiles) throws MojoExecutionException {
        List<ImageNode> images = new ArrayList<ImageNode>(imageFiles.size());
        for (File f : imageFiles) {
            try {
                images.add(new ImageNode(f, ImageIO.read(f)));
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to open file: " + f.getAbsolutePath(), e);
            }
        }
        return images;
    }

    /**
     * Pack a list of images into a spritesheet, adding Nodes to each ImageNode with the coordinates of the image
     * in the spritesheet
     *
     * @param images    the list of images to pack
     * @param padding   the padding that should be added between images in the spritesheet
     * @return          the root node of the spritesheet
     */
    private Dimension packImages(List<ImageNode> images, Integer padding) {
        PackGrowing packGrowing = new PackGrowing();
        return packGrowing.fit(images, padding);
    }

    /**
     * Save list of packed images
     *
     * @param images    list of packed images to save
     * @param root      the root node, the dimensions of which determine the spritesheet size
     * @param output    the output PNG file to write to
     * @throws MojoExecutionException
     */
    private void saveSpritesheet(List<ImageNode> images, Dimension dim, File output) throws MojoExecutionException {
        if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Couldn't create target directory: " + output.getParentFile());
        }


        BufferedImage spritesheet = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = spritesheet.createGraphics();
        for (ImageNode imageNode : images) {
            if (imageNode.getNode() == null) {
                throw new MojoExecutionException("Cannot save images because they have not yet been packed.");
            }

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

    /**
     * Save the list of images as coordinate and dimension data in a JSON file
     *
     * @param images        the list of images
     * @param json          the output JSON file to write to
     * @param jsonpVariable optional JSON variable name
     * @throws MojoExecutionException
     */
    private void saveJSON(List<ImageNode> images, File json, String jsonpVariable) throws MojoExecutionException {
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

            map.put(fileNameWithoutExtension(n.getFile()), props);
        }

        // Generate json representation of map object
        String out = "";
        try {
            out = mapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't generate JSON data", e);
        }

        // If user has passed in a variable to wrap this in, append it to the front
        if (jsonpVariable != null) {
            out = jsonpVariable + " = " + out;
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

    /**
     * Get the file name without the file extension
     *
     * @param file  the file to get the name of
     * @return      the file name without an extension
     */
    private String fileNameWithoutExtension(File file) {
        String fileName = file.getName();
        String[] parts = fileName.split("\\.(?=[^\\.]+$)");
        if (parts.length > 0) {
            return parts[0];
        } else {
            return fileName;
        }
    }

    public void log(Object message) {
        getLog().info(message.toString());
    }

}
