package com.murphybob.spritepacker.converters;

import com.murphybob.spritepacker.ImagePacking;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Interface for converting an ImagePacking.
 *
 * @author mklein
 */
public interface PackingConverter {

    /**
     * Convert the specified ImagePacking.
     *
     * @param imagePacking  the ImagePacking to convert
     * @param log           the log object to use
     * @throws MojoExecutionException
     */
    void convert(ImagePacking imagePacking, Log log) throws MojoExecutionException;
}
