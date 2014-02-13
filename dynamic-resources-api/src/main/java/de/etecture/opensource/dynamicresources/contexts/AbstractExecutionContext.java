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
package de.etecture.opensource.dynamicresources.contexts;

import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public abstract class AbstractExecutionContext<R, B> implements
        ExecutionContext<R, B> {

    private final ResourceMethodRequest<B> requestMetadata;
    private final ResourceMethodResponse<R> responseMetadata;
    private final ResourceMethod resourceMethod;
    private final Map<String, Object> parameters = new HashMap<>();
    private final B body;

    protected AbstractExecutionContext(
            ResourceMethodResponse<R> responseMetadata,
            ResourceMethodRequest<B> requestMetadata) {
        this(responseMetadata, requestMetadata, null);
    }

    protected AbstractExecutionContext(
            ResourceMethodResponse<R> responseMetadata,
            ResourceMethodRequest<B> requestMetadata,
            B body) {
        this(responseMetadata, requestMetadata, body, Collections
                .<String, Object>emptyMap());
    }

    protected AbstractExecutionContext(
            ResourceMethodResponse<R> responseMetadata,
            ResourceMethodRequest<B> requestMetadata,
            B body, Map<String, Object> parameters) {
        this.requestMetadata = requestMetadata;
        if (responseMetadata == null) {
            throw new IllegalArgumentException(
                    "responsemetadata may not be null");
        }
        this.responseMetadata = responseMetadata;
        this.resourceMethod = this.responseMetadata.getMethod();
        this.body = body;
        this.parameters.putAll(parameters);
    }

    @Override
    public ResourceMethodRequest<B> getRequestMetadata() {
        return requestMetadata;
    }

    @Override
    public ResourceMethodResponse<R> getResponseMetadata() {
        return responseMetadata;
    }

    @Override
    public ResourceMethod getResourceMethod() {
        return resourceMethod;
    }

    @Override
    public Set<String> getParameterNames() {
        return Collections.unmodifiableSet(parameters.keySet());
    }

    @Override
    public <T> T getParameterValue(String name, T defaultValue) {
        Object value = getParameterValue(name);
        if (value == null) {
            return defaultValue;
        } else {
            return (T) value;
        }
    }

    @Override
    public Object getParameterValue(String name) {
        return parameters.get(name);
    }

    @Override
    public void setParameterValue(String name, Object value) {
        this.parameters.put(name, value);
    }

    @Override
    public boolean hasParameter(String name) {
        return this.parameters.containsKey(name);
    }

    @Override
    public void removeParameter(String name) {
        this.parameters.remove(name);
    }

    @Override
    public B getBody() {
        return body;
    }
}
