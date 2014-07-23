package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.util.List;

/**
 * Interface for converting an ImagePacking.
 *
 * @author mklein
 */
public interface PackingConverter {

    /**
     * Convert the specified ImagePacking.
     *
     * @param imageList     the list of images
     * @param imagePacking  the ImagePacking to convert
     * @param log           the log object to use
     * @throws MojoExecutionException
     */
    void convert(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException;
}
