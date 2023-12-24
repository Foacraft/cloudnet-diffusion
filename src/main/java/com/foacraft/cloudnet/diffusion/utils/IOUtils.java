package com.foacraft.cloudnet.diffusion.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.stream.Stream;

/**
 * CloudNet-Diffusion
 * com.foacraft.cloudnet.diffusion.utils.IOUtils
 *
 * @author scorez
 * @since 12/25/23 01:24.
 */
public class IOUtils {


    public static void deleteFilesWithExtensions(Path path, String... extensions) throws IOException {
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> {
                        String file = p.toString().toLowerCase();
                        for (String ext : extensions) {
                            if (file.endsWith(ext)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .forEach(p -> {
                        try {
                            Files.delete(p);
//                        System.out.println("Deleted: " + p);
                        } catch (IOException e) {
                            System.err.println("Error deleting file " + p + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error walking the directory " + path + ": " + e.getMessage());
        }
    }

    public static void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(p -> {
                    try {
                        deleteRecursively(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            Files.delete(path);
        }
    }

    public static void copyFiles(File sourceDirectory, File targetDirectory) throws IOException {
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
//            System.out.println("Source directory does not exist or is not a directory.");
            return;
        }

        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }

        File[] files = sourceDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String subDir = file.getName();
                    copyFiles(new File(sourceDirectory, subDir), new File(targetDirectory, subDir));
                } else {
                    File targetFile = new File(targetDirectory, file.getName());


                    if (!Files.isWritable(targetFile.toPath())) {
                        String sourceFileMD5 = calculateMD5(file);
                        String targetFileMD5 = calculateMD5(targetFile);

                        if (!sourceFileMD5.equals(targetFileMD5)) {
                            Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                            System.out.println("Copied: " + file.getAbsolutePath());
                        } else {
//                            System.out.println("Skipped (MD5 match): " + file.getAbsolutePath());
                        }
                    } else {
//                        System.out.println("Skipped (File in use): " + targetFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static String calculateMD5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                md5Digest.update(buffer, 0, bytesRead);
            }

            byte[] md5Bytes = md5Digest.digest();
            StringBuilder md5StringBuilder = new StringBuilder();

            for (byte md5Byte : md5Bytes) {
                md5StringBuilder.append(Integer.toString((md5Byte & 0xff) + 0x100, 16).substring(1));
            }

            return md5StringBuilder.toString();
        } catch (Throwable et) {
            return "";
        }
    }

}
