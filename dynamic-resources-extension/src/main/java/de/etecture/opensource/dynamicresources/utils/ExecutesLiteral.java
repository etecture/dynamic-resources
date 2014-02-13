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
import de.etecture.opensource.dynamicresources.contexts.ExecutionContext;
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
    private final Class<? extends ExecutionContext<?, ?>> contextType;
    private final String method;
    private final String resource;
    private final String application;
    private final Class<?> responseType, requestType;

    public ExecutesLiteral(
            Class<? extends ExecutionContext<?, ?>> contextType, String method,
            String resource, String application,
            Class<?> responseType,
            Class<?> requestType) {
        this.contextType = contextType;
        this.method = method;
        this.resource = resource;
        this.application = application;
        this.responseType = responseType;
        this.requestType = requestType;
    }

    @Override
    public Class<? extends ExecutionContext<?, ?>> contextType() {
        return contextType;
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

        private Class<? extends ExecutionContext<?, ?>> contextType;
        private String method;
        private String resource;
        private String application;
        private Class<?> responseType, requestType;

        public Builder withContextType(
                final Class<? extends ExecutionContext<?, ?>> contextType) {
            this.contextType = contextType;
            return this;
        }

        public Builder withMethod(final String method) {
            this.method = method;
            return this;
        }

        public Builder withResource(final String resource) {
            this.resource = resource;
            return this;
        }

        public Builder withApplication(final String application) {
            this.application = application;
            return this;
        }

        public Builder withResponseType(
                final Class<?> responseType) {
            this.responseType = responseType;
            return this;
        }

        public Builder withRequestType(
                final Class<?> requestType) {
            this.requestType = requestType;
            return this;
        }

        public ExecutesLiteral build() {
            return new ExecutesLiteral(contextType, method, resource,
                    application,
                    responseType, requestType);
        }
    }

    public static Builder create() {
        return new Builder();
    }
}
