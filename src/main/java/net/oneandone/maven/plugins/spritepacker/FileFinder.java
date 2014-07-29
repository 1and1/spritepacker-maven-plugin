package net.oneandone.maven.plugins.spritepacker;

import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FileFinder extends SimpleFileVisitor<Path> {
    private final List<PathMatcher> includeMatchers;
    private final List<PathMatcher> excludeMatchers;
    private final List<Path> pathList;

    FileFinder(String[] includes, String[] excludes, FileSystem fileSystem) {
        Objects.requireNonNull(fileSystem);

        includeMatchers = new ArrayList<>(includes.length);
        for (String include : includes) {
            includeMatchers.add(fileSystem.getPathMatcher("glob:" + include));
        }

        excludeMatchers = new ArrayList<>(excludes.length);
        for (String exclude: excludes) {
            excludeMatchers.add(fileSystem.getPathMatcher("glob:" + exclude));
        }

        this.pathList = new ArrayList<>();
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attrs) {
        if (matches(file)) {
            pathList.add(file);
        }
        return FileVisitResult.CONTINUE;
    }

    private boolean matches(Path file) {
        for (PathMatcher excludeMatcher : excludeMatchers) {
            if (excludeMatcher.matches(file)) {
                return false;
            }
        }

        for (PathMatcher includeMatcher : includeMatchers) {
            if (includeMatcher.matches(file)) {
                return true;
            }
        }

        return false;
    }

    public List<Path> getResults() {
        return pathList;
    }

}