package com.murphybob.spritepacker.converters;

import com.murphybob.spritepacker.ImagePacking;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * TODO mklein: document class purpose
 * <p/>
 *
 * @author mklein
 */
public interface PackingConverter {
    void convert(ImagePacking imagePacking, Log log) throws MojoExecutionException;
}
