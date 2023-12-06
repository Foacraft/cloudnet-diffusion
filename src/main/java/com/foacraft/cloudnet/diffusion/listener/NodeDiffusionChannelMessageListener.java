package com.foacraft.cloudnet.diffusion.listener;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.driver.event.events.service.CloudServiceLifecycleChangeEvent;
import eu.cloudnetservice.driver.event.events.service.CloudServiceUpdateEvent;
import eu.cloudnetservice.node.event.service.CloudServicePostLifecycleEvent;
import jakarta.inject.Singleton;

/**
 * Cloudnet-Diffusion
 * com.foacraft.cloudnet.diffusion.listener.NodeDiffusionChannelMessageListener
 *
 * @author scorez
 * @since 12/3/23 12:48.
 */
@Singleton
public class NodeDiffusionChannelMessageListener {

    @EventListener
    public void e(ChannelMessageReceiveEvent e) {
//        System.out.println("收信通道 " + e.channel() + " 消息 " + e.message());
    }
}
