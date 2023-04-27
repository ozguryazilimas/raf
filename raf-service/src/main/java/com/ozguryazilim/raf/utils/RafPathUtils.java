package com.ozguryazilim.raf.utils;


import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RafPathUtils {
    public static boolean isPathsEqual(String p1, String p2) {
        try {
            Path firstPath = Paths.get(p1);
            Path secondPath = Paths.get(p2);


            return firstPath.equals(secondPath);
        } catch (InvalidPathException ex) {
            return false;
        }
    }
}
