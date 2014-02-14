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

package de.etecture.opensource.dynamicresources.metadata;

import de.etecture.opensource.dynamicresources.api.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class BasicResourceMethodResponse<R> implements ResourceMethodResponse<R> {
    private final ResourceMethod method;
    private final Class<R> responseType;
    private final int statusCode;
    private final Set<MediaType> mediaTypes = new HashSet<>();
    private final Set<ResourceMethodResponseHeader> headers = new HashSet<>();

    public BasicResourceMethodResponse(ResourceMethod method,
            Class<R> responseType, int statusCode, MediaType... mediaTypes) {
        this.method = method;
        this.responseType = responseType;
        this.statusCode = statusCode;
        this.mediaTypes.addAll(Arrays.asList(mediaTypes));
    }

    public void addHeader(ResourceMethodResponseHeader header) {
        this.headers.add(header);
    }

    public void addSupportedResponseMediaType(MediaType mediaType) {
        this.mediaTypes.add(mediaType);
    }

    @Override
    public ResourceMethod getMethod() {
        return method;
    }

    @Override
    public Set<ResourceMethodResponseHeader> getResponseHeaders() {
        return Collections.unmodifiableSet(headers);
    }

    @Override
    public Class<R> getResponseType() {
        return responseType;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public Set<MediaType> getSupportedResponseMediaTypes() {
        return Collections.unmodifiableSet(mediaTypes);
    }

}
