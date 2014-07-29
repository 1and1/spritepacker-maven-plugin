package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import java.awt.Point;
import java.nio.file.Path;
import java.util.List;

/**
 * Converts ImagePacking to a Less file, with the result that each icon's properties are available
 * via mixins. the ".create" mixin returns all properties, the ".pos" mixin returns only the position
 * and the ".size" mixin returns only the dimensions of the icon.
 *
 * @author mklein
 */
public class LessPackingConverter extends AbstractTextConverter {
    final String lessNamespace;

    /**
     * Create a Less converter with output file less and namespace lessNamespace.
     * @param less          the output Less file to write to
     * @param lessNamespace the Less namespace under which the mixins should be added
     */
    public LessPackingConverter(Path less, String lessNamespace) {
        super(less, "Less");
        this.lessNamespace = fixFirstChar(sanitize(lessNamespace));
    }

    /**
     * Create output Less string based on an ImagePacking.
     *
     * @param imageList     the list of images - must not be null
     * @param imagePacking  the ImagePacking to convert - must not be null
     * @param log           the log object to use
     * @return              String containing the Less file contents
     * @throws MojoExecutionException
     */
    @Override
    protected String createOutput(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException {
        StringBuilder sb = new StringBuilder("/* this file is generated by the sprite packer. don't make any changes in here! */\n");
        String tab = "";
        if (StringUtils.isNotEmpty(lessNamespace)) {
            tab = "    ";
            sb.append("/* icons can be referenced with the syntax \"#").append(lessNamespace).append(" > .create(icon-name);\" */\n")
              .append("#").append(lessNamespace).append("{\n");
        }
        sb.append(tab).append(".create(@name){.pos(@name);.size(@name);}\n");
        for (NamedImage image : imageList) {
            String name = sanitize(image.getName());
            Point position = imagePacking.getPosition(image);
            String x = intToPixel(-position.x);
            String y = intToPixel(-position.y);
            String width = intToPixel(image.getWidth());
            String height = intToPixel(image.getHeight());
            sb.append(tab).append(".pos(").append(name).append("){background-position:").append(x).append(" ").append(y).append(";}\n")
              .append(tab).append(".size(").append(name).append("){width:").append(width).append(";height:").append(height).append(";}\n");
        }

        if (StringUtils.isNotEmpty(lessNamespace)) {
            sb.append("}");
        }
        return sb.toString();
    }
}
