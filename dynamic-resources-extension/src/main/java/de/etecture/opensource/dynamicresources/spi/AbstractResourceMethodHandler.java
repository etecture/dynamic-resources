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
package de.etecture.opensource.dynamicresources.spi;

import de.etecture.opensource.dynamicrepositories.api.Param;
import de.etecture.opensource.dynamicrepositories.api.Params;
import de.etecture.opensource.dynamicrepositories.extension.TechnologyLiteral;
import de.etecture.opensource.dynamicrepositories.spi.QueryExecutor;
import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.api.ExceptionHandler;
import de.etecture.opensource.dynamicresources.api.RequestReader;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.extension.DefaultQueryMetaData;
import de.etecture.opensource.dynamicresources.extension.MediaTypeExpression;
import de.etecture.opensource.dynamicresources.extension.RequestReaderResolver;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import de.etecture.opensource.dynamicresources.extension.VersionNumberRangeExpression;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
public abstract class AbstractResourceMethodHandler implements
        ResourceMethodHandler {

    @Inject
    @Any
    Instance<QueryExecutor> queryExecutors;
    @Inject
    @Any
    Instance<ExceptionHandler> exceptionHandlers;
    @Inject
    ResponseWriterResolver responseWriterResolver;
    @Inject
    RequestReaderResolver requestReaderResolver;
    private final String method;
    private final QueryMetaData.Kind kind;

    protected AbstractResourceMethodHandler(String method,
            QueryMetaData.Kind kind) {
        this.method = method;
        this.kind = kind;
    }

    protected abstract Class<?> getRequestType(Class<?> resourceClazz);

    protected abstract String getQuery(Class<?> resourceClazz);

    protected abstract String getQueryName(Class<?> resourceClazz);

    protected abstract String getTechnology(Class<?> resourceClazz);

    protected abstract int getDefaultStatus(Class<?> resourceClazz);
    @Inject
    @Any
    Instance<ResourceInterceptor> resourceInterceptors;

    private Response before(String method, Resource resource,
            Class<?> resourceClass,
            Map<String, Object> parameter) {
        Response response = null;
        for (ResourceInterceptor ri : resourceInterceptors) {
            response = ri.before(method, resource, resourceClass, parameter);
            if (response != null) {
                break;
            }
        }
        return response;
    }

    private Response after(String method, Resource resource,
            Class<?> resourceClass,
            Map<String, Object> parameter, Response response) {
        for (ResourceInterceptor ri : resourceInterceptors) {
            response = ri.after(method, resource, resourceClass, parameter,
                    response);
        }
        return response;
    }

    @Override
    public <T> void handleRequest(
            Class<T> resourceClazz,
            Map<String, String> pathValues, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        Map<String, Object> parameter =
                buildParameterMap(pathValues, req, resourceClazz);
        Response response = before(req.getMethod(), resourceClazz.getAnnotation(
                Resource.class),
                resourceClazz, parameter);
        if (response == null) {
            QueryMetaData<T> qmd = buildMetaData(resourceClazz, parameter);
            response = executeQuery(resourceClazz, qmd);
            response = after(req.getMethod(), resourceClazz.getAnnotation(
                    Resource.class), resourceClazz, qmd.getParameterMap(),
                    response);
        }
        writeResponse(req, resp, response);
    }

    protected <T> QueryMetaData<T> buildMetaData(Class<T> resourceClazz,
            Map<String, Object> parameter) throws IOException {

        DefaultQueryMetaData<T> queryMetaData = new DefaultQueryMetaData(
                resourceClazz,
                kind,
                getQuery(resourceClazz),
                getQueryName(resourceClazz),
                parameter);
        if (resourceClazz.isAnnotationPresent(Param.class)) {
            Param param = resourceClazz.getAnnotation(Param.class);
            queryMetaData.addParameter(param);
        } else if (resourceClazz.isAnnotationPresent(Params.class)) {
            for (Param param : resourceClazz.getAnnotation(Params.class)
                    .value()) {
                queryMetaData.addParameter(param);
            }
        }
        return queryMetaData;
    }

    protected <T> Response<?> handleException(Throwable exception,
            Class<T> resourceClazz) {
        System.out.printf("handle Exception: %s, resource: %s, method: %s%n",
                exception.getClass().getSimpleName(),
                resourceClazz.getSimpleName(),
                method);
        for (ExceptionHandler exh : exceptionHandlers) {
            System.out.printf("check handler: %s%n", exh.getClass()
                    .getSimpleName());
            if (exh
                    .isResponsibleFor(resourceClazz, method, exception
                    .getClass())) {
                System.out.printf("call exception handler: %s%n", exh.getClass()
                        .getSimpleName());
                return exh.handleException(resourceClazz, method, exception);
            }
        }
        return new DefaultResponse(exception, 500);
    }

    protected <T> Response<?> executeQuery(
            Class<T> resourceClazz,
            QueryMetaData<T> queryMetaData) {
        try {
            return new DefaultResponse(
                    queryExecutors.select(new TechnologyLiteral(getTechnology(
                    resourceClazz))).get().execute(queryMetaData),
                    getDefaultStatus(resourceClazz));
        } catch (Throwable ex) {
            return handleException(ex, resourceClazz);
        }
    }

    @Override
    public <T> T execute(
            Class<T> resourceClazz,
            Map<String, Object> params, Object request) throws Exception {
        Map<String, Object> parameter = new HashMap<>();
        parameter.putAll(params);
        if (request != null) {
            parameter.put("request", request);
        }
        return queryExecutors.select(new TechnologyLiteral(getTechnology(
                resourceClazz))).get().execute(new DefaultQueryMetaData<>(
                resourceClazz,
                kind,
                getQuery(resourceClazz),
                getQueryName(resourceClazz),
                parameter));
    }

    protected <T, R> R readRequest(Class<T> resourceClazz,
            HttpServletRequest req) throws IOException {
        String versionString = req.getHeader("Content-Version");
        String contentType = req.getHeader("Content-Type");
        if (StringUtils.isBlank(contentType)) {
            contentType = "application/xml";
        }
        MediaTypeExpression mediaType = new MediaTypeExpression(contentType);
        VersionNumberRangeExpression version;
        if (StringUtils.isBlank(versionString)) {
            version = new VersionNumberRangeExpression(mediaType.version());
        } else {
            version = new VersionNumberRangeExpression(versionString);
        }

        RequestReader<R> reader = requestReaderResolver.resolve(getRequestType(
                resourceClazz),
                mediaType, version);

        if (reader != null) {
            return reader.processRequest(req.getReader(), contentType);
        } else {
            return null;
        }
    }

    protected void writeResponse(HttpServletRequest req,
            HttpServletResponse resp, Response<?> response) throws IOException {
        String versionString = req.getHeader("Accept-Version");
        String contentType = req.getHeader("Accept");
        if (StringUtils.isBlank(contentType)) {
            contentType = "application/xml";
        }
        MediaTypeExpression mediaType = new MediaTypeExpression(contentType);
        VersionNumberRangeExpression version;
        if (StringUtils.isBlank(versionString)) {
            version = new VersionNumberRangeExpression(mediaType.version());
        } else {
            version = new VersionNumberRangeExpression(versionString);
        }

        for (Map.Entry<String, List<Object>> e : response.getHeaders()) {
            for (Object o : e.getValue()) {
                if (o == null) {
                    // do nothing here
                } else if (Number.class.isAssignableFrom(o.getClass())) {
                    resp.addIntHeader(e.getKey(), ((Number) o).intValue());
                } else if (Date.class.isAssignableFrom(o.getClass())) {
                    resp.addDateHeader(e.getKey(), ((Date) o).getTime());
                } else {
                    resp.addHeader(e.getKey(), o.toString());
                }
            }
        }
        resp.setStatus(response.getStatus());
        resp.setContentType(mediaType.toString());
        if (response.getEntity() != null) {
            ResponseWriter writer =
                    responseWriterResolver.resolve(response
                    .getEntity().getClass(), mediaType, version);
            if (writer != null) {
                writer.processElement(response.getEntity(), resp.getWriter(),
                        mediaType);
            } else {
                resp.sendError(406, String.format(
                        "The resource is not available with mediatype: %s and version: %s",
                        mediaType.toString(), version.toString()));
            }
        }
    }

    protected <R> Map<String, Object> buildParameterMap(
            Map<String, String> pathValues, HttpServletRequest req,
            Class<R> resourceClazz) throws IOException {
        Map<String, Object> parameter = new HashMap<>();
        parameter.putAll(pathValues);
        for (String parameterName : Collections.list(req
                .getParameterNames())) {
            parameter
                    .put(parameterName, req.getParameter(parameterName));
        }
        Object requestObject = readRequest(resourceClazz, req);
        if (requestObject != null) {
            parameter.put("request", requestObject);
        }
        return parameter;
    }
}
