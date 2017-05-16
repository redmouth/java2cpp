package co.deepblue.java2cpp.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created by levin on 17-5-7.
 */
public class FileUtils {
    public static BufferedWriter createWriter(Path fileDir, String fileName) throws Exception {
        return createWriter(fileDir.toString(), fileName);

    }

    public static BufferedWriter createWriter(String fileDir, String fileName) throws Exception {
        Path filePath = Paths.get(fileDir, fileName);

        File file = filePath.toFile();
        if (!file.exists())
            file.createNewFile();

        return new BufferedWriter(new FileWriter(file));
    }

    public static void copyDirectory(String srcDir, String dstDir) {
        File dstFile = new File(dstDir);
        dstFile.mkdirs();
        File file = new File(srcDir);
        File[] filelist = file.listFiles();
        if (filelist != null) {
            for (File f : filelist) {
                if (f.isDirectory()) {
                    copyDirectory(PathUtils.concatenate(srcDir, f.getName()),
                            PathUtils.concatenate(dstDir, f.getName()));
                } else {
                    if (f.getName().endsWith(".h") || f.getName().endsWith(".cpp") || f.getName().endsWith(".cxx")) {
                        try {
                            Files.copy(Paths.get(f.getAbsolutePath()), Paths.get(dstDir, f.getName()), StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    public static void deleteDirectory(File file)
            throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                if (files.length == 0) {
                    file.delete();
                } else {
                    for (File f : files) {
                        deleteDirectory(f);
                    }

                    File[] fList = file.listFiles();
                    if (fList != null && fList.length == 0)
                        file.delete();
                }
            }
        } else {
            file.delete();
        }
    }
}
