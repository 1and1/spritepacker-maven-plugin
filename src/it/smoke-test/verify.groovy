import java.nio.charset.StandardCharsets
import java.nio.file.*

assert basedir != null;

def imageDir = basedir.toPath().resolve(Paths.get("target", "images"));



System.err.println( groovy.lang.GroovySystem.getVersion());

assert Files.exists(imageDir);

def referenceDir = basedir.toPath().resolve("references");

["json", "less", "css", "png"].each {
  def basename = "sprite.".concat(it);
  def output = imageDir.resolve(basename).toFile();
  def reference = referenceDir.resolve(basename).toFile();
  assert output.exists();
  if (it == "png") {
    def filesAreEqual = output.getBytes() == reference.getBytes();
    assert filesAreEqual : "png output differs";
  } else {
    def charset = StandardCharsets.UTF_8.toString()
    reference.withReader(charset) { reader ->
      def lineNumber = 1;
      output.eachLine { outputLine ->
        def referenceLine = reader.readLine();
        assert referenceLine == outputLine : "Line " + lineNumber + " of file " + basename + " differs";
        lineNumber++;
      };
    };
  }
};

return true;