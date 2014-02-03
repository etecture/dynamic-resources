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
package de.etecture.opensource.dynamicresources.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * represents the response object for a ReST request.
 *
 * @param <T>
 * @author rhk
 */
public class DefaultResponse<T> implements Response<T> {

    private final T entity;
    private final Throwable exception;
    private int status;
    private final Map<String, List<Object>> header = new HashMap<>();

    public DefaultResponse(T entity, int status) {
        this.entity = entity;
        this.status = status;
        this.exception = null;
    }

    public DefaultResponse(Class<T> responseType, Throwable exception) {
        this.entity = null;
        this.status = 500;
        this.exception = exception;
    }

    @Override
    public T getEntity() throws ResponseException {
        if (exception != null) {
            throw new ResponseException(exception);
        }
        return entity;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void addHeader(String headerName, Object value) {
        List<Object> values = header.get(headerName);
        if (values == null) {
            values = new ArrayList<>();
            header.put(headerName, values);
        }
        values.add(value);
    }

    @Override
    public List<Object> getHeader(String headerName) {
        if (header.containsKey(headerName)) {
            return Collections.unmodifiableList(header.get(headerName));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<Map.Entry<String, List<Object>>> getHeaders() {
        return header.entrySet();
    }
}
