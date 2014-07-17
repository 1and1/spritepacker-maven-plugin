package com.murphybob.spritepacker.converters;

import com.murphybob.spritepacker.ImagePacking;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;

/**
 * TODO mklein: document class purpose
 * <p/>
 *
 * @author mklein
 */
public class CssPackingConverter extends AbstractTextConverter {

    public CssPackingConverter(File css) {
        super(css, "CSS");
    }

    @Override
    protected String createOutput(ImagePacking imagePacking, Log log) throws MojoExecutionException {
        return null; // TODO
    }
}
