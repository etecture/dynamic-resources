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
import de.etecture.opensource.dynamicresources.api.Global;
import de.etecture.opensource.dynamicresources.api.Method;
import de.etecture.opensource.dynamicresources.api.RequestReader;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.extension.Current;
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
import java.util.Set;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
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
    BeanManager beanManager;
    @Inject
    @Any
    Instance<ExceptionHandler> exceptionHandlers;
    @Inject
    ResponseWriterResolver responseWriterResolver;
    @Inject
    RequestReaderResolver requestReaderResolver;
    @Inject
    @Current
    HttpServletRequest req;
    @Inject
    @Current
    HttpServletResponse resp;
    @Inject
    @Global
    Instance<ResourceInterceptor> globalInterceptors;
    private final QueryMetaData.Kind kind;

    protected AbstractResourceMethodHandler(
            QueryMetaData.Kind kind) {
        this.kind = kind;
    }

    private Response before(
            Resource resource,
            Method method,
            Class<?> resourceClass,
            Map<String, Object> parameter) {
        Response response = null;
        try {
            for (Class<ResourceInterceptor> ric : method.interceptors()) {
                ResourceInterceptor ri = (ResourceInterceptor) ric.newInstance();
                response = ri.before(resource, method, resourceClass, parameter);
                if (response != null) {
                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("cannot invoke resource interceptor", ex);
        }
        if (response == null) {
            for (ResourceInterceptor ri : globalInterceptors) {
                response = ri.before(resource, method, resourceClass, parameter);
                if (response != null) {
                    break;
                }
            }
        }
        return response;
    }

    private Response after(Resource resource, Method method,
            Class<?> resourceClass,
            Map<String, Object> parameter, Response response) {
        for (Class<ResourceInterceptor> ric : method.interceptors()) {
            final Bean<? extends Object> resolved =
                    beanManager.resolve(beanManager.getBeans(ric));
            ResourceInterceptor ri = (ResourceInterceptor) beanManager
                    .getReference(resolved, ric,
                    beanManager
                    .createCreationalContext(resolved));
            response = ri.after(resource, method, resourceClass, parameter,
                    response);
            if (response != null) {
                break;
            }
        }
        for (ResourceInterceptor ri : globalInterceptors) {
            response = ri.after(resource, method, resourceClass, parameter,
                    response);
        }
        return response;
    }

    protected <T> Method getMethodForRequest(Class<T> resourceClazz) {
        Resource resource = resourceClazz.getAnnotation(Resource.class);
        for (Method methodAnnotation : resource.methods()) {
            if (req.getMethod().equalsIgnoreCase(methodAnnotation.name())) {
                return methodAnnotation;
            }
        }
        return null;
    }

    @Override
    public String getDescription(
            Class<?> resourceClass) {
        Resource resource = resourceClass.getAnnotation(Resource.class);
        if (resource != null) {
            Verb verb = this.getClass().getAnnotation(Verb.class);
            if (verb != null) {
                for (Method methodAnnotation : resource.methods()) {
                    if (verb.value().equalsIgnoreCase(methodAnnotation.name())) {
                        return methodAnnotation.description();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean isAvailable(
            Class<?> resourceClass) {
        Resource resource = resourceClass.getAnnotation(Resource.class);
        if (resource == null) {
            return false;
        }
        Verb verb = this.getClass().getAnnotation(Verb.class);
        if (verb == null) {
            return false;
        }
        for (Method methodAnnotation : resource.methods()) {
            if (verb.value().equalsIgnoreCase(methodAnnotation.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> void handleRequest(
            Class<T> resourceClazz, Map<String, String> pathValues) throws
            IOException {
        Map<String, Object> parameter =
                buildParameterMap(pathValues, resourceClazz);
        Response response;
        Method methodAnnotation = getMethodForRequest(resourceClazz);
        if (methodAnnotation == null) {
            response = new DefaultResponse("method " + req.getMethod()
                    + " is not defined for resource: " + resourceClazz
                    .getSimpleName(), StatusCodes.METHOD_NOT_ALLOWED);
            writeResponse(response);
        } else {
            response = checkSecurityConstraints(resourceClazz);
            if (response == null) {
                response = before(resourceClazz
                        .getAnnotation(
                        Resource.class), methodAnnotation,
                        resourceClazz, parameter);
                if (response == null) {
                    QueryMetaData<T> qmd = buildMetaData(resourceClazz,
                            methodAnnotation,
                            parameter);
                    response =
                            executeQuery(resourceClazz, methodAnnotation, qmd);
                    response = after(resourceClazz
                            .getAnnotation(
                            Resource.class), methodAnnotation, resourceClazz,
                            qmd
                            .getParameterMap(),
                            response);
                }
            }
        }
        writeResponse(response);
    }

    protected <T> QueryMetaData<T> buildMetaData(Class<T> resourceClazz,
            Method methodAnnotation, Map<String, Object> parameter) throws
            IOException {

        DefaultQueryMetaData<T> queryMetaData = new DefaultQueryMetaData(
                methodAnnotation.query(),
                methodAnnotation.name(),
                resourceClazz,
                kind,
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
            Class<T> resourceClazz, Method method) {
        System.out.printf("handle Exception: %s, resource: %s, method: %s%n",
                exception.getClass().getSimpleName(),
                resourceClazz.getSimpleName(),
                method.name());
        for (ExceptionHandler exh : exceptionHandlers) {
            System.out.printf("check handler: %s%n", exh.getClass()
                    .getSimpleName());
            if (exh
                    .isResponsibleFor(resourceClazz, method.name(), exception
                    .getClass())) {
                System.out.printf("call exception handler: %s%n", exh.getClass()
                        .getSimpleName());
                return exh.handleException(resourceClazz, method.name(),
                        exception);
            }
        }
        return new DefaultResponse(exception, 500);
    }

    protected <T> Response<?> executeQuery(
            Class<T> resourceClazz,
            Method method,
            QueryMetaData<T> queryMetaData) {
        try {
            return new DefaultResponse(
                    getExecutorByTechnology(queryMetaData
                    .getQueryTechnology()).execute(queryMetaData),
                    method.status());
        } catch (Throwable ex) {
            return handleException(ex, resourceClazz, method);
        }
    }

    private QueryExecutor getExecutorByTechnology(String technology) {
        Bean<?> resolvedBean;
        if ("default".equalsIgnoreCase(technology)) {
            Set<Bean<?>> queryExecutors = beanManager.getBeans(
                    QueryExecutor.class, new TechnologyLiteral(technology));
            if (queryExecutors.isEmpty()) {
                queryExecutors = beanManager.getBeans(QueryExecutor.class,
                        new AnnotationLiteral<Any>() {
                    private static final long serialVersionUID = 1L;
                });
                if (queryExecutors.isEmpty()) {
                    throw new UnsatisfiedResolutionException(
                            "no queryexecutor defined.");
                } else {
                    resolvedBean = queryExecutors.iterator().next();
                }
            } else if (queryExecutors.size() > 1) {
                throw new UnsatisfiedResolutionException(
                        "more than one technologies found.");
            } else {
                resolvedBean = queryExecutors.iterator().next();
            }
        } else {
            Set<Bean<?>> queryExecutors = beanManager.getBeans(
                    QueryExecutor.class, new TechnologyLiteral(technology));
            resolvedBean =
                    beanManager.resolve(queryExecutors);
        }
        QueryExecutor qe = (QueryExecutor) this.beanManager.getReference(
                resolvedBean, QueryExecutor.class, this.beanManager
                .createCreationalContext(resolvedBean));
        return qe;
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
        Method methodAnnotation = getMethodForRequest(resourceClazz);
        return getExecutorByTechnology(methodAnnotation.query().technology())
                .execute(
                new DefaultQueryMetaData<>(
                methodAnnotation.query(),
                methodAnnotation.name(),
                resourceClazz,
                kind,
                parameter));
    }

    protected <T, R> R readRequest(Class<T> resourceClazz) throws IOException {
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

        Method methodAnnotation = getMethodForRequest(resourceClazz);
        RequestReader<R> reader = requestReaderResolver.resolve(methodAnnotation
                .requestType(),
                mediaType, version);

        if (reader != null) {
            return reader.processRequest(req.getReader(), contentType);
        } else {
            return null;
        }
    }

    protected void writeResponse(Response<?> response) throws IOException {
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
            Map<String, String> pathValues,
            Class<R> resourceClazz) throws IOException {
        Map<String, Object> parameter = new HashMap<>();
        parameter.putAll(pathValues);
        for (String parameterName : Collections.list(req
                .getParameterNames())) {
            parameter
                    .put(parameterName, req.getParameter(parameterName));
        }
        Object requestObject = readRequest(resourceClazz);
        if (requestObject != null) {
            parameter.put("request", requestObject);
        }
        return parameter;
    }

    protected Response checkSecurityConstraints(
            Class<?> resourceClass) {
        Method methodAnnotation = getMethodForRequest(resourceClass);
        String[] allowedRoles = methodAnnotation.rolesAllowed();
        if (allowedRoles != null && allowedRoles.length > 0) {
            boolean trust = false;
            for (String role : allowedRoles) {
                trust = trust || req.isUserInRole(role);
            }
            if (!trust) {
                return new DefaultResponse("User " + req.getUserPrincipal()
                        + " is not allowed to perform this request.\n",
                        StatusCodes.FORBIDDEN);
            }
        }
        return null;
    }
}
