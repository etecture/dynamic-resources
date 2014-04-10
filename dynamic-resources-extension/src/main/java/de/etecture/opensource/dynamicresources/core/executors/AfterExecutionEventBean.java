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
package de.etecture.opensource.dynamicresources.core.executors;

import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.HeaderValueGenerator;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ResponseException;
import de.etecture.opensource.dynamicresources.api.events.AfterExecutionEvent;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponseHeader;
import de.etecture.opensource.dynamicresources.utils.HeaderValueMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

class AfterExecutionEventBean implements
        AfterExecutionEvent {

    private final HeaderValueMap headers = new HeaderValueMap();
    private ExecutionContext<?, ?> context;
    private Object originalEntity;
    private Object currentEntity;
    private int currentStatus;
    @Inject
    Instance<HeaderValueGenerator> generators;

    void init(ExecutionContext<?, ?> context, Object originalEntity) {
        this.context = context;
        // process the original response headers
        for (ResourceMethodResponseHeader h : context.getResponseMetadata()
                .getResponseHeaders()) {
            // create the generator.
            this.headers.add(h.getName(), h.getType(), generators.select(h
                    .getGenerator()).get().generateHeaderValue(h, context));
        }
        if (originalEntity instanceof Response) {
            this.currentStatus = ((Response<?>) originalEntity).getStatus();
            try {
                this.originalEntity = ((Response<?>) originalEntity).getEntity();
            } catch (ResponseException ex) {
                this.originalEntity = ex;
            }
            this.currentEntity = this.originalEntity;
            for (Entry<String, List<Object>> e : ((Response<?>) originalEntity)
                    .getHeaders()) {
                this.headers.addAll(e.getKey(),
                        ResourceMethodResponseHeader.Type.DEFAULT, e.getValue());
            }

        } else {
            this.originalEntity = originalEntity;
            this.currentEntity = originalEntity;
            this.currentStatus = context.getResponseMetadata().getStatusCode();

        }
    }

    @Override
    public ExecutionContext<?, ?> getExecutionContext() {
        return this.context;
    }

    @Override
    public Object getOriginalEntity() {
        return this.originalEntity;
    }

    @Override
    public void setNewEntity(Object entity) {
        this.currentEntity = entity;
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.currentStatus = statusCode;
    }

    @Override
    public Object getEntity() throws ResponseException {
        return this.currentEntity;
    }

    @Override
    public int getStatus() {
        return this.currentStatus;
    }

    @Override
    public final void addHeaderValue(String name, String value) {
        headers.add(name, ResourceMethodResponseHeader.Type.DEFAULT, value);
    }

    @Override
    public final void addHeaderValue(String name, Date date) {
        headers.add(name, ResourceMethodResponseHeader.Type.DATE, date);
    }

    @Override
    public final void addHeaderValue(String name, Number number) {
        headers.add(name, ResourceMethodResponseHeader.Type.INTEGER, number);
    }

    @Override
    public final void setHeaderValue(String name, String value) {
        headers.set(name, ResourceMethodResponseHeader.Type.DEFAULT, Arrays
                    .asList(value));
    }

    @Override
    public final void setHeaderValue(String name, Date date) {
        headers.set(name, ResourceMethodResponseHeader.Type.DATE, Arrays.asList(
                    date));
    }

    @Override
    public final void setHeaderValue(String name, Number number) {
        headers.set(name, ResourceMethodResponseHeader.Type.INTEGER, Arrays
                    .asList(number));
    }

    @Override
    public List<Object> getHeader(String headerName) {
        if (headers.containsKey(headerName)) {
            return Collections.unmodifiableList(headers.get(headerName));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<Map.Entry<String, List<Object>>> getHeaders() {
        return headers.entrySet();
    }
}
