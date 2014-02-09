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

import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Resources;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
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
    protected final Map<String, String> pathParams;
    protected final Map<String, String[]> queryParams;
    protected Object body;
    private int expectedStatus;

    protected ResourcesImpl(
            BeanManager bm,
            Class<T> resourceClass,
            Object body,
            int expectedStatus,
            Map<String, String> pathParams, Map<String, String[]> queryParams) {
        this.bm = bm;
        this.body = body;
        this.expectedStatus = expectedStatus;
        this.pathParams = pathParams;
        this.queryParams = queryParams;
        this.resourceClass = resourceClass;
    }

    public ResourcesImpl(
            BeanManager bm, Class<T> resourceClass) {
        this(bm, resourceClass, null, -1, new HashMap<String, String>(),
                new HashMap<String, String[]>());

    }

    @Override
    public Resources<T> withPathParam(String paramName, String paramValue) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(paramName, paramValue);
        return new ResourcesWithParametersImpl(bm, resourceClass, body,
                expectedStatus, parameters, new HashMap<String, String[]>());
    }

    @Override
    public Resources<T> withQueryParam(String paramName, String... paramValue) {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put(paramName, paramValue);
        return new ResourcesWithParametersImpl(bm, resourceClass, body,
                expectedStatus, new HashMap<String, String>(), parameters);
    }

    @Override
    public Resources<T> expect(int expectedStatusCode) {
        this.expectedStatus = expectedStatusCode;
        return this;
    }

    @Override
    public Resources<T> body(Object requestBody) {
        this.body = requestBody;
        return this;
    }

    @Override
    public Response<T> invoke(final String methodName) throws ResourceException {
        final HttpServletRequest req =
                (HttpServletRequest) Proxy
                .newProxyInstance(ResourcesImpl.class
                .getClassLoader(), new Class[]{HttpServletRequest.class},
                new InvocationHandler() {
            @Override
                    public Object invoke(Object proxy, Method method,
                            Object[] args)
                            throws ResourceException {
                if ("getMethod".equals(method.getName())) {
                    return methodName;
                } else if ("toString".equals(method.getName())) {
                    return "dummy-reqeust to execute " + methodName + " on "
                            + resourceClass.getSimpleName();
                }
                throw new UnsupportedOperationException(String.format(
                        "method %s is not supported",
                        method.getName()));
            }
        });
        HttpContextProducer.setRequest(req);
        DefaultRequest.Builder<T> builder = DefaultRequest
                .fromMethod(resourceClass, methodName)
                .addPathParameter(pathParams)
                .addQueryParameter(queryParams)
                .withRequestContent(body);
        return lookupExecutor(bm, methodName)
                .handleRequest(builder.build());
    }

    private static ResourceMethodHandler lookupExecutor(BeanManager bm,
            final String methodName) {
        final Set<Bean<?>> beans =
                bm.getBeans(ResourceMethodHandler.class,
                new VerbLiteral(methodName));
        Bean<ResourceMethodHandler> b = (Bean<ResourceMethodHandler>) bm
                .resolve(
                beans);
        return b.create(bm
                .createCreationalContext(b));
    }

    private T invokeAndCheck(String method) throws ResourceException {
        Response<T> response = invoke(method);
        if (expectedStatus == -1 || response.getStatus() == expectedStatus) {
            Object entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            if (resourceClass.isInstance(entity)) {
                return resourceClass.cast(entity);
            } else if (entity instanceof String) {
                throw new ResourceException(((String) entity));
            } else {
                throw new ResourceException(String.format(
                        "Returned entity of %s %s is not the expected type. Instead it is: %s",
                        method, resourceClass.getSimpleName(),
                        entity.getClass().getName()));
            }
        } else {
            throw new ResourceException(String.format(
                    "Response of %s %s is not of expected status: %s. Actual status is: %s",
                    method, resourceClass.getSimpleName(), expectedStatus,
                    response.getStatus()));
        }
    }

    @Override
    public T get() throws ResourceException {
        return invokeAndCheck("GET");
    }

    @Override
    public T delete() throws ResourceException {
        return invokeAndCheck("DELETE");
    }

    @Override
    public boolean remove() throws ResourceException {
        return invoke("DELETE").getStatus() == StatusCodes.OK;
    }

    @Override
    public T put() throws ResourceException {
        return invokeAndCheck("PUT");
    }

    @Override
    public T post() throws ResourceException {
        return invokeAndCheck("POST");
    }

    @Override
    public T put(Object requestBody) throws ResourceException {
        return body(requestBody).put();
    }

    @Override
    public T post(Object requestBody) throws ResourceException {
        return body(requestBody).post();
    }
}
