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

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Version;
import de.etecture.opensource.dynamicresources.api.VersionNumberRange;
import de.etecture.opensource.dynamicresources.spi.VersionNumberResolver;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @param <T>
 * @param <A>
 * @author rhk
 */
public abstract class AbstractMimeAndVersionResolver<T, A extends Annotation> {

    @Inject
    BeanManager beanManager;
    @Inject
    VersionNumberResolver versionNumberResolver;
    private final Class<T> beanClass;
    private final Class<A> annotationClass;

    protected AbstractMimeAndVersionResolver(
            Class<T> beanClass, Class<A> annotationClass) {
        this.beanClass = beanClass;
        this.annotationClass = annotationClass;
    }

    protected abstract Class<?> getTypeDefinition(A annotation);

    protected abstract String[] getMimeType(A annotation);

    protected abstract String getVersion(A annotation);

    public Map<MediaType, List<Version>> getAvailableFormats(
            Class<?> resourceClass) {
        Map<MediaType, List<Version>> formats = new HashMap<>();
        final Set<Bean<?>> beans = beanManager.getBeans(beanClass,
                new AnnotationLiteral<Any>() {
            private static final long serialVersionUID = 1L;
        });
        outer:
        for (Bean<?> bean : beans) {
            for (Annotation qualifier : bean.getQualifiers()) {
                if (qualifier.annotationType() == annotationClass) {
                    A annotation = annotationClass.cast(qualifier);
                    if (!getTypeDefinition(annotation).isAssignableFrom(
                            resourceClass)) {
                        continue outer;
                    } else {
                        final String[] mimeTypes = getMimeType(annotation);
                        for (String mimeType : mimeTypes) {
                            MediaType mediaType = new MediaTypeExpression(
                                    mimeType);
                            List<Version> versions;
                            if (formats.containsKey(mediaType)) {
                                versions = formats.get(mediaType);
                            } else {
                                versions = new ArrayList<>();
                                formats.put(mediaType, versions);
                            }
                            versions.add(new VersionExpression(getVersion(
                                    annotation)));
                        }
                        continue outer;
                    }
                }
            }
        }
        return formats;
    }

    public T resolve(Class<?> resourceClass,
            MediaType mimeType,
            VersionNumberRange versionMatcher) {
        final Set<Bean<?>> beans = beanManager.getBeans(beanClass,
                new AnnotationLiteral<Any>() {
            private static final long serialVersionUID = 1L;
        });
        System.out.printf(
                "selected mimetype: %s, selected version: %s, beans: %s, entity: %s%n",
                mimeType,
                versionMatcher == null ? "not specified" : versionMatcher
                .toString(),
                beans.size(), resourceClass.getName());
        // (1) Build the map of versioned beans.
        TreeMap<Version, Bean<T>> versionedBeans =
                new TreeMap<>(new VersionComparator(false));
        outer:
        for (Bean<?> bean : beans) {
            for (Annotation qualifier : bean.getQualifiers()) {
                if (qualifier.annotationType() == annotationClass) {
                    A annotation = annotationClass.cast(qualifier);
                    if (!getTypeDefinition(annotation).isAssignableFrom(
                            resourceClass)) {
                        continue outer;
                    } else {
                        if (mimeType != null && mimeType.isCompatibleTo(
                                getMimeType(annotation))) {
                            if (StringUtils.isNotBlank(getVersion(annotation))) {
                                // found a mimetype and a version, so add the bean with this version
                                versionedBeans.put(new VersionExpression(
                                        getVersion(annotation)),
                                        (Bean<T>) bean);
                            } else {
                                versionedBeans.put(
                                        new VersionExpression(bean),
                                        (Bean<T>) bean);
                            }
                        }
                        continue outer;
                    }
                }
            }
            // add the bean only, if the mimeType is not specified or text/plain.
            versionedBeans.put(
                    new VersionExpression(bean),
                    (Bean<T>) bean);
        }
        System.out.println("Versions are: ");
        for (Version v : versionedBeans.keySet()) {
            System.out.println(v.toString());
        }
        // (2) now resolve the correct bean for the correct version
        Bean<T> resolved = versionNumberResolver.resolve(
                versionedBeans, versionMatcher);
        if (resolved != null) {
            return resolved.create(beanManager
                    .createCreationalContext(resolved));
        }
        return null;
    }
}
