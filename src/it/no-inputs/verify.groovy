import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

assert basedir != null;

def imageDir = basedir.toPath().resolve(Paths.get("target", "images"));

assert Files.notExists(imageDir);

return true;