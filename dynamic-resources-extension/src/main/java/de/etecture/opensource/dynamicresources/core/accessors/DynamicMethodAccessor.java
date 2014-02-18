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
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.TypedResourceAccessor;
import de.etecture.opensource.dynamicresources.core.executors.ResourceMethodExecutions;
import de.etecture.opensource.dynamicresources.metadata.RequestTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class DynamicMethodAccessor<R, B> implements MethodAccessor<R>,
        DynamicAccessPoint<ResourceMethodResponse<R>> {

    private ResourceMethodResponse<R> resourceMethodResponse;
    private final Map<String, Object> parameter = new HashMap<>();
    private B requestBody;
    private int expectedStatusCode = -1; // all status codes are acceptable
    @Inject
    DynamicAccessPoints accessPoints;
    @Inject
    ResourceMethodExecutions executions;

    @Override
    public void init(
            ResourceMethodResponse<R> metadata, Object... args) {
        this.resourceMethodResponse = metadata;
        parameter.clear();
        parameter.putAll((Map<String, String>) args[0]);
    }

    @Override
    public ResourceMethodResponse<R> getMetadata() {
        return resourceMethodResponse;
    }

    @Override
    public TypedResourceAccessor<R> methods() {
        Resource resource = resourceMethodResponse.getMethod().getResource();
        return accessPoints.create(resource, resourceMethodResponse
                .getResponseType());
    }

    @Override
    public MethodAccessor<R> queryParam(String name, Object... values) {
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
    public MethodAccessor<R> body(Object body) throws
            RequestTypeNotSupportedException {
        if (body != null) {
            if (!resourceMethodResponse.getMethod().getRequests()
                    .containsKey(requestBody.getClass())) {
                throw new RequestTypeNotSupportedException(
                        resourceMethodResponse.getMethod(), requestBody
                        .getClass());
            }
        }
        this.requestBody = (B) body;
        return this;
    }

    @Override
    public MethodAccessor<R> expect(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
        return this;
    }

    @Override
    public Response<R> invoke() throws ResourceException {
        ResourceMethodRequest requestMetadata;
        if (requestBody == null) {
            requestMetadata = null;
        } else {
            requestMetadata = resourceMethodResponse.getMethod().getRequests()
                    .get(requestBody.getClass());
        }
        return executions.execute(resourceMethodResponse, requestMetadata,
                requestBody,
                parameter);
    }

    @Override
    public R invokeAndCheck() throws ResourceException {
        Response<R> response = invoke();
        if (expectedStatusCode == -1 || response.getStatus()
                == expectedStatusCode) {
            Object entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            if (resourceMethodResponse.getResponseType().isInstance(entity)) {
                return resourceMethodResponse.getResponseType().cast(entity);
            } else if (entity instanceof String) {
                throw new ResourceException(((String) entity));
            } else {
                throw new ResourceException(String.format(
                        "Returned entity of %s %s is not the expected type. Instead it is: %s",
                        resourceMethodResponse.getMethod().getName(),
                        resourceMethodResponse.getMethod().getResource()
                        .getName(),
                        entity.getClass().getName()));
            }
        } else {
            throw new ResourceException(String.format(
                    "Response of %s %s is not of expected status: %s. Actual status is: %s",
                    resourceMethodResponse.getMethod().getName(),
                    resourceMethodResponse.getMethod().getResource().getName(),
                    expectedStatusCode,
                    response.getStatus()));
        }
    }

}
