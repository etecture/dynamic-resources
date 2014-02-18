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

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.TypedResourceAccessor;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeAmbigiousException;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotAllowedException;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class DynamicResourceAccessor implements ResourceAccessor,
        DynamicAccessPoint<Resource> {

    private Resource resource;
    private final Map<String, String> pathParams = new HashMap<>();
    @Inject
    DynamicAccessPoints accessPoints;

    @Override
    public void init(Resource resource, Object... args) {
        this.resource = resource;
        this.pathParams.clear();
    }

    @Override
    public Resource getMetadata() {
        return resource;
    }

    @Override
    public <R> TypedResourceAccessor<R> select(
            Class<R> responseType) throws ResponseTypeNotSupportedException {
        // check the response type
        for (ResourceMethod method : resource.getMethods().values()) {
            if (method.getResponses().containsKey(responseType)) {
                return accessPoints.create(resource, responseType).pathParams(
                        pathParams);
            }
        }
        throw new ResponseTypeNotSupportedException(resource, responseType);
    }

    @Override
    public <R> TypedResourceAccessor<R> select(
            MediaType mediaType) throws MediaTypeNotAllowedException {
        // find the response type
        Class<R> responseType = null;
        for (ResourceMethod method : resource.getMethods().values()) {
            try {
                responseType = (Class<R>) method.getResponse(mediaType)
                        .getResponseType();
                continue;
            } catch (MediaTypeNotSupportedException |
                    MediaTypeAmbigiousException ex) {
            }
        }
        if (responseType != null) {
            return accessPoints.create(resource, responseType).pathParams(
                    pathParams);
        } else {
            throw new MediaTypeNotAllowedException(resource, mediaType);
        }
    }

    @Override
    public <R> Response<R> invoke(String method,
            Class<R> responseType) throws ResourceException {
        return method(method, responseType).invoke();
    }

    @Override
    public <R> MethodAccessor<R> method(String methodName,
            Class<R> responseType) throws ResponseTypeNotSupportedException,
            ResourceMethodNotFoundException {
        return select(responseType).pathParams(pathParams).method(methodName);
    }

    @Override
    public ResourceAccessor pathParam(String paramName, String paramValue) {
        this.pathParams.put(paramName, paramValue);
        return this;
    }

    @Override
    public ResourceAccessor pathParams(
            Map<String, String> params) {
        this.pathParams.putAll(params);
        return this;
    }

}
