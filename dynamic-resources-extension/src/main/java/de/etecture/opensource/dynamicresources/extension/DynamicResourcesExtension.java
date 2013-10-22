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

import de.etecture.opensource.dynamicresources.api.Consumes;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.RequestReader;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.Resources;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.Producer;

/**
 *
 * @author rhk
 */
public class DynamicResourcesExtension implements Extension {

    private Set<ResponseWriterBean> responseWriters = new HashSet<>();
    private Set<RequestReaderBean> requestReaders = new HashSet<>();
    public Set<Class<?>> resourcesInterfaces = new HashSet<>();
    private Map<Class<?>, ResourcesBean> resourcesInjectionTargets =
            new HashMap<>();
    private final Logger log = Logger.getLogger("DynamicResourcesExtension");

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        log.info("beginning the scanning process");
    }

    void processInjectionTargets(@Observes ProcessInjectionTarget pit,
            BeanManager bm) {
        for (InjectionPoint ip : ((Producer<InjectionPoint>) pit
                .getInjectionTarget()).getInjectionPoints()) {
            if (ip.getType() instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) ip.getType();
                if (pt.getRawType() == Resources.class) {
                    Class<?> resourceClass =
                            (Class<?>) pt.getActualTypeArguments()[0];
                    if (!resourceClass.isInterface()
                            || !this.resourcesInterfaces.contains(resourceClass)) {
                        throw new InjectionException(String.format(
                                "the injection point for resources in bean: %s for class: %s is not correct. The Resource-Class is not an interface or is not annotated with @Resource.",
                                ip.getMember().getDeclaringClass().
                                getSimpleName(), resourceClass.getSimpleName()));
                    }
                    log.info(
                            String.format(
                            "found injection point for resources of type: %s in bean: %s (field: %s)",
                            resourceClass.getName(),
                            ip.getMember().getDeclaringClass().getSimpleName(),
                            ip.getMember().getName()));
                    if (!resourcesInjectionTargets.containsKey(resourceClass)) {
                        resourcesInjectionTargets.put(resourceClass,
                                createResourcesBean(bm, resourceClass, ip
                                .getType()));
                    }
                }
            }
        }
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat,
            BeanManager beanManager) throws
            Exception {

        if (pat.getAnnotatedType().isAnnotationPresent(Resource.class)) {
            log.info(String.format("found resource interface type: %s%n",
                    pat.getAnnotatedType().getJavaClass().getName()));
            Resource resource = pat.getAnnotatedType().getAnnotation(
                    Resource.class);

            resourcesInterfaces.add(pat.getAnnotatedType().getJavaClass());
        }


        if (pat.getAnnotatedType().getJavaClass().isEnum()
                && ResponseWriter.class
                .isAssignableFrom(pat.getAnnotatedType().getJavaClass())) {
            for (AnnotatedField af
                    : pat.getAnnotatedType()
                    .getFields()) {
                if (af.isAnnotationPresent(Produces.class)) {
                    final String name = String.format("%s_%s",
                            pat.getAnnotatedType().getJavaClass().
                            getSimpleName(), af.getJavaMember().getName());
                    log.info(String.format(
                            "found ResponseWriter for Entity: %s with name: %s",
                            af.getAnnotation(Produces.class).contentType()
                            .getName(),
                            name));
                    final ResponseWriter responseWriter =
                            (ResponseWriter) af.getJavaMember().get(pat
                            .getAnnotatedType().getJavaClass());
                    if (af.isAnnotationPresent(Produces.class)) {
                        responseWriters.add(new ResponseWriterBean(
                                beanManager,
                                new ProducesLiteral(af.getAnnotation(
                                Produces.class)), responseWriter, name));
                    } else if (pat.getAnnotatedType().getJavaClass()
                            .isAnnotationPresent(Produces.class)) {
                        responseWriters.add(new ResponseWriterBean(
                                beanManager,
                                new ProducesLiteral(pat.getAnnotatedType()
                                .getJavaClass().getAnnotation(
                                Produces.class)), responseWriter, name));

                    } else {
                        responseWriters.add(new ResponseWriterBean(
                                beanManager,
                                new ProducesLiteral(responseWriter),
                                responseWriter, name));
                    }
                }
            }
        }

        if (pat.getAnnotatedType().getJavaClass().isEnum()
                && RequestReader.class
                .isAssignableFrom(pat.getAnnotatedType().getJavaClass())) {
            for (AnnotatedField af
                    : pat.getAnnotatedType()
                    .getFields()) {
                if (af.isAnnotationPresent(Consumes.class)) {
                    final String name = String.format("%s_%s",
                            pat.getAnnotatedType().getJavaClass().
                            getSimpleName(), af.getJavaMember().getName());
                    log.info(String.format(
                            "found RequestReader for Entity: %s with name: %s",
                            af.getAnnotation(Consumes.class).requestType()
                            .getName(),
                            name));
                    final RequestReader requestReader =
                            (RequestReader) af.getJavaMember().get(pat
                            .getAnnotatedType().getJavaClass());
                    if (af.isAnnotationPresent(Consumes.class)) {
                        requestReaders.add(new RequestReaderBean(
                                new ConsumesLiteral(af.getAnnotation(
                                Consumes.class)), requestReader, name));
                    } else if (pat.getAnnotatedType().getJavaClass()
                            .isAnnotationPresent(Consumes.class)) {
                        requestReaders.add(new RequestReaderBean(
                                new ConsumesLiteral(pat.getAnnotatedType()
                                .getJavaClass().getAnnotation(
                                Consumes.class)), requestReader, name));

                    } else {
                        requestReaders.add(new RequestReaderBean(
                                new ConsumesLiteral(requestReader),
                                requestReader, name));
                    }
                }
            }
        }
    }

    <T> Resources<T> createResourcesImpl(BeanManager bm, Class<T> clazz) {
        return new ResourcesImpl<>(bm, clazz);
    }

    <T> ResourcesBean<T> createResourcesBean(BeanManager bm,
            Class<T> resourcesClazz, Type t) {
        return new ResourcesBean<>(t,
                createResourcesImpl(bm, resourcesClazz));
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        log.info("finished the scanning process");
        for (ResponseWriterBean b : responseWriters) {
            log.info(String.format("add a %s", b.toString()));
            abd.addBean(b);
        }
        for (RequestReaderBean b : requestReaders) {
            log.info(String.format("add a %s", b.toString()));
            abd.addBean(b);
        }
        for (ResourcesBean b : resourcesInjectionTargets.values()) {
            log.info(String.format("add a %s", b.toString()));
            abd.addBean(b);
        }
    }
}
