package com.foacraft.cloudnet.diffusion.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import eu.cloudnetservice.common.concurrent.Task;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.provider.ServiceTaskProvider;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.driver.service.ServiceTask;
import eu.cloudnetservice.node.command.annotation.CommandAlias;
import eu.cloudnetservice.node.command.source.CommandSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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

    @Inject
    public DiffusionCommand(
            @NonNull ServiceTaskProvider taskProvider,
            @NonNull CloudServiceProvider serviceProvider
    ) {
        this.taskProvider = taskProvider;
        this.serviceProvider = serviceProvider;
    }

    @Parser(suggestions = "task")
    public @NonNull ServiceTask taskNameParser(@NonNull CommandContext<CommandSource> $, @NonNull Queue<String> input) {
        var name = input.remove();
        return taskProvider.serviceTasks()
                .stream()
                .filter(task -> name.equalsIgnoreCase(task.name()))
                .findFirst()
                .orElseThrow();
    }

    @Suggestions("task")
    public @NonNull List<String> suggestTaskName(@NonNull CommandContext<CommandSource> $, @NonNull String input) {
        return taskProvider.serviceTasks().stream().map(ServiceTask::name).toList();
    }

    @CommandMethod("diffusion|diff executeLater <task> <command>")
    public void executeLaterCommand(
            @NonNull CommandSource source,
            @NonNull @Argument("task") ServiceTask task,
            @NonNull @Argument("command") String[] commandArgs
    ) {
        int interval = 3000;

        StringBuilder command = new StringBuilder();
        for (String s : commandArgs) {
            command.append(s).append(" ");
        }

        AtomicLong delay = new AtomicLong();
        Collection<ServiceInfoSnapshot> serviceInfoSnapshots = serviceProvider.servicesByTask(task.name());
        Task.supply(() -> {
            serviceInfoSnapshots.forEach((serviceInfoSnapshot) -> {
                serviceInfoSnapshot.provider().runCommand(command.toString());
                delay.addAndGet(interval);
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        System.out.println("Diffusion-Execute: Executor will gradually execute command for all service by task " + task.name() + ". execution successful time left(s): " + TimeUnit.MILLISECONDS.toSeconds(delay.get()));
    }


}
