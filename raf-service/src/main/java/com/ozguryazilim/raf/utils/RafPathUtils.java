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

    public static String getRafCodeByPath(String path) {
        if (isInSharedRaf(path)) {
            return "SHARED";
        } else if (isInPrivateRaf(path)) {
            return "PRIVATE";
        } else if (isInGeneralRaf(path)) {
            return path.split("/")[2];
        } else {
            return null;
        }
    }

    public static boolean isRafRootPath(String fullPath) throws RafException {
        return isRootPath(fullPath, RAF_ROOT_PATH_NAME);
    }

    public static boolean isPrivateRafRootPath(String fullPath) throws RafException {
        return isRootPath(fullPath, PRIVATE_PATH_NAME);
    }

    public static boolean isPrivateRafPath(String fullPath) throws RafException {
        if (!isInPrivateRaf(fullPath)) {
            return false;
        }

        if (StringUtils.isNotBlank(fullPath)) {
            List<String> pathNames = Arrays.stream(fullPath.split("/"))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            return pathNames.size() == 2;
        }
        return false;
    }

    public static boolean isSharedRafRootPath(String fullPath) throws RafException {
        return isRootPath(fullPath, SHARED_PATH_NAME);
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

    public static String getRafPath(String fullPath) {
        if (isInSharedRaf(fullPath)) {
            return "/" + SHARED_PATH_NAME;
        } else if (isInPrivateRaf(fullPath)) {
            return "/" + PRIVATE_PATH_NAME + "/" + fullPath.split("/")[2];
        } else if (isInGeneralRaf(fullPath)) {
            return "/" + RAF_ROOT_PATH_NAME + "/" + fullPath.split("/")[2];
        } else {
            return "/" + fullPath.split("/")[1];
        }
    }

    public static String getRelativeRafObjectPath(String fullPath) {
        String rafPath = getRafPath(fullPath);
        return fullPath.substring(rafPath.length());
    }

    public static boolean isInSharedRaf(String path) {
        if (StringUtils.isBlank(path))  {
            return false;
        }
        return path.startsWith("/SHARED");
    }

    public static boolean isInGeneralRaf(String path) {
        if (StringUtils.isBlank(path))  {
            return false;
        }
        return path.startsWith("/RAF/");
    }

    public static boolean isInPrivateRaf(String path) {
        if (StringUtils.isBlank(path))  {
            return false;
        }
        return path.startsWith("/PRIVATE");
    }

}
