/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Entity;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 *
 * @author rhk
 */
public class DynamicResourcesExtension implements Extension {

    private Set<ResponseWriterBean> responseWriters = new HashSet<>();
    public Set<Class<?>> resourcesInterfaces = new HashSet<>();
    private final Logger log = Logger.getLogger("RepositoryExtension");

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        log.info("beginning the scanning process");
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) throws
            Exception {
        log.info(String.format("processing: %s",
                pat.getAnnotatedType().getJavaClass().getName()));
        if (pat.getAnnotatedType().isAnnotationPresent(Resource.class)) {
            log.info(String
                    .format("... found resource interface type: %s%n",
                    pat.getAnnotatedType().getJavaClass().getName()));
            Resource resource = pat.getAnnotatedType().getAnnotation(
                    Resource.class);
            resourcesInterfaces.add(pat.getAnnotatedType().getJavaClass());
        }
        if (pat.getAnnotatedType().getJavaClass().isEnum()
                && ResponseWriter.class
                .isAssignableFrom(pat.getAnnotatedType().getJavaClass())) {
            log.info(String.format("processing enum: %s",
                    pat.getAnnotatedType().getJavaClass().getName()));
            for (AnnotatedField af : pat.getAnnotatedType().getFields()) {
                if (af.isAnnotationPresent(Entity.class)) {
                    log.info(String.format(
                            "... found Writer for Entity: %s in class: %s",
                            af.getAnnotation(Entity.class).value().getName(),
                            af.getJavaMember().getName()));
                    final ResponseWriter responseWriter =
                            (ResponseWriter) af.getJavaMember().get(pat
                            .getAnnotatedType().getJavaClass());
                    if (af.isAnnotationPresent(Produces.class)) {
                        responseWriters.add(new ResponseWriterBean(
                                new EntityLiteral(af.getAnnotation(Entity.class)),
                                new ProducesLiteral(af.getAnnotation(
                                Produces.class)), responseWriter));
                    } else if (pat.getAnnotatedType().getJavaClass()
                            .isAnnotationPresent(Produces.class)) {
                        responseWriters.add(new ResponseWriterBean(
                                new EntityLiteral(af.getAnnotation(Entity.class)),
                                new ProducesLiteral(pat.getAnnotatedType()
                                .getJavaClass().getAnnotation(
                                Produces.class)), responseWriter));

                    } else {
                        responseWriters.add(new ResponseWriterBean(
                                new EntityLiteral(af.getAnnotation(Entity.class)),
                                new ProducesLiteral(responseWriter),
                                responseWriter));
                    }
                }
            }
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        log.info("finished the scanning process");
        for (ResponseWriterBean b : responseWriters) {
            abd.addBean(b);
        }
    }
}