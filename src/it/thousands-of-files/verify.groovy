import java.nio.file.Files
import java.nio.file.Paths

assert basedir != null;

def imageDir = basedir.toPath().resolve(Paths.get("target", "images"));

assert Files.exists(imageDir);

["json", "less", "css", "png"].each {
  def basename = "sprite.".concat(it);
  def output = imageDir.resolve(basename).toFile();
  assert output.exists();
};

return true;