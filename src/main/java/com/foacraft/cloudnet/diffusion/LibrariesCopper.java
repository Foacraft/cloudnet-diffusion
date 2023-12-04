package com.foacraft.cloudnet.diffusion;

import com.foacraft.cloudnet.diffusion.config.DiffusionConfig;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.service.CloudServiceUpdateEvent;
import eu.cloudnetservice.driver.provider.ServiceTaskProvider;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.node.event.service.CloudServicePreProcessStartEvent;
import eu.cloudnetservice.node.service.CloudService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Cloudnet-Diffusion
 * com.foacraft.cloudnet.diffusion.LibrariesCopper
 *
 * @author scorez
 * @since 12/4/23 02:32.
 */
@Singleton
public class LibrariesCopper {

    private final Set<UUID> copiedUUIDs = new HashSet<>();
    private final ServiceTaskProvider taskProvider;

    @Inject
    public LibrariesCopper(
        @NonNull ServiceTaskProvider taskProvider
    ) {
        this.taskProvider = taskProvider;
    }

    @EventListener
    public void e(CloudServiceUpdateEvent e) {
        UUID uuid = e.serviceInfo().serviceId().uniqueId();
        if (copiedUUIDs.contains(uuid)) {
            return;
        }
        switch (e.serviceInfo().lifeCycle()) {
            case RUNNING -> {
                copiedUUIDs.add(uuid);
                copyToStorage(e.serviceInfo());
//                System.out.println("添加" + uuid);
            }
            case STOPPED, DELETED -> {
                copiedUUIDs.remove(uuid);
//                System.out.println("删除 " + uuid);
            }
        }
//        System.out.println("服务 " + e.serviceInfo().name() + " 状态改变 " + e.serviceInfo().lifeCycle().name());
    }

    @EventListener
    public void e(CloudServicePreProcessStartEvent e) {
        copyToService(e.serviceInfo());
    }

    public void copyToService(ServiceInfoSnapshot serviceInfoSnapshot) {
        CloudService cloudService = (CloudService) serviceInfoSnapshot.provider();
        File serviceDirectory = cloudService.directory().toFile();
        String taskName = cloudService.serviceId().taskName();
        File storageTaskDirectory = new File(Diffusion.config.copperStorageDir(), taskName);
        if (!storageTaskDirectory.exists()) {
            return;
        }
        File[] files = storageTaskDirectory.listFiles();
        if (files == null) {
            return;
        }
        try {
            for (File file : files) {
                copyFiles(file, serviceDirectory.toPath().resolve(file.getName()).toFile());
//                System.out.println(file.toPath() + " -> " + serviceDirectory.toPath().resolve(file.getName()));
            }
        } catch (Throwable t) {
            System.err.println("Diffusion-Copper: An error was coping " + storageTaskDirectory + " to " + serviceDirectory);
            return;
        }

        System.out.println("Diffusion-Copper: Copied task " + taskName + "'s files to " + cloudService.serviceId().name());
    }

    public void copyToStorage(ServiceInfoSnapshot serviceInfoSnapshot) {
        CloudService cloudService = (CloudService) serviceInfoSnapshot.provider();
        File serviceDirectory = cloudService.directory().toFile();
        String taskName = cloudService.serviceId().taskName();

        for (String copperDirectory : Diffusion.config.copperDirectories()) {
            File sourceDirectory = new File(serviceDirectory, copperDirectory);
            File targetDirectory = new File(Diffusion.config.copperStorageDir() + File.separator + taskName, copperDirectory);
            try {
                copyFiles(sourceDirectory, targetDirectory);
            } catch (IOException e) {
                System.err.println("Diffusion-Copper: An error was coping " + sourceDirectory + " to " + targetDirectory);
                e.printStackTrace();
            }
        }
    }

    private void copyFiles(File sourceDirectory, File targetDirectory) throws IOException {
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

    private String calculateMD5(File file) {
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
