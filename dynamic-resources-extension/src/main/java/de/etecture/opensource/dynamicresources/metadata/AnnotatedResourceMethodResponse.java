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

import de.etecture.opensource.dynamicresources.api.Header;
import de.etecture.opensource.dynamicresources.api.Method;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.metadata.ResourceMethodResponse;
import de.etecture.opensource.dynamicresources.api.metadata.ResourceMethodResponseHeader;
import de.etecture.opensource.dynamicresources.extension.MediaTypeExpression;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class AnnotatedResourceMethodResponse implements ResourceMethodResponse {

    private final Class<?> responseType;
    private final int statusCode;
    private final Set<ResourceMethodResponseHeader> headers = new HashSet<>();
    private final Set<MediaType> mediaTypes = new HashSet<>();

    public AnnotatedResourceMethodResponse(
            Class<?> resourceClass, Method method,
            ResponseWriterResolver writers) {
        this.responseType = resourceClass;
        this.statusCode = method.status();
        for (Header header : method.headers()) {
            this.headers.add(new AnnotatedResourceMethodResponseHeader(header));
        }
        for (MediaType mediaType : writers.getAvailableFormats(responseType)
                .keySet()) {
            this.mediaTypes.add(mediaType);
        }
    }

    public AnnotatedResourceMethodResponse(Class<?> resourceClass, Method method,
            Produces produces,
            ResponseWriterResolver writers) {
        this((produces.contentType() == null ? resourceClass : produces
                .contentType()), method, writers);
        for (String mimeType : produces.mimeType()) {
            this.mediaTypes.add(new MediaTypeExpression(mimeType));
        }
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public Set<ResourceMethodResponseHeader> getResponseHeaders() {
        return Collections.unmodifiableSet(headers);
    }

    @Override
    public Set<MediaType> getResponseMediaTypes() {
        return Collections.unmodifiableSet(mediaTypes);
    }
}
