package com.ozguryazilim.raf.utils;


import com.ozguryazilim.raf.RafException;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RafPathUtils {
    public static String RAF_ROOT_PATH_NAME = "RAF";
    public static String PRIVATE_PATH_NAME = "PRIVATE";
    public static String SHARED_PATH_NAME = "SHARED";

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

    public static boolean isRafRootPath(String fullPath) throws RafException {
        return isRootPath(fullPath, RAF_ROOT_PATH_NAME);
    }

    public static boolean isPrivateRafRootPath(String fullPath) throws RafException {
        return isRootPath(fullPath, PRIVATE_PATH_NAME);
    }

    private static boolean isRootPath(String fullPath, String rootPath) throws RafException {
        if (StringUtils.isNotBlank(fullPath)) {
            List<String> pathNames = Arrays.stream(fullPath.split("/"))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            if (pathNames.size() == 1 && Objects.equals(pathNames.get(0), rootPath)) {
                return true;
            }
            return false;
        } else {
            throw new RafException(String.format("Could not parse full path. Path: %s", fullPath));
        }
    }

    public static boolean isInSharedRaf(String path) {
        if (StringUtils.isBlank(path))  {
            return false;
        }
        return path.startsWith("/SHARED");
    }

}
