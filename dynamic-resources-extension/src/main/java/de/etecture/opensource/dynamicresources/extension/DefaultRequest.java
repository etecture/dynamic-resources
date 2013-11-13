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
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 * the request.
 *
 * @author rhk
 * @version
 * @since
 */
public class DefaultRequest implements Request {

    private final String methodName;
    private final MediaType acceptedMediaType, contentMediaType;
    private final VersionNumberRange acceptedVersionRange;
    private final Version contentVersion;
    private final Map<String, String[]> queryParameter;
    private final Map<String, String> pathParameter;
    private final Class<?> resourceClass;
    private final Resource resource;
    private final Method resourceMethod;
    private final BufferedReader contentReader;
    private final RequestReaderResolver requestReaderResolver;
    private Object requestContent;
    private boolean wasRead = false;

    public DefaultRequest(HttpServletRequest req,
            Map<String, String> pathParameter,
            Class<?> resourceClass, RequestReaderResolver requestReaderResolver)
            throws IOException {
        this.requestReaderResolver = requestReaderResolver;
        this.methodName = req.getMethod();
        this.contentReader = req.getReader();
        String acceptVersionString = req.getHeader("Accept-Version");
        String acceptType = req.getHeader("Accept");
        if (StringUtils.isBlank(acceptType)) {
            acceptType = "application/xml";
        }
        this.acceptedMediaType = new MediaTypeExpression(
                acceptType);
        if (StringUtils.isBlank(acceptVersionString)) {
            this.acceptedVersionRange = new VersionNumberRangeExpression(
                    acceptedMediaType
                    .version());
        } else {
            this.acceptedVersionRange = new VersionNumberRangeExpression(
                    acceptVersionString);
        }

        String contentVersionString = req.getHeader("Content-Version");
        String contentType = req.getHeader("Content-Type");
        if (StringUtils.isBlank(contentType)) {
            contentType = "application/xml";
        }
        this.contentMediaType = new MediaTypeExpression(
                contentType);
        if (StringUtils.isBlank(contentVersionString)) {
            if (contentMediaType.version() != null) {
                this.contentVersion = new VersionExpression(
                        contentMediaType.version().major(), contentMediaType
                        .version().minor(), contentMediaType.version().release());
            } else {
                this.contentVersion = null;
            }
        } else {
            this.contentVersion = new VersionExpression(
                    contentVersionString);
        }

        this.queryParameter = req.getParameterMap();
        this.pathParameter = pathParameter;
        this.resourceClass = resourceClass;

        this.resource = resourceClass.getAnnotation(Resource.class);

        if (!HttpMethods.OPTIONS.equalsIgnoreCase(this.methodName)) {
            StringBuilder sb = new StringBuilder();
            for (Method methodAnnotation : this.resource.methods()) {
                if (this.methodName.equalsIgnoreCase(methodAnnotation.name())) {
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
    public Class<?> getResourceClass() {
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
            RequestReader reader = requestReaderResolver.resolve(
                    getRequestType(),
                    contentMediaType, range);

            if (reader != null) {
                requestContent = reader.processRequest(contentReader,
                        contentMediaType.toString());
            } else {
                requestContent = null;
            }
        }
        return requestContent;
    }
}
