package com.murphybob.spritepacker.converters;

import com.murphybob.spritepacker.ImagePacking;
import com.murphybob.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.awt.Point;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO mklein: document class purpose
 * <p/>
 *
 * @author mklein
 */
public class JsonPackingConverter extends AbstractTextConverter {
    private final String jsonpVar;

    /**
    * @param json     the output JSON file to write to
    * @param jsonpVar optional JSON variable name
    */
    public JsonPackingConverter(File json, String jsonpVar) {
        super(json, "JSON");
        this.jsonpVar = jsonpVar;

    }

    /**
     * Save the list of images as coordinate and dimension data in a JSON file
     *
     * @throws MojoExecutionException
     */
    @Override
    protected String createOutput(ImagePacking imagePacking, Log log) throws MojoExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        Map<String, Object> map = new HashMap<>();
        for (NamedImage n : imagePacking.getImages()) {
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
