/*
 *  This file is part of the ETECTURE Open Source Community Projects.
 *
 *  Copyright (c) 2013 by:
 *
 *  ETECTURE GmbH
 *  Darmstädter Landstraße 112
 *  60598 Frankfurt
 *  Germany
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  3. Neither the name of the author nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.core;

import de.etecture.opensource.dynamicresources.metadata.Application;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;

/**
 * this application-scoped bean receives the ServletContextEvent from the
 * ServletContextListener and registers a servlet for each application found
 * from scanning.
 *
 * @author rhk
 * @version
 * @since
 */
@ApplicationScoped
public class DynamicResourcesServletRegistration {

    private static final Logger LOG = Logger.getLogger(
            "DynamicResourcesServletRegistration");
    private static final String APPLICATION_MAPPING_TEMPLATE = "/%s/*";
    private static final String APPLICATION_SERVLET_NAME_TEMPLATE = "%s-Servlet";
    @Inject
    Instance<Application> allApplications;

    public void onRegistration(@Observes(during =
            TransactionPhase.AFTER_COMPLETION) ServletContextEvent sce) {
        LOG.info("start registration of application servlets.");
        for (Application application : allApplications) {
            ServletRegistration.Dynamic dn = sce.getServletContext().addServlet(
                    String.format(APPLICATION_SERVLET_NAME_TEMPLATE, application
                    .getName()),
                    DynamicResourcesServlet.class);
            dn.setInitParameter(DynamicResourcesServlet.APPLICATION_NAME,
                    application.getName());
            final String mapping =
                    String.format(APPLICATION_MAPPING_TEMPLATE, application
                    .getBase());
            dn.addMapping(mapping);
            LOG.log(Level.INFO,
                    "application: {0} is registered at: {1}{2} with servlet: {3}",
                    new Object[]{
                application.getName(),
                sce.getServletContext().getContextPath(),
                mapping,
                dn.getName()});
        }
        LOG.info("registration of application servlets done.");
    }
}
