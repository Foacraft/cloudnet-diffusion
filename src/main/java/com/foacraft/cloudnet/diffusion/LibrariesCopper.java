package com.foacraft.cloudnet.diffusion;

import com.foacraft.cloudnet.diffusion.config.DiffusionConfig;
import com.foacraft.cloudnet.diffusion.utils.IOUtils;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.service.CloudServiceUpdateEvent;
import eu.cloudnetservice.driver.provider.ServiceTaskProvider;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.node.event.service.CloudServicePreProcessStartEvent;
import eu.cloudnetservice.node.service.CloudService;
import eu.cloudnetservice.node.service.defaults.provider.RemoteNodeCloudServiceProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                if (e.serviceInfo().provider() instanceof RemoteNodeCloudServiceProvider) {
//                    System.out.println("Diffusion: " + e.serviceInfo().name() + " is remote service. skipped!");
                    return;
                }
                copiedUUIDs.add(uuid);
                copyToStorage(e.serviceInfo());

                if (!(e.serviceInfo().provider() instanceof CloudService cloudService)) {
                    return;
                }
                Path TEMP_SERVICE_DIR = Path.of(System.getProperty("cloudnet.tempDir.services", "temp/services"));

                try {
                    Files.list(TEMP_SERVICE_DIR).forEach((path) -> {
                        if (!path.getFileName().toString().startsWith(cloudService.serviceId().name())) {
                            return;
                        }
                        if (path.getFileName().toString().endsWith(cloudService.serviceId().uniqueId().toString())) {
                            return;
                        }
                        try {
                            IOUtils.deleteRecursively(path);
                            System.out.println("Diffusion: Removed temp service " + cloudService.serviceId().name() + "'s residual files from " + path);
                        } catch (IOException ex) {
                        }
                    });
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

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
                IOUtils.copyFiles(file, serviceDirectory.toPath().resolve(file.getName()).toFile());
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
                IOUtils.copyFiles(sourceDirectory, targetDirectory);
            } catch (IOException e) {
                System.err.println("Diffusion-Copper: An error was coping " + sourceDirectory + " to " + targetDirectory);
                e.printStackTrace();
            }
        }
    }
}
