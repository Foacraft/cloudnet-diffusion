package com.foacraft.cloudnet.diffusion;

import com.foacraft.cloudnet.diffusion.command.DiffusionCommand;
import com.foacraft.cloudnet.diffusion.config.DiffusionConfig;
import com.foacraft.cloudnet.diffusion.listener.NodeDiffusionChannelMessageListener;
import dev.derklaro.aerogel.Inject;
import eu.cloudnetservice.driver.document.DocumentFactory;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.driver.event.events.service.CloudServiceUpdateEvent;
import eu.cloudnetservice.driver.module.ModuleLifeCycle;
import eu.cloudnetservice.driver.module.ModuleTask;
import eu.cloudnetservice.driver.module.driver.DriverModule;
import eu.cloudnetservice.driver.util.ModuleHelper;
import eu.cloudnetservice.node.command.CommandProvider;
import eu.cloudnetservice.node.module.listener.PluginIncludeListener;
import jakarta.inject.Singleton;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * cloudnet-diffusion
 * com.foacraft.cloudnet.diffusion.Diffusion
 *
 * @author scorez
 * @since 12/2/23 23:05.
 */
@Singleton
public class Diffusion extends DriverModule {

    public static final String DIFFUSION_CHANNEL_ID = "module:diffusion";

    public static DiffusionConfig config;

    @Inject
    public Diffusion() {

    }

    @ModuleTask(order = Byte.MAX_VALUE, lifecycle = ModuleLifeCycle.LOADED)
    public void initConf() {
        config = this.readConfig(
            DiffusionConfig.class,
            () -> new DiffusionConfig(
            "local/copper",
                Set.of("libraries", "libs"),
                Set.of("yml", "json")
            ),
            DocumentFactory.json()
        );
    }

    @ModuleTask(lifecycle = ModuleLifeCycle.LOADED)
    public void initListeners(
        @NonNull EventManager eventManager,
        @NonNull ModuleHelper moduleHelper
    ) {
        eventManager.registerListener(NodeDiffusionChannelMessageListener.class);
        eventManager.registerListener(LibrariesCopper.class);
        /*eventManager.registerListener(new PluginIncludeListener(
            "cloudnet-diffusion",
            Diffusion.class,
            moduleHelper,
            (service) -> true
        ));*/
    }

    @ModuleTask(lifecycle = ModuleLifeCycle.LOADED)
    public void registerCommands(@NonNull CommandProvider commandProvider) {
        commandProvider.register(DiffusionCommand.class);
    }
}
