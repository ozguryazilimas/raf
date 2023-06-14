package com.ozguryazilim.raf.utils;


import org.jodconverter.core.util.StringUtils;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        if (StringUtils.isBlank(path))  {
            return false;
        }
        return path.startsWith("/SHARED");
    }

}
