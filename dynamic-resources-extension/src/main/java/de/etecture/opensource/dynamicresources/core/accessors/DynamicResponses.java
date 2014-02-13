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

import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodsForResponse;
import de.etecture.opensource.dynamicresources.api.accesspoints.Responses;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import de.etecture.opensource.dynamicresources.utils.BeanInstanceBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
class DynamicResponses<R, B> implements Responses<R>, Request<R, B> {

    private final ResourceMethodResponse<R> resourceMethodResponse;
    private final Map<String, Object> parameter = new HashMap<>();
    private B requestBody;
    private int expectedStatusCode = -1; // all status codes are acceptable
    @Inject
    BeanManager beanManager;

    private DynamicResponses(
            ResourceMethodResponse<R> resourceMethodResponse,
            Map<String, String> pathParameters) {
        this.resourceMethodResponse = resourceMethodResponse;
        parameter.putAll(pathParameters);
    }

    @Override
    public ResourceMethodResponse<R> getResponseMetadata() {
        return resourceMethodResponse;
    }

    @Override
    public ResourceMethodResponse<R> getMetadata() {
        return resourceMethodResponse;
    }

    @Override
    public MethodsForResponse<R> methods() {
        Resource resource = resourceMethodResponse.getMethod().getResource();
        return DynamicMethodsForResponse.create(beanManager,
                resourceMethodResponse
                .getResponseType(),
                resource);
    }

    @Override
    public Responses<R> queryParam(String name, Object... values) {
        Object current = parameter.get(name);
        List<Object> valueList;
        if (current != null && current instanceof List) {
            valueList = (List<Object>) current;
        } else {
            valueList = new ArrayList<>();
            if (current != null) {
                valueList.add(current);
            }
            parameter.put(name, valueList);
        }
        valueList.addAll(Arrays.asList(values));
        return this;
    }

    @Override
    public Responses<R> body(Object body) {
        this.requestBody = (B) body;
        return this;
    }

    @Override
    public Responses<R> expect(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
        return this;
    }

    @Override
    public Set<String> getParameterNames() {
        return Collections.unmodifiableSet(parameter.keySet());
    }

    @Override
    public <T> T getParameterValue(String name, T defaultValue) {
        Object parameterValue = parameter.get(name);
        if (parameterValue == null) {
            return defaultValue;
        } else {
            return (T) parameterValue;
        }
    }

    @Override
    public Object getParameterValue(String name) {
        return parameter.get(name);
    }

    @Override
    public void setParameterValue(String name, Object value) {
        parameter.put(name, value);
    }

    @Override
    public boolean hasParameter(String name) {
        return parameter.containsKey(name);
    }

    @Override
    public void removeParameter(String name) {
        parameter.remove(name);
    }

    @Override
    public B getBody() {
        return requestBody;
    }

    @Override
    public ResourceMethodRequest<B> getRequestMetadata() {
        if (requestBody != null) {
            return (ResourceMethodRequest<B>) resourceMethodResponse.getMethod()
                    .getRequests().get(requestBody.getClass());
        }
        return null;
    }

    @Override
    public Response<R> invoke() throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public R invokeAndCheck() throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    static <R> DynamicResponses<R, Object> create(BeanManager beanManager,
            ResourceMethodResponse<R> resourceMethodResponse,
            Map<String, String> pathParameters)
            throws InjectionException {
        try {
            return BeanInstanceBuilder.forBeanType(
                    DynamicResponses.class,
                    beanManager).build(resourceMethodResponse, pathParameters);
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException ex) {
            throw new InjectionException(ex);
        }
    }
}
