package com.foacraft.cloudnet.diffusion;

import dev.derklaro.aerogel.Inject;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.module.driver.DriverModule;
import eu.cloudnetservice.node.event.service.CloudServicePostProcessStartEvent;
import jakarta.inject.Singleton;

/**
 * cloudnet-temp-reverse
 * com.foacraft.cloudnet.tempreverse.TempReverse
 *
 * @author scorez
 * @since 12/2/23 23:05.
 */
@Singleton
public class Diffusion extends DriverModule {

    @Inject
    public void registerListener(EventManager eventManager) {
        eventManager.registerListener(this);
    }

    @EventListener
    public void e(CloudServicePostProcessStartEvent e) {
        System.out.println("服务 " + e.serviceInfo().name() + " 状态改变 " + e.serviceInfo().lifeCycle().name());
    }

}
