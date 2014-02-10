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
package de.etecture.opensource.dynamicresources.scanner;

import de.etecture.opensource.dynamicresources.annotations.declaration.Resource;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.DefaultApplication;
import de.etecture.opensource.dynamicresources.utils.BeanBuilder;
import de.etecture.opensource.dynamicresources.utils.ResourceWithTypeLiteral;
import de.etecture.opensource.dynamicresources.utils.ResourceWithURITemplateLiteral;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * this CDI-{@link Extension} is responsible for scanning all classes to find
 * {@link Resource}s.
 *
 * As a result, an {@link Application} is built and registered as a
 * {@link Bean}.
 *
 * @author rhk
 * @version
 * @since
 */
public class RequestReaderScanner implements Extension {

    private static final Logger LOG = Logger.getLogger(
            "ResourceMetadataScanner");
    private Set<Class<?>> resourceTypes = new HashSet<>();

    /**
     * will be called by the CDI-container to inform about an annotated type
     * that we have to scan.
     *
     * @param <T>
     * @param pat
     * @param bm
     * @throws Exception
     */
    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat,
            BeanManager bm) throws
            Exception {

        // look if the type is annotated with @Resource
        if (pat.getAnnotatedType().isAnnotationPresent(Resource.class)) {
            LOG.log(Level.FINER, "found resource interface type: {0}",
                    pat.getAnnotatedType().getJavaClass().getName());
            // add the type as a resourceType.
            resourceTypes.add(pat.getAnnotatedType().getJavaClass());
        }
    }

    /**
     * will be called by the CDI-Container to let us register all of our scanned
     * beans.
     *
     * @param abd
     * @param bm
     */
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        // create the application.
        DefaultApplication application = new DefaultApplication("/");
        // register the application as a bean.
        abd.addBean(BeanBuilder.forInstance(Application.class, application)
                .withDefault()
                .withAny()
                .applicationScoped()
                .build());

        // iterate about the resource types
        for (Class<?> resourceType : resourceTypes) {
            // create resource metadata
            LOG.log(Level.FINE, "build metadata for resourceType: {0}",
                    resourceType.getName());
            de.etecture.opensource.dynamicresources.metadata.Resource resource =
                    application.addAsResource(resourceType, null, null);
            LOG.log(Level.FINE, "register a resource with name: {0}", resource
                    .getName());
            // register the resource by it's name as a bean so it can be injected.
            abd.addBean(BeanBuilder
                    .forInstanceWithName(
                    de.etecture.opensource.dynamicresources.metadata.Resource.class,
                    resource, resource.getName())
                    .withDefault()
                    .withAny()
                    .withQualifier(new ResourceWithTypeLiteral(resourceType))
                    .withQualifier(new ResourceWithURITemplateLiteral(resource
                    .getUriTemplate()))
                    .applicationScoped()
                    .build());
        }
    }
}
