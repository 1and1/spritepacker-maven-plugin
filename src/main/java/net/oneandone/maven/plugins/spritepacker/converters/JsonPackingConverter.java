package net.oneandone.maven.plugins.spritepacker.converters;

import net.oneandone.maven.plugins.spritepacker.ImagePacking;
import net.oneandone.maven.plugins.spritepacker.NamedImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.plexus.util.StringUtils;

import java.awt.Point;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts ImagePacking to a JSON(P) file, with the result that each icon's details are available
 * via a named property which encapsulates w, h, x, y and xy as pixel values as well as storing
 * integer values for x, y, w and h in the property n.
 *
 * @author Robert Murphy, mklein
 */
public class JsonPackingConverter extends AbstractTextConverter {
    public static final Pattern CHARS_NOT_ALLOWED_IN_VARIABLES = Pattern.compile("[^\\p{L}\\d_$]");
    public static final Collection<String> RESERVED_WORDS = Collections.unmodifiableCollection(Arrays.asList(
            "break", "case", "catch", "continue", "debugger", "default", "delete", "do", "else",
            "finally", "for", "function", "if", "in", "instanceof", "new", "return", "switch",
            "this", "throw", "try", "typeof", "var", "void", "while", "with", "class", "const",
            "enum", "export", "extends", "import", "super", "implements", "interface", "let",
            "package", "private", "protected", "public", "static", "yield", "null", "true",
            "false", "NaN", "Infinity", "undefined", "eval", "arguments", "int", "byte", "char",
            "goto", "long", "final", "float", "short", "double", "native", "throws", "boolean",
            "abstract", "volatile", "transient", "synchronized"));

    protected static final int IMAGE_PROPERTY_COUNT = 6;
    protected static final int RAW_NUMBER_COUNT = 4;

    final String jsonpVar;

    /**
     * Create a JSON converter with output file json and optional JSONP variable jsonpVar.
     * @param json     the output JSON file to write to
     * @param jsonpVar optional JSONP variable name
     */
    public JsonPackingConverter(Path json, String jsonpVar) {
        super(json, "JSON");
        this.jsonpVar = fixJsonpVar(jsonpVar);
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
        Map<String, Object> map = buildOutputMap(imageList, imagePacking);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

            String out = mapper.writeValueAsString(map);
            return jsonpVar == null ? out : jsonpVar + " = " + out;
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't generate JSON data", e);
        }
    }

    /**
     * Builds the output structure
     * @param imageList a list of images, defines the order of the entries in the map
     * @param imagePacking a packing of the images in the list
     * @return a map with an entry for each image
     */
    protected Map<String, Object> buildOutputMap(List<NamedImage> imageList, ImagePacking imagePacking) {
        Map<String, Object> map = new LinkedHashMap<>(imageList.size());
        for (NamedImage n : imageList) {
            Point position = imagePacking.getPosition(n);

            Map<String, Object> props = new LinkedHashMap<>(IMAGE_PROPERTY_COUNT);
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

            Map<String, Integer> numbers = new LinkedHashMap<>(RAW_NUMBER_COUNT);
            numbers.put("x", x);
            numbers.put("y", y);
            numbers.put("w", width);
            numbers.put("h", height);

            props.put("n", numbers);

            map.put(n.getName(), props);
        }
        return map;
    }

    /**
     * Remove invalid characters from JavaScript variable and avoid leading numbers or reserved words
     * by prefixing them with an underscore
     *
     * @param jsonpVar  the jsonpVar to fix
     * @return          the fixed jsonpVar
     */
    protected static String fixJsonpVar(String jsonpVar) {
        if (StringUtils.isNotEmpty(jsonpVar)) {
            String newVar = CHARS_NOT_ALLOWED_IN_VARIABLES.matcher(jsonpVar).replaceAll("");
            boolean needsPrefix = newVar.isEmpty() || hasLeadingNumber(newVar) || RESERVED_WORDS.contains(newVar);
            return needsPrefix ? "_" + newVar : newVar;
        }
        return jsonpVar;
    }

    /**
     * Check if a string starts with an arabic [0-9] digit character
     *
     * @param str   the string to check
     * @return      whether the string starts with [0-9]
     */
    private static boolean hasLeadingNumber(String str) {
        return str.charAt(0) >= '0' && str.charAt(0) <= '9';
    }
}
