package com.ozguryazilim.raf.utils;


import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class RafPathUtils {

    private RafPathUtils() {
        throw new IllegalStateException("Util Class");
    }

    public static boolean isPathsEqual(String p1, String p2) {
        try {
            Path firstPath = Paths.get(p1);
            Path secondPath = Paths.get(p2);


            return firstPath.equals(secondPath);
        } catch (InvalidPathException ex) {
            return false;
        }
    }

    public static boolean isInSharedRaf(String path) {
        Objects.requireNonNull(path);
        return path.startsWith("/SHARED");
    }

}
