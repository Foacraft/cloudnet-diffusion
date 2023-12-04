package com.foacraft.cloudnet.diffusion.bungee;

import eu.cloudnetservice.ext.platforminject.api.PlatformEntrypoint;
import eu.cloudnetservice.ext.platforminject.api.stereotype.Dependency;
import eu.cloudnetservice.ext.platforminject.api.stereotype.PlatformPlugin;
import jakarta.inject.Singleton;
import lombok.NonNull;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import org.bukkit.event.EventHandler;

/**
 * Cloudnet-Diffusion
 * com.foacraft.cloudnet.diffusion.bukkit.DiffusionBukkitPlugin
 *
 * @author scorez
 * @since 12/3/23 01:42.
 */
@Singleton
@PlatformPlugin(
        platform = "bungeecord",
        name = "@project_name@",
        version = "@project_version@",
        authors = {"Score2"},
        dependencies = {@Dependency(name = "CloudNet-Bridge")}
)
public class DiffusionBungee implements PlatformEntrypoint {

    @Override
    public void onLoad() {

    }

}
