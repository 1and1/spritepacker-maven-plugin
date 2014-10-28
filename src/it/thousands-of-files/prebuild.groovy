import java.nio.file.Files
import java.nio.file.Paths

assert basedir != null;

def inputsDir = basedir.toPath().resolve(Paths.get("src", "images", "sprites"))

def input = inputsDir.resolve("0.png");
assert Files.exists(input);

for (i in 0..9999) {
  Files.copy(input, inputsDir.resolve(i+".png"))
}


