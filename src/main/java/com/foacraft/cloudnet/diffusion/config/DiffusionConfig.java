package com.foacraft.cloudnet.diffusion.config;

import lombok.NonNull;

import java.util.List;
import java.util.Set;

/**
 * Cloudnet-Diffusion
 * com.foacraft.cloudnet.diffusion.config.DiffusionConfig
 *
 * @author scorez
 * @since 12/4/23 02:56.
 */
public record DiffusionConfig(
    @NonNull String copperStorageDir,
    @NonNull Set<String> copperDirectories
) {
}
