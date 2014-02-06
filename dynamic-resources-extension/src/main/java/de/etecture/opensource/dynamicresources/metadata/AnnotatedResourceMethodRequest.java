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

import de.etecture.opensource.dynamicresources.api.Consumes;
import de.etecture.opensource.dynamicresources.api.Filter;
import de.etecture.opensource.dynamicresources.api.Method;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.api.metadata.ResourceMethodRequestFilter;
import de.etecture.opensource.dynamicresources.api.metadata.ResourceMethodResponse;
import de.etecture.opensource.dynamicresources.extension.MediaTypeExpression;
import de.etecture.opensource.dynamicresources.extension.RequestReaderResolver;
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
public class AnnotatedResourceMethodRequest implements ResourceMethodRequest {

    private final Class<?> requestType;
    private final Set<ResourceMethodResponse> responses = new HashSet<>();
    private final Set<ResourceMethodRequestFilter> filters = new HashSet<>();
    private final Set<MediaType> mediaTypes = new HashSet<>();

    public AnnotatedResourceMethodRequest(Class<?> resourceClass, Method method,
            ResponseWriterResolver writers,
            RequestReaderResolver readers) {
        this.requestType = resourceClass;
        if (method.produces().length > 0) {
            for (Produces produces : method.produces()) {
                this.responses.add(new AnnotatedResourceMethodResponse(
                        resourceClass, method, produces, writers));
            }
        } else {
            this.responses.add(
                    new AnnotatedResourceMethodResponse(resourceClass, method,
                    writers));
        }
        for (Filter filter : method.filters()) {
            this.filters.add(new AnnotatedResourceMethodRequestFilter(filter));
        }
        for (MediaType mediaType : readers.getAvailableFormats(this.requestType)
                .keySet()) {
            this.mediaTypes.add(mediaType);
        }
    }

    public AnnotatedResourceMethodRequest(Class<?> resourceClass, Method method,
            Consumes consumes,
            ResponseWriterResolver writers,
            RequestReaderResolver readers) {
        this((consumes.requestType() == null ? resourceClass : consumes
                .requestType()), method, writers, readers);
        for (String mimeType : consumes.mimeType()) {
            this.mediaTypes.add(new MediaTypeExpression(mimeType));
        }
    }

    @Override
    public Class<?> getRequestType() {
        return requestType;
    }

    @Override
    public Set<ResourceMethodRequestFilter> getFilters() {
        return Collections.unmodifiableSet(filters);
    }

    @Override
    public Set<ResourceMethodResponse> getResponses() {
        return Collections.unmodifiableSet(responses);
    }

    @Override
    public Set<MediaType> getRequestMediaTypes() {
        return Collections.unmodifiableSet(mediaTypes);
    }
}
