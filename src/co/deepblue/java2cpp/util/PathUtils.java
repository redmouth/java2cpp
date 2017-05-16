package co.deepblue.java2cpp.util;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by levin on 17-5-7.
 */
public class PathUtils {
    public static String packageName2Path(String packageName) {
        return packageName.replace(".", File.separator);
    }

    public static String removeExtension(String fileName, String extension) {
        if (fileName.endsWith(extension)) {
            return fileName.substring(0, fileName.lastIndexOf(extension));
        }

        return fileName;
    }

    public static String concatenate(String dir, String name) {
        return Paths.get(dir, name).toString();
    }
}
