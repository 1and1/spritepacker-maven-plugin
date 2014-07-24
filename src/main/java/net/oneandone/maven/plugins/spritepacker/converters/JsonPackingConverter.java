package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.awt.Point;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts ImagePacking to a JSON(P) file, with the result that each icon's details are available
 * via a named property which encapsulates w, h, x, y and xy as pixel values as well as storing
 * integer values for x, y, w and h in the property n.
 *
 * @author Robert Murphy, mklein
 */
public class JsonPackingConverter extends AbstractTextConverter {
    private final String jsonpVar;

    /**
     * Create a JSON converter with output file json and optional JSONP variable jsonpVar.
     *  @param json     the output JSON file to write to
     * @param jsonpVar optional JSONP variable name
     */
    public JsonPackingConverter(Path json, String jsonpVar) {
        super(json, "JSON");
        this.jsonpVar = jsonpVar;
    }

    /**
     *Create output JSON string based on an ImagePacking.
     *
     * @param imageList     the list of images - must not be null
     * @param imagePacking  the ImagePacking to convert - must not be null
     * @param log           the log object to use
     * @return              String containing the JSON file contents
     * @throws MojoExecutionException
     */
    @Override
    protected String createOutput(List<NamedImage> imageList, ImagePacking imagePacking, Log log) throws MojoExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        Map<String, Object> map = new HashMap<>();
        for (NamedImage n : imageList) {
            Point position = imagePacking.getPosition(n);

            Map<String, Object> props = new HashMap<>();
            int x = position.x;
            int y = position.y;
            int width = n.getWidth();
            int height = n.getHeight();

            String xStr = intToPixel(-x);
            String yStr = intToPixel(-y);

            props.put("x", xStr);
            props.put("y", yStr);
            props.put("w", intToPixel(width));
            props.put("h", intToPixel(height));
            props.put("xy", xStr + " " + yStr);

            Map<String, Integer> numbers = new HashMap<>();
            numbers.put("x", x);
            numbers.put("y", y);
            numbers.put("w", width);
            numbers.put("h", height);

            props.put("n", numbers);

            map.put(n.getName(), props);
        }

        // Generate json representation of map object
        String out;
        try {
            out = mapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't generate JSON data", e);
        }

        // If user has passed in a variable to wrap this in, append it to the front
        if (jsonpVar != null) {
            out = jsonpVar + " = " + out;
        }

        return out;
    }
}
