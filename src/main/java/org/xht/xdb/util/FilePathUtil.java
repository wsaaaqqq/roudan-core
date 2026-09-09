package org.xht.xdb.util;

public class FilePathUtil {

    /**
     * System.getProperty("user.dir").concat("/").concat(relativePath)
     */
    public static String get(String relativePath) {
        return System.getProperty("user.dir").concat("/").concat(relativePath);
    }

    /**
     * 一定以“/”结尾
     */
    public static String getDir(String relativePath) {
        if (relativePath.endsWith("/")) {
            return System.getProperty("user.dir").concat("/").concat(relativePath);
        }
        return System.getProperty("user.dir").concat("/").concat(relativePath).concat("/");
    }

}
