package com.murphybob.spritepacker.converters;

import com.murphybob.spritepacker.ImagePacking;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * TODO mklein: document class purpose
 * <p/>
 *
 * @author mklein
 */
public abstract class AbstractTextConverter implements PackingConverter {
    private final File file;
    private final String name;

    protected AbstractTextConverter(File file, String name) {
        this.file = file;
        this.name = name;
    }

    @Override
    public void convert(ImagePacking imagePacking, Log log) throws MojoExecutionException {
        if (file == null) {
            log.info("No " + name + " output file specified.");
            return;
        }

        log.info("Generating " + name + " output...");

        String output = createOutput(imagePacking, log);

        try {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file.toPath(), Charset.forName("UTF-8"))) {
                log.info("Saving " + name + "...");
                bufferedWriter.write(output);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write to file '"+file.getAbsolutePath()+"'.", e);
        }

    }

    /**
     *
     * @param imagePacking
     * @param log
     * @return  output of the converter; null if the
     */
    protected abstract String createOutput(ImagePacking imagePacking, Log log) throws MojoExecutionException;

    protected String intToPixel(int i) {
        return i == 0 ? "0" : i + "px";
    }
}
