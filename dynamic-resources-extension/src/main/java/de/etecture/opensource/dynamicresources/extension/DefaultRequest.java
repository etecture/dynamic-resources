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
package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Consumes;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Method;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.RequestReader;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.Version;
import de.etecture.opensource.dynamicresources.api.VersionNumberRange;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 * the request.
 *
 * @author rhk
 * @version
 * @since
 */
public class DefaultRequest<T> implements Request<T> {

    private final String methodName;
    private final MediaType acceptedMediaType, contentMediaType;
    private final VersionNumberRange acceptedVersionRange;
    private final Version contentVersion;
    private final Map<String, String[]> queryParameter;
    private final Map<String, String> pathParameter;
    private final Map<String, Object> parameter;
    private final Class<T> resourceClass;
    private final Resource resource;
    private final Method resourceMethod;
    private final BufferedReader contentReader;
    private final RequestReaderResolver requestReaderResolver;
    private Object requestContent;
    private boolean wasRead = false;

    public DefaultRequest(String methodName, MediaType acceptedMediaType,
            MediaType contentMediaType, VersionNumberRange acceptedVersionRange,
            Version contentVersion,
            Map<String, String[]> queryParameter,
            Map<String, String> pathParameter,
            Map<String, Object> parameter,
            Class<T> resourceClass, Resource resource, Method resourceMethod,
            RequestReaderResolver requestReaderResolver,
            BufferedReader contentReader) {
        this.methodName = methodName;
        this.acceptedMediaType = acceptedMediaType;
        this.contentMediaType = contentMediaType;
        this.acceptedVersionRange = acceptedVersionRange;
        this.contentVersion = contentVersion;
        this.queryParameter = queryParameter;
        this.pathParameter = pathParameter;
        this.parameter = parameter;
        this.resourceClass = resourceClass;
        this.resource = resource;
        this.resourceMethod = resourceMethod;
        this.requestReaderResolver = requestReaderResolver;
        this.contentReader = contentReader;
    }

    public DefaultRequest(String methodName, MediaType acceptedMediaType,
            MediaType contentMediaType, VersionNumberRange acceptedVersionRange,
            Version contentVersion,
            Map<String, String[]> queryParameter,
            Map<String, String> pathParameter,
            Map<String, Object> parameter,
            Class<T> resourceClass, Resource resource, Method resourceMethod,
            Object content) {
        this.methodName = methodName;
        this.acceptedMediaType = acceptedMediaType;
        this.contentMediaType = contentMediaType;
        this.acceptedVersionRange = acceptedVersionRange;
        this.contentVersion = contentVersion;
        this.queryParameter = queryParameter;
        this.pathParameter = pathParameter;
        this.parameter = parameter;
        this.resourceClass = resourceClass;
        this.resource = resource;
        this.resourceMethod = resourceMethod;
        this.requestReaderResolver = null;
        this.contentReader = null;
        this.requestContent = content;
        this.wasRead = true;
    }

    private static boolean checkConsumes(MediaType contentMediaType,
            Version contentVersion,
            Method methodAnnotation) {
        if (methodAnnotation.consumes().length > 0) {
            for (Consumes consumes : methodAnnotation.consumes()) {
                // validate, that the consumes contains the desired type
                VersionNumberRange range;
                String version = consumes.version();
                if (StringUtils.isBlank(version)) {
                    range = new VersionNumberRangeExpression(0, 0, 0, true,
                            Integer.MAX_VALUE, Integer.MAX_VALUE,
                            Integer.MAX_VALUE, true);
                } else {
                    range = new VersionNumberRangeExpression(version);
                }
                if (range.includes(contentVersion)) {
                    if (consumes.mimeType().length == 0) {
                        return true;
                    } else if (contentMediaType.isCompatibleTo(consumes
                            .mimeType())) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    private static boolean checkProduces(MediaType acceptMediaType,
            VersionNumberRange acceptedRange,
            Method methodAnnotation) {
        if (methodAnnotation.produces().length > 0) {
            for (Produces produces : methodAnnotation.produces()) {
                // validate, that the consumes contains the desired type
                if (produces.mimeType().length == 0) {
                    return true;
                }
                if (acceptMediaType.isCompatibleTo(produces
                        .mimeType()) && acceptedRange.includes(produces
                        .version())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public Class<?> getRequestType() {
        if (this.resourceMethod.consumes().length > 0) {
            for (Consumes consumes : this.resourceMethod.consumes()) {
                // validate, that the consumes contains the desired type
                VersionNumberRange range;
                String version = consumes.version();
                if (StringUtils.isBlank(version)) {
                    range = new VersionNumberRangeExpression(0, 0, 0, true,
                            Integer.MAX_VALUE, Integer.MAX_VALUE,
                            Integer.MAX_VALUE, true);
                } else {
                    range = new VersionNumberRangeExpression(version);
                }
                if (range.includes(contentVersion)) {
                    if (consumes.mimeType().length == 0) {
                        return consumes.requestType();
                    } else if (contentMediaType.isCompatibleTo(consumes
                            .mimeType())) {
                        return consumes.requestType();
                    }
                }
            }
        }
        return this.resourceClass;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public MediaType getAcceptedMediaType() {
        return acceptedMediaType;
    }

    @Override
    public MediaType getContentMediaType() {
        return contentMediaType;
    }

    @Override
    public VersionNumberRange getAcceptedVersionRange() {
        return acceptedVersionRange;
    }

    @Override
    public Version getContentVersion() {
        return contentVersion;
    }

    @Override
    public Map<String, String[]> getQueryParameter() {
        return queryParameter;
    }

    @Override
    public Map<String, String> getPathParameter() {
        return pathParameter;
    }

    @Override
    public Map<String, Object> getParameter() {
        return parameter;
    }

    @Override
    public Class<T> getResourceClass() {
        return resourceClass;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public Method getResourceMethod() {
        return resourceMethod;
    }

    @Override
    public BufferedReader getContentReader() {
        return contentReader;
    }

    @Override
    public Object getContent() throws IOException {
        if (!wasRead) {
            wasRead = true;
            VersionNumberRange range;
            if (contentVersion == null) {
                range = new VersionNumberRangeExpression("(0.0.0,]");
            } else {
                range = new VersionNumberRangeExpression(contentVersion);
            }
            if (requestReaderResolver != null) {
                RequestReader reader = requestReaderResolver.resolve(
                        getRequestType(),
                        contentMediaType, range);

                if (reader != null) {
                    requestContent = reader.processRequest(contentReader,
                            contentMediaType.toString());
                } else {
                    requestContent = null;
                }
            } else {
                requestContent = null;
            }
        }
        return requestContent;
    }

    @Override
    public String getSingleQueryParameterValue(String name,
            String defaultValue) {
        String[] values = getQueryParameter().get(name);
        if (values != null && values.length > 0 && StringUtils.isNotBlank(
                values[0])) {
            return values[0];
        }
        return defaultValue;
    }

    @Override
    public boolean hasQueryParameterValue(String name) {
        String[] values = getQueryParameter().get(name);
        return (values != null && values.length > 0 && StringUtils.isNotBlank(
                values[0]));
    }

    public static <X> Builder<X> fromHttpRequest(HttpServletRequest req,
            Class<X> resourceClass) throws
            IOException {
        return new Builder(req, resourceClass);
    }

    public static <X> Builder<X> fromMethod(Class<X> resourceClass,
            String methodName) {
        return new Builder(resourceClass, methodName);
    }

    public static class Builder<T> {

        private final String methodName;
        private final MediaType acceptedMediaType, contentMediaType;
        private final VersionNumberRange acceptedVersionRange;
        private final Class<T> resourceClass;
        private final Version contentVersion;
        private final Resource resource;
        private final Method resourceMethod;
        private final Map<String, String[]> queryParameter = new HashMap<>();
        private final Map<String, String> pathParameter = new HashMap<>();
        private final Map<String, Object> parameter = new HashMap<>();
        private final BufferedReader contentReader;
        private RequestReaderResolver requestReaderResolver;
        private Object requestContent;

        private Builder(Class<T> resourceClass, String methodName,
                MediaType acceptedMediaType,
                VersionNumberRange acceptedVersionRange,
                MediaType contentMediaType,
                Version contentVersion, BufferedReader contentReader) {
            this.methodName = methodName;
            this.acceptedMediaType = acceptedMediaType;
            this.contentMediaType = contentMediaType;
            this.acceptedVersionRange = acceptedVersionRange;
            this.resourceClass = resourceClass;
            this.contentVersion = contentVersion;
            this.resource = resourceClass.getAnnotation(Resource.class);
            this.contentReader = contentReader;

            if (!HttpMethods.OPTIONS.equalsIgnoreCase(this.methodName)) {
                StringBuilder sb = new StringBuilder();
                for (Method methodAnnotation : this.resource.methods()) {
                    if (this.methodName
                            .equalsIgnoreCase(methodAnnotation.name())) {
                        if (!checkConsumes(this.contentMediaType,
                                this.contentVersion,
                                methodAnnotation)) {
                            sb.append("Consuming MediaTypes for method: ");
                            sb.append(methodAnnotation.name());
                            sb.append(" (");
                            sb.append(methodAnnotation.description());
                            sb.append(") is not compatible to: ");
                            sb.append(this.contentMediaType.toString());
                            sb.append(" version: ");
                            sb.append(this.contentVersion.toString());
                            sb.append("\n");
                            continue;
                        }
                        if (!checkProduces(this.acceptedMediaType,
                                this.acceptedVersionRange,
                                methodAnnotation)) {
                            sb.append("Accepted MediaTypes for method: ");
                            sb.append(methodAnnotation.name());
                            sb.append(" (");
                            sb.append(methodAnnotation.description());
                            sb.append(") is not compatible to: ");
                            sb.append(this.acceptedMediaType.toString());
                            sb.append(" version-range: ");
                            sb.append(this.acceptedVersionRange.toString());
                            sb.append("\n");
                            continue;
                        }
                        this.resourceMethod = methodAnnotation;
                        return;
                    }
                }
                if (sb.length() == 0) {
                    sb.append("No method definition found for resource: ");
                    sb.append(this.resourceClass.getSimpleName());
                    sb.append("\n");
                }
                throw new IllegalArgumentException(
                        "there is no method/resource found, that match the request. Reasons: \n"
                        + sb.toString());
            } else {
                this.resourceMethod = null;
            }
        }

        private static MediaType getAcceptedType(HttpServletRequest req) {
            String acceptType = req.getHeader("Accept");
            if (StringUtils.isBlank(acceptType)) {
                acceptType = "application/xml";
            }
            return new MediaTypeExpression(acceptType);
        }

        private static MediaType getContentType(HttpServletRequest req) {
            String contentType = req.getHeader("Content-Type");
            if (StringUtils.isBlank(contentType)) {
                contentType = "application/xml";
            }
            return new MediaTypeExpression(contentType);
        }

        private static VersionNumberRange getAcceptedVersionRange(
                HttpServletRequest req) {
            String acceptVersionString = req.getHeader("Accept-Version");
            if (StringUtils.isBlank(acceptVersionString)) {
                return new VersionNumberRangeExpression(
                        getAcceptedType(req)
                        .version());
            } else {
                return new VersionNumberRangeExpression(
                        acceptVersionString);
            }
        }

        private static Version getContentVersionRange(HttpServletRequest req) {
            String contentVersionString = req.getHeader("Content-Version");
            if (StringUtils.isBlank(contentVersionString)) {
                MediaType contentMediaType = getContentType(req);
                if (contentMediaType.version() != null) {
                    return new VersionExpression(
                            contentMediaType.version().major(), contentMediaType
                            .version().minor(), contentMediaType.version()
                            .release());
                } else {
                    return null;
                }
            } else {
                return new VersionExpression(
                        contentVersionString);
            }

        }

        private Builder(HttpServletRequest req, Class<T> resourceClass) throws
                IOException {
            this(resourceClass, req.getMethod(), getAcceptedType(req),
                    getAcceptedVersionRange(req), getContentType(req),
                    getContentVersionRange(req),
                    req.getReader());
            this.queryParameter.putAll(req.getParameterMap());
        }

        private Builder(Class<T> resourceClass, String methodName) {
            this(resourceClass, methodName, null,
                    null, null,
                    null,
                    null);
        }

        public Builder addQueryParameter(
                String name, String... values) {
            String[] actValues;
            if (this.queryParameter.containsKey(name)) {
                actValues = this.queryParameter.get(name);
            } else {
                actValues = new String[0];
            }
            String[] newValues = new String[actValues.length + values.length];
            System.arraycopy(actValues, 0, newValues, 0,
                    actValues.length);
            System.arraycopy(values, 0, newValues, actValues.length,
                    values.length);
            this.queryParameter.put(name, newValues);
            return this;
        }

        public Builder addSingleQueryParameter(
                Map<String, String> params) {
            for (Entry<String, String> e : params.entrySet()) {
                addQueryParameter(e.getKey(), e.getValue());
            }
            return this;
        }

        public Builder addQueryParameter(
                Map<String, String[]> params) {
            this.queryParameter.putAll(params);
            return this;
        }

        public Builder addPathParameter(
                String name, String value) {
            this.pathParameter.put(name, value);
            return this;
        }

        public Builder addPathParameter(
                Map<String, String> params) {
            this.pathParameter.putAll(params);
            return this;
        }

        public Builder addParameter(
                String name, Object value) {
            this.parameter.put(name, value);
            return this;
        }

        public Builder addParameter(
                Map<String, Object> params) {
            this.parameter.putAll(params);
            return this;
        }

        public Builder withRequestReaderResolver(
                final RequestReaderResolver requestReaderResolver) {
            this.requestReaderResolver = requestReaderResolver;
            return this;
        }

        public Builder withRequestContent(final Object requestContent) {
            this.requestContent = requestContent;
            return this;
        }

        public DefaultRequest<T> build() {
            if (this.requestContent != null) {
                return new DefaultRequest(methodName, acceptedMediaType,
                        contentMediaType, acceptedVersionRange, contentVersion,
                        queryParameter, pathParameter, parameter, resourceClass,
                        resource,
                        resourceMethod, requestContent);
            } else {
                return new DefaultRequest(methodName, acceptedMediaType,
                        contentMediaType, acceptedVersionRange, contentVersion,
                        queryParameter, pathParameter, parameter, resourceClass,
                        resource,
                        resourceMethod, requestReaderResolver, contentReader);
            }
        }
    }
}
