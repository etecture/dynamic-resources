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
package de.etecture.opensource.dynamicresources.metadata.annotated;

import de.etecture.opensource.dynamicresources.annotations.Consumes;
import de.etecture.opensource.dynamicresources.annotations.Filter;
import de.etecture.opensource.dynamicresources.annotations.Header;
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Produces;
import de.etecture.opensource.dynamicresources.api.ResourceMethodInterceptor;
import de.etecture.opensource.dynamicresources.core.mapping.mime.MediaTypeExpression;
import de.etecture.opensource.dynamicresources.metadata.AbstractResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.BasicResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.Resource;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class AnnotatedResourceMethod extends AbstractResourceMethod {

    AnnotatedResourceMethod(Resource resource,
            Method annotation) {
        super(resource, annotation.name(), annotation.description());
    }

    public static AnnotatedResourceMethod create(Resource resource,
            Class<?> resourceClass, Method annotation,
            Iterable<String> producedMimes, Iterable<String> consumedMimes) {
        AnnotatedResourceMethod method = new AnnotatedResourceMethod(resource,
                annotation);
        for (String roleName : annotation.rolesAllowed()) {
            method.addAllowedRoleName(roleName);
        }
        for (Class<? extends ResourceMethodInterceptor> interceptorType
                : annotation.interceptors()) {
            method.addInterceptor(interceptorType);
        }
        for (Filter filter : annotation.filters()) {
            method.addFilter(AnnotatedResourceMethodFilter
                    .create(method, filter));
        }
        if (annotation.consumes().length > 0) {
            for (Consumes consumes : annotation.consumes()) {
                BasicResourceMethodRequest request =
                        new BasicResourceMethodRequest(method, consumes
                        .requestType());
                for (String mime : consumes.mimeType()) {
                    request.addAcceptedRequestMediaType(new MediaTypeExpression(
                            mime));
                }
                method.addRequest(request);
            }
        } else {
            if (consumedMimes != null) {
                // add at least the resourceClass as a requestType.
                final BasicResourceMethodRequest request =
                        new BasicResourceMethodRequest(method, resourceClass);
                for (String consumedMime : consumedMimes) {
                    request.addAcceptedRequestMediaType(new MediaTypeExpression(
                            consumedMime));
                }
                method.addRequest(request);
            }
        }
        if (annotation.produces().length > 0) {
            for (Produces produces : annotation.produces()) {
                AnnotatedQueryResourceMethodResponse response =
                        AnnotatedQueryResourceMethodResponse
                        .createWithQueryExecution(method, produces.contentType(),
                        annotation);
                for (String mime : produces.mimeType()) {
                    response.addSupportedResponseMediaType(
                            new MediaTypeExpression(
                            mime));
                }
                for (Header header : annotation.headers()) {
                    response.addHeader(
                            new AnnotatedResourceMethodResponseHeader(
                            header));
                }
                method.addResponse(response);
            }
        } else {
            // add at least the resource class itself as a response
            AnnotatedResourceMethodResponse response =
                    AnnotatedResourceMethodResponse
                    .createWithQueryExecution(method, resourceClass,
                    annotation);
            for (Header header : annotation.headers()) {
                response.addHeader(
                        new AnnotatedResourceMethodResponseHeader(
                        header));
            }
            if (producedMimes != null) {
                for (String producedMime : producedMimes) {
                    response.addSupportedResponseMediaType(
                            new MediaTypeExpression(
                            producedMime));
                }
            } else {
                response.addSupportedResponseMediaType(new MediaTypeExpression(
                        "text/plain"));
            }
            method.addResponse(response);

        }
        return method;
    }
}
