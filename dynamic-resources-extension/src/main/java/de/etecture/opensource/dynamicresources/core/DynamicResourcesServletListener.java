package de.etecture.opensource.dynamicresources.core;

import java.util.logging.Logger;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@WebListener
public class DynamicResourcesServletListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(
            "DynamicResourcesServletListener");

    @Inject
    BeanManager bm;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (bm != null) {
            LOG.finer("fire registration event");
            bm.fireEvent(sce);
            LOG.info("contextInitialized done.");
        } else {
            throw new IllegalStateException(
                    "BeanManager not injected in ServletContextListener!\nRegistration of Servlets for applications cannot be performed!");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
