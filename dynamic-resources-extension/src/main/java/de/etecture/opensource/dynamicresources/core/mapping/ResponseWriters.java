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
package de.etecture.opensource.dynamicresources.core.mapping;

import de.etecture.opensource.dynamicresources.annotations.Produces;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeAmbigiousException;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.utils.AnyLiteral;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
public class ResponseWriters {

    private static final WeakHashMap<String, ResponseWriter<?>> cache =
            new WeakHashMap<>();
    @Inject
    BeanManager beanManager;

    public <T> int getContentLength(T entity, MediaType acceptedType) {
        try {
            final ResponseWriter<T> responseWriter =
                    (ResponseWriter<T>) resolve(entity.getClass(), acceptedType);
            return responseWriter
                    .getContentLength(
                    entity,
                    acceptedType);
        } catch (MediaTypeNotSupportedException | MediaTypeAmbigiousException ex) {
            return -1;
        }
    }

    public <T> void write(T entity, MediaType acceptedType, Writer writer)
            throws IOException, MediaTypeNotSupportedException,
            MediaTypeAmbigiousException {
        final ResponseWriter<T> responseWriter =
                (ResponseWriter<T>) resolve(entity.getClass(), acceptedType);
        responseWriter
                .processElement(entity,
                writer,
                acceptedType);
    }

    private <T> ResponseWriter<T> resolve(final Class<T> responseType,
            MediaType acceptedType) throws MediaTypeNotSupportedException,
            MediaTypeAmbigiousException {
        String key = responseType.getName() + "::" + acceptedType.toString();

        ResponseWriter<T> responseWriter = (ResponseWriter<T>) cache.get(key);

        if (responseWriter == null) {
            Set<Bean<?>> beans = beanManager
                    .getBeans(Object.class, new AnyLiteral());
            Set<Bean<?>> compatibleBeans = new HashSet<>();
            for (Bean<?> bean : beans) {
                if (ResponseWriter.class.isAssignableFrom(bean.getBeanClass())) {
                    for (Annotation annotation : bean.getQualifiers()) {
                        if (Produces.class.isInstance(annotation)) {
                            if (((Produces) annotation).contentType()
                                    .isAssignableFrom(
                                    responseType)) {
                                if (((Produces) annotation).mimeType().length
                                        > 0) {
                                    for (String mime : ((Produces) annotation)
                                            .mimeType()) {
                                        if (acceptedType.isCompatibleTo(mime)) {
                                            compatibleBeans.add(bean);
                                        }
                                    }
                                } else {
                                    compatibleBeans.add(bean);
                                }
                            }
                        }
                    }
                }
            }

            if (compatibleBeans.isEmpty()) {
                throw new MediaTypeNotSupportedException(acceptedType);
            } else if (compatibleBeans.size() == 1) {
                Bean<ResponseWriter<T>> bean =
                        (Bean<ResponseWriter<T>>) compatibleBeans
                        .iterator()
                        .next();
                responseWriter = (ResponseWriter<T>) bean.create(
                        beanManager.createCreationalContext(bean));
                cache.put(key, responseWriter);
            } else {
                Set<String> writerClasses = new HashSet<>();
                for (Bean<?> bean : compatibleBeans) {
                    writerClasses.add(StringUtils.defaultIfBlank(bean.getName(),
                            bean.getBeanClass().toString()));
                }
                throw new MediaTypeAmbigiousException(acceptedType,
                        writerClasses.toArray(new String[writerClasses.size()]));
            }
        }
        return responseWriter;
    }
}
