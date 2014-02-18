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

import de.etecture.opensource.dynamicresources.annotations.Consumes;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.RequestReader;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotAllowedException;
import de.etecture.opensource.dynamicresources.utils.AnyLiteral;
import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 *
 * @author rhk
 */
public class RequestReaders {

    private static final WeakHashMap<String, RequestReader<?>> cache =
            new WeakHashMap<>();
    @Inject
    BeanManager beanManager;

    public <T> T read(Class<T> requestType, MediaType contentType, Reader reader)
            throws IOException {
        try {
            return resolve(requestType, contentType).processRequest(reader,
                    contentType.toString());
        } catch (MediaTypeNotAllowedException ex) {
            return null;
        }
    }

    private <T> RequestReader<T> resolve(Class<T> requestType,
            MediaType consumeType) throws MediaTypeNotAllowedException {
        String key = requestType.getName() + "::" + consumeType.toString();

        RequestReader<T> requestReader = (RequestReader<T>) cache.get(key);

        if (requestReader == null) {
            Set<Bean<?>> beans = beanManager
                    .getBeans(Object.class, new AnyLiteral());
            Map<Bean<?>, Class<?>> compatibleBeans = new HashMap<>();
            for (Bean<?> bean : beans) {
                if (RequestReader.class.isAssignableFrom(bean.getBeanClass())) {
                    for (Annotation annotation : bean.getQualifiers()) {
                        if (Consumes.class.isInstance(annotation)) {
                            if (((Consumes) annotation).requestType()
                                    .isAssignableFrom(
                                    requestType)) {
                                if (((Consumes) annotation).mimeType().length
                                        > 0) {
                                    for (String mime : ((Consumes) annotation)
                                            .mimeType()) {
                                        if (consumeType.isCompatibleTo(mime)) {
                                            compatibleBeans.put(bean,
                                                    ((Consumes) annotation)
                                                    .requestType());
                                        }
                                    }
                                } else {
                                    compatibleBeans.put(bean,
                                            ((Consumes) annotation)
                                            .requestType());
                                }
                            }
                        }
                    }
                }
            }

            if (compatibleBeans.isEmpty()) {
                throw new MediaTypeNotAllowedException(consumeType);
            } else {
                Bean<RequestReader<T>> bean = null;
                if (compatibleBeans.size() == 1) {
                    bean = (Bean<RequestReader<T>>) compatibleBeans.keySet()
                            .iterator()
                            .next();
                } else {
                    for (Map.Entry<Bean<?>, Class<?>> e : compatibleBeans
                            .entrySet()) {
                        if (e.getValue() != Object.class) {
                            bean = (Bean<RequestReader<T>>) e.getKey();
                        }
                    }
                    if (bean == null) {
                        bean = (Bean<RequestReader<T>>) compatibleBeans
                                .keySet()
                                .iterator()
                                .next();
                    }
                }
                requestReader = (RequestReader<T>) bean.create(
                        beanManager.createCreationalContext(bean));
                cache.put(key, requestReader);
            }
        }
        return requestReader;

    }
}
