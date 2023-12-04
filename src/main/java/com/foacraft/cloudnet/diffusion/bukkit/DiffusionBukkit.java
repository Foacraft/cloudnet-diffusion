package com.foacraft.cloudnet.diffusion.bukkit;

import eu.cloudnetservice.ext.platforminject.api.PlatformEntrypoint;
import eu.cloudnetservice.ext.platforminject.api.stereotype.Dependency;
import eu.cloudnetservice.ext.platforminject.api.stereotype.PlatformPlugin;
import jakarta.inject.Singleton;

/**
 * Cloudnet-Diffusion
 * com.foacraft.cloudnet.diffusion.bukkit.DiffusionBukkitPlugin
 *
 * @author scorez
 * @since 12/3/23 01:42.
 */
@Singleton
@PlatformPlugin(
        platform = "bukkit",
        name = "@project_name@",
        version = "@project_version@",
        authors = {"Score2"},
        dependencies = {@Dependency(name = "CloudNet-Bridge")}
)
public class DiffusionBukkit implements PlatformEntrypoint {

    @Override
    public void onLoad() {

    }
}
