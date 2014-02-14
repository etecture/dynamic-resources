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
package de.etecture.opensource.dynamicresources.utils;

import de.etecture.opensource.dynamicresources.annotations.Executes;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class ExecutesLiteral extends AnnotationLiteral<Executes> implements
        Executes {

    private static final long serialVersionUID = 1L;
    private final String method;
    private final String resource;
    private final String application;
    private final Class<?> responseType, requestType;

    public ExecutesLiteral(
            String method,
            String resource, String application,
            Class<?> responseType,
            Class<?> requestType) {
        this.method = method;
        this.resource = resource;
        this.application = application;
        this.responseType = responseType;
        this.requestType = requestType;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String resource() {
        return resource;
    }

    @Override
    public String application() {
        return application;
    }

    @Override
    public Class<?> responseType() {
        return responseType;
    }

    @Override
    public Class<?> requestType() {
        return requestType;
    }

    public static class Builder {

        private String method;
        private String resource;
        private String application;
        private Class<?> responseType, requestType;

        public Builder forMethodWithNameMatching(final String methodNamePattern) {
            this.method = methodNamePattern;
            return this;
        }

        public Builder forResourceWithNameMatching(
                final String resourceNamePattern) {
            this.resource = resourceNamePattern;
            return this;
        }

        public Builder forApplicationWithNameMatching(
                final String applicationNamePattern) {
            this.application = applicationNamePattern;
            return this;
        }

        public Builder forExactMethodResponseAndRequest(
                final ResourceMethodResponse response,
                final ResourceMethodRequest request) {
            if (request != null) {
                this.requestType = request.getRequestType();
            }
            return this.forExactMethodResponse(response);
        }

        public Builder forExactMethodResponse(
                final ResourceMethodResponse response) {
            this.responseType = response.getResponseType();
            return this.forExactMethod(response.getMethod());
        }

        public Builder forExactMethod(final ResourceMethod method) {
            this.method = method.getName();
            return this.forExactResource(method.getResource());
        }

        public Builder forExactResource(final Resource resource) {
            this.resource = resource.getName();
            return this.forExactApplication(resource.getApplication());
        }

        public Builder forExactApplication(final Application application) {
            this.application = application.getName();
            return this;
        }

        public Builder forResponseType(
                final Class<?> responseType) {
            this.responseType = responseType;
            return this;
        }

        public Builder forRequestType(
                final Class<?> requestType) {
            this.requestType = requestType;
            return this;
        }

        public ExecutesLiteral build() {
            return new ExecutesLiteral(method, resource,
                    application,
                    responseType, requestType);
        }
    }

    /**
     * starts the creation of the {@link ExecutesLiteral} by using the
     * {@link Builder}.
     *
     * @return
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * creates an {@link ExecutesLiteral} from the given
     * {@link ExecutionContext}
     *
     * @param context
     * @return
     */
    public static ExecutesLiteral create(ExecutionContext<?, ?> context) {
        return create()
                .forExactMethodResponseAndRequest(
                context.getResponseMetadata(),
                context.getRequestMetadata())
                .build();
    }

    public static boolean matches(Executes checkFor, Executes possibleMatch) {
        if (!checkFor.application().matches(possibleMatch.application())) {
            return false;
        }
        if (!checkFor.resource().matches(possibleMatch.resource())) {
            return false;
        }
        if (!checkFor.method().matches(possibleMatch.method())) {
            return false;
        }
        if (!possibleMatch.responseType().isAssignableFrom(checkFor
                .responseType())) {
            return false;
        }
        if (!possibleMatch.requestType()
                .isAssignableFrom(checkFor.requestType())) {
            return false;
        }
        return true;
    }
}
