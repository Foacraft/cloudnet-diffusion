package com.foacraft.cloudnet.diffusion.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import com.foacraft.cloudnet.diffusion.Diffusion;
import com.foacraft.cloudnet.diffusion.utils.IOUtils;
import eu.cloudnetservice.common.concurrent.Task;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.provider.GroupConfigurationProvider;
import eu.cloudnetservice.driver.provider.ServiceTaskProvider;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.driver.service.ServiceTask;
import eu.cloudnetservice.driver.service.ServiceTemplate;
import eu.cloudnetservice.driver.template.FileInfo;
import eu.cloudnetservice.node.command.annotation.CommandAlias;
import eu.cloudnetservice.node.command.source.CommandSource;
import eu.cloudnetservice.node.service.CloudService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Cloudnet-Diffusion
 * com.foacraft.cloudnet.diffusion.command.DiffusionCommand
 *
 * @author scorez
 * @since 12/4/23 22:56.
 */
@Singleton
@CommandAlias("diff")
@CommandPermission("cloudnet.command.diffusion")
public class DiffusionCommand {

    private final ServiceTaskProvider taskProvider;
    private final CloudServiceProvider serviceProvider;
    private final GroupConfigurationProvider groupProvider;

    @Inject
    public DiffusionCommand(
        @NonNull ServiceTaskProvider taskProvider,
        @NonNull CloudServiceProvider serviceProvider,
        @NonNull GroupConfigurationProvider groupProvider
    ) {
        this.taskProvider = taskProvider;
        this.serviceProvider = serviceProvider;
        this.groupProvider = groupProvider;
    }

    @Suggestions("task")
    public @NonNull List<String> suggestTaskName(@NonNull CommandContext<CommandSource> $, @NonNull String input) {
        return taskProvider.serviceTasks().stream().map(ServiceTask::name).toList();
    }

    @Parser(suggestions = "task")
    public @NonNull Collection<ServiceTask> serviceTasksParser(@NonNull CommandContext<CommandSource> $, @NonNull Queue<String> input) {
        var name = input.remove();
        if (name.equals("*")) {
            return this.taskProvider.serviceTasks();
        }
        return this.taskProvider.serviceTasks()
                .stream()
                .filter(task -> name.equalsIgnoreCase(task.name()))
                .collect(Collectors.toList());
    }

    @Suggestions("service")
    public @NonNull List<String> serviceSuggest(@NonNull CommandContext<?> $, @NonNull String input) {
        return this.serviceProvider.services()
                .stream()
                .map(ServiceInfoSnapshot::name)
                .toList();
    }

    @Parser(suggestions = "service")
    public @NonNull Collection<ServiceInfoSnapshot> serviceParser(
        @NonNull CommandContext<?> $,
        @NonNull Queue<String> input
    ) {
        var name = input.remove();
        if (name.equals("*")) {
            return this.serviceProvider.services();
        }
        return this.serviceProvider.services()
                .stream()
                .filter(service -> name.equalsIgnoreCase(service.name()))
                .collect(Collectors.toList());
    }

    @Suggestions("onlyFile")
    public @NonNull List<String> onlyFileSuggest(@NonNull CommandContext<?> $, @NonNull String input) {
        var task = ((ServiceTask) $.get("task"));
        var files = task.templates().stream().flatMap((template -> template.storage().listFiles(template, "", true).stream()));

        return files.map((FileInfo::path)).collect(Collectors.toList());
    }

    @Parser(suggestions = "onlyFile")
    public @NonNull Collection<String> onlyFileParser(
        @NonNull CommandContext<?> $,
        @NonNull Queue<String> input
    ) {
        var name = input.remove();
        return List.of(name);
    }

    /*// copy plugin dataFolder to temp service from template
    @CommandMethod("diffusion|diff updateFile <task> <fileName>")
    public void updateDataFolder(
        @NonNull CommandSource source,
        @NonNull @Argument("task") Collection<ServiceTask> serviceTasks,
        @NonNull @Argument("fileName") String fileName
    ) {
        serviceTasks.forEach((task) -> {
            var templateOptional = task.templates().stream().filter((template) -> template.storage().hasFile(template, "plugins/" + fileName)).findFirst();
            if (templateOptional.isEmpty()) {
                source.sendMessage("Diffusion-Update-File: The file " + fileName + " is not found in task " + task.name() + " templates.");
                return;
            }
            var template = templateOptional.get();

//            var originFile = new File(template.storage().fil)

            source.sendMessage(template.storage());

            serviceProvider.servicesByTask(task.name()).forEach((service) -> {
                if (!(service.provider() instanceof CloudService cloudService)) {
                    source.sendMessage("Diffusion-Update-File: service " + service.serviceId().name() + " is not a LocalService.");
                    return;
                }

                template.storage().pullAsync(template, cloudService.directory().resolve("plugins/" + fileName));
                source.sendMessage("Diffusion-Update-File: Copied " + fileName + " to service " + service.serviceId().name() + " from template " + template.fullName() + ".");
            });

        });

    }*/

    @CommandMethod("diffusion|diff pullTemplates <task> [onlyFile]")
    public void pullTemplates(
        @NonNull CommandSource source,
        @NonNull @Argument("task") Collection<ServiceTask> serviceTasks,
        @Nullable @Argument("onlyFile") String onlyFile
    ) {
        serviceTasks.forEach((task) -> {
            var templates = new HashSet<>(task.templates());

            // from task's groups
            task.groups().forEach((gn) -> {
                var group = groupProvider.groupConfiguration(gn);
                templates.addAll(group.templates());
            });

            // for environment
            groupProvider.groupConfigurations().forEach((group) -> {
                if (group.targetEnvironments().contains(task.processConfiguration().environment())) {
                    templates.addAll(group.templates());
                }
            });

            serviceProvider.servicesByTask(task.name()).forEach((service) -> {
                if (!(service.provider() instanceof CloudService cloudService)) {
                    source.sendMessage("Diffusion-Pull-Templates: service " + service.serviceId().name() + " is not a LocalService.");
                    return;
                }
                Task.supply(() -> {
                    try {
                        if (onlyFile != null) {
                            IOUtils.deleteRecursively(cloudService.directory().resolve(onlyFile));
                        } else {
                            IOUtils.deleteFilesWithExtensions(cloudService.directory(), Diffusion.config.pullsOverrideSuffixes().toArray(new String[0]));
                            templates.forEach((template -> template.storage().pull(template, cloudService.directory())));
                            return;
                        }
                        templates.forEach((template -> {
                            template.storage().deployDirectory(template, cloudService.directory().resolve(onlyFile));
                        }));
                    } catch (IOException e) {
                        source.sendMessage("Diffusion-Pull-Templates: Failed to pull task " + task.name() + "'s templates to service " + service.serviceId().name() + ".");
                        e.printStackTrace();
                    }
                });
                source.sendMessage("Diffusion-Pull-Templates: Pulled task " + task.name() + "'s templates to service " + service.serviceId().name() + ".");
            });
        });

    }

    @CommandMethod("diffusion|diff executeLater <task> <command>")
    public void executeLaterCommand(
        @NonNull CommandSource source,
        @NonNull @Argument("task") Collection<ServiceTask> serviceTasks,
        @NonNull @Greedy @Argument("command") String command
    ) {
        int interval = 3000;

        AtomicLong delay = new AtomicLong();
        var joiner = new StringJoiner(",");

        serviceTasks.forEach((task) -> {
            joiner.add(task.name());
            Collection<ServiceInfoSnapshot> serviceInfoSnapshots = serviceProvider.servicesByTask(task.name())
                    .stream().filter(ServiceInfoSnapshot::connected).toList();
            delay.addAndGet((long) serviceInfoSnapshots.size() * interval);
            Task.supply(() -> {
                serviceInfoSnapshots.forEach((serviceInfoSnapshot) -> {
                    serviceInfoSnapshot.provider().runCommand(command);
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        });

        source.sendMessage("Diffusion-Execute: Executor will gradually execute command for all service by task '" + joiner + "'. execution successful time left(s): " + TimeUnit.MILLISECONDS.toSeconds(delay.get()));
    }
}
