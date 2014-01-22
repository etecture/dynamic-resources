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

import de.etecture.opensource.dynamicresources.api.Resources;
import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author rhk
 */
public class ResourcesImpl<T> implements Resources<T> {

    private final BeanManager bm;
    private final Class<T> resourceClass;
    protected final Map<String, Object> params;
    protected Object body;

    protected ResourcesImpl(
            BeanManager bm,
            Class<T> resourceClass,
            Map<String, Object> params) {
        this.bm = bm;
        this.params = params;
        this.resourceClass = resourceClass;
    }

    public ResourcesImpl(
            BeanManager bm, Class<T> resourceClass) {
        this(bm, resourceClass, new HashMap<String, Object>());

    }

    @Override
    public Resources<T> select(
            Map<String, Object> params) {
        return new ResourcesWithParametersImpl<>(bm, resourceClass, params);
    }

    @Override
    public Resources<T> select(String paramName, Object paramValue) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(paramName, paramValue);
        return select(parameters);
    }

    @Override
    public Resources<T> body(Object requestBody) {
        this.body = requestBody;
        return this;
    }

    @Override
    public T invoke(final String methodName) throws Exception {
        HttpContextProducer.setRequest((HttpServletRequest) Proxy
                .newProxyInstance(ResourcesImpl.class
                .getClassLoader(), new Class[]{HttpServletRequest.class},
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                if ("getMethod".equals(method.getName())) {
                    return methodName;
                }
                throw new UnsupportedOperationException(String.format(
                        "method %s is not supported",
                        method.getName()));
            }
        }));

        return lookupExecutor(bm, methodName)
                .execute(resourceClass, params, body);
    }

    @Override
    public void call(final String methodName) throws Exception {
        HttpContextProducer.setRequest((HttpServletRequest) Proxy
                .newProxyInstance(ResourcesImpl.class
                .getClassLoader(), new Class[]{HttpServletRequest.class},
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                if ("getMethod".equals(method.getName())) {
                    return methodName;
                }
                throw new UnsupportedOperationException(String.format(
                        "method %s is not supported",
                        method.getName()));
            }
        }));

        lookupExecutor(bm, methodName)
                .execute(Object.class, params, body);
    }

    private static ResourceMethodHandler lookupExecutor(BeanManager bm,
            final String methodName) {
      final Set<Bean<?>> beans =
                bm.getBeans(ResourceMethodHandler.class,
                new VerbLiteral(methodName));
        System.out.println(methodName + " " + beans.size());
        Bean<ResourceMethodHandler> b = (Bean<ResourceMethodHandler>) bm
                .resolve(
                beans);
        System.out.println(b);
        return b.create(bm
                .createCreationalContext(b));
    }
}
