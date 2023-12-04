package com.foacraft.cloudnet.diffusion.command;

import cloud.commandframework.annotations.CommandPermission;
import eu.cloudnetservice.driver.provider.GroupConfigurationProvider;
import eu.cloudnetservice.driver.provider.ServiceTaskProvider;
import eu.cloudnetservice.node.command.annotation.CommandAlias;
import eu.cloudnetservice.node.command.annotation.Description;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;

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

    @Inject
    public DiffusionCommand(
            @NonNull ServiceTaskProvider taskProvider
    ) {
        this.taskProvider = taskProvider;
    }




}
