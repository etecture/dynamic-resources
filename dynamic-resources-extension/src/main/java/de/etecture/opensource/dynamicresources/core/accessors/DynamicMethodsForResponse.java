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
package de.etecture.opensource.dynamicresources.core.accessors;

import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodsForResponse;
import de.etecture.opensource.dynamicresources.api.accesspoints.Responses;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.utils.BeanInstanceBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
class DynamicMethodsForResponse<T> implements MethodsForResponse<T> {

    private final Map<String, String> pathParameters = new HashMap<>();
    private final Resource resource;
    private final Class<T> responseType;
    @Inject
    BeanManager beanManager;

    private DynamicMethodsForResponse(
            Class<T> responseType, Resource resource) {
        this.resource = resource;
        this.responseType = responseType;
    }

    @Override
    public Class<T> getSelectedResponseType() {
        return responseType;
    }

    @Override
    public Resource getMetadata() {
        return resource;
    }

    @Override
    public <R> MethodsForResponse<R> select(
            Class<R> responseType) throws ResponseTypeNotSupportedException {
        // check the response type
        for (ResourceMethod method : resource.getMethods().values()) {
            if (method.getResponses().containsKey(responseType)) {
                return new DynamicMethodsForResponse(responseType, resource)
                        .pathParams(pathParameters);
            }
        }
        throw new ResponseTypeNotSupportedException(resource, responseType);
    }

    @Override
    public MethodsForResponse<T> pathParam(String paramName, String paramValue) {
        this.pathParameters.put(paramName, paramValue);
        return this;
    }

    @Override
    public MethodsForResponse<T> pathParams(
            Map<String, String> params) {
        this.pathParameters.putAll(params);
        return this;
    }

    @Override
    public Responses<T> method(String methodName) throws
            ResourceMethodNotFoundException {
        ResourceMethod method = resource.getMethod(methodName);
        if (method == null) {
            throw new ResourceMethodNotFoundException(resource, methodName);
        } else {
            // check the type...
            if (method.getResponses().containsKey(responseType)) {
                return DynamicResponses.create(beanManager,
                        (ResourceMethodResponse<T>) method.getResponses().get(
                        responseType), pathParameters);
            } else {
                throw new ResourceMethodNotFoundException(resource, methodName,
                        new ResponseTypeNotSupportedException(method,
                        responseType));
            }
        }
    }

    @Override
    public Response<T> invoke(String method) throws ResourceException {
        return method(method).invoke();
    }

    @Override
    public T get() throws ResourceException {
        return method("GET").invoke().getEntity();
    }

    @Override
    public T delete() throws ResourceException {
        return method("DELETE").invoke().getEntity();
    }

    @Override
    public boolean remove() throws ResourceException {
        return method("DELETE").invoke().getStatus() == StatusCodes.OK;
    }

    @Override
    public T put() throws ResourceException {
        return method("PUT").invoke().getEntity();
    }

    @Override
    public T post() throws ResourceException {
        return method("POST").invoke().getEntity();
    }

    @Override
    public T put(Object requestBody) throws ResourceException {
        return method("PUT").body(requestBody).invoke().getEntity();
    }

    @Override
    public T post(Object requestBody) throws ResourceException {
        return method("POST").body(requestBody).invoke().getEntity();
    }

    static <T> DynamicMethodsForResponse<T> create(
            BeanManager beanManager, Class<T> responseType, Resource resource)
            throws InjectionException {
        try {
            return BeanInstanceBuilder.forBeanType(
                    DynamicMethodsForResponse.class,
                    beanManager).build(responseType, resource);
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException ex) {
            throw new InjectionException(ex);
        }
    }
}
