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
import de.etecture.opensource.dynamicresources.api.Filter;
import de.etecture.opensource.dynamicresources.api.Global;
import de.etecture.opensource.dynamicresources.api.Method;
import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.extension.Current;
import de.etecture.opensource.dynamicresources.extension.DefaultQueryMetaData;
import de.etecture.opensource.dynamicresources.extension.RequestReaderResolver;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import java.io.IOException;
import java.util.HashMap;
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
import org.apache.commons.beanutils.ConvertUtils;

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
    RequestReaderResolver requestReaderResolver;
    @Inject
    ResponseWriterResolver responseWriterResolver;
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

    private Response before(Request request) {
        Response response = null;
        try {
            for (Class<? extends ResourceInterceptor> ric : request
                    .getResourceMethod()
                    .interceptors()) {
                ResourceInterceptor ri = (ResourceInterceptor) ric.newInstance();
                response = ri.before(request);
                if (response != null) {
                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("cannot invoke resource interceptor", ex);
        }
        if (response == null) {
            for (ResourceInterceptor ri : globalInterceptors) {
                response = ri.before(request);
                if (response != null) {
                    break;
                }
            }
        }
        return response;
    }

    private Response after(Request request, Response response) {
        for (Class<? extends ResourceInterceptor> ric : request
                .getResourceMethod().interceptors()) {
            final Bean<? extends Object> resolved =
                    beanManager.resolve(beanManager.getBeans(ric));
            ResourceInterceptor ri = (ResourceInterceptor) beanManager
                    .getReference(resolved, ric,
                    beanManager
                    .createCreationalContext(resolved));
            response = ri.after(request,
                    response);
            if (response != null) {
                break;
            }
        }
        for (ResourceInterceptor ri : globalInterceptors) {
            response = ri.after(request,
                    response);
        }
        return response;
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
            Class<?> resourceClazz) {
        Resource resource = resourceClazz.getAnnotation(Resource.class);
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
    public Response handleRequest(Request request) throws IOException {
        Response response;
        response = before(request);
        if (response == null) {
            QueryMetaData qmd = buildMetaData(request);
            response =
                    executeQuery(request, qmd);
            response = after(request, response);
        }
        return response;
    }

    protected <T> QueryMetaData<T> buildMetaData(Request request) throws
            IOException {

        Map<String, Object> parameter = buildParameterMap(request);
        DefaultQueryMetaData<T> queryMetaData = new DefaultQueryMetaData(
                request.getResourceMethod().query(),
                request.getResourceMethod().name(),
                request.getResourceClass(),
                kind,
                parameter);

        if (request.getResourceClass().isAnnotationPresent(Param.class)) {
            Param param = request.getResourceClass().getAnnotation(Param.class);

            queryMetaData.addParameter(param);
        } else if (request.getResourceClass().isAnnotationPresent(Params.class)) {
            for (Param param : request.getResourceClass().getAnnotation(
                    Params.class).value()) {
                queryMetaData.addParameter(param);
            }
        }
        return queryMetaData;
    }

    protected Response<?> handleException(Throwable exception,
            Request request) {
        System.out.printf("handle Exception: %s, resource: %s, method: %s%n",
                exception.getClass().getSimpleName(),
                request.getResourceClass().getSimpleName(),
                request.getResourceMethod().name());
        for (ExceptionHandler exh : exceptionHandlers) {
            System.out.printf("check handler: %s%n", exh.getClass()
                    .getSimpleName());
            if (exh
                    .isResponsibleFor(request, exception
                    .getClass())) {
                System.out.printf("call exception handler: %s%n", exh.getClass()
                        .getSimpleName());
                return exh.handleException(request,
                        exception);
            }
        }
        return new DefaultResponse(exception, 500);
    }

    protected <T> Response<?> executeQuery(
            Request request,
            QueryMetaData<T> queryMetaData) {
        try {
            return new DefaultResponse(
                    getExecutorByTechnology(queryMetaData
                    .getQueryTechnology()).execute(queryMetaData),
                    request.getResourceMethod().status());
        } catch (Throwable ex) {
            return handleException(ex, request);
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
            } else if (queryExecutors.size()
                    > 1) {
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
        for (Method methodAnnotation : resourceClazz.getAnnotation(
                Resource.class).methods()) {
            if (methodAnnotation.name().equalsIgnoreCase(this.getClass()
                    .getAnnotation(Verb.class).value())) {
                return getExecutorByTechnology(methodAnnotation.query()
                        .technology())
                        .execute(
                        new DefaultQueryMetaData<>(
                        methodAnnotation.query(),
                        methodAnnotation.name(),
                        resourceClazz,
                        kind,
                        parameter));
            }
        }
        return null;
    }

    protected Map<String, Object> buildParameterMap(Request request) throws
            IOException {
        Map<String, Object> parameter = new HashMap<>();
        for (Filter filter : request.getResourceMethod().filters()) {
            String stringValue = null;
            if (request.getQueryParameter().containsKey(filter.name())) {
                String[] paramValues = request.getQueryParameter().get(filter
                        .name());
                if (paramValues.length >= 1) {
                    stringValue = paramValues[0];
                }
            }
            if (stringValue == null && !filter.defaultValue().isEmpty()) {
                stringValue = filter.defaultValue();
            }
            parameter.put(filter.name(), ConvertUtils.convert(stringValue,
                    filter.type()));
        }
        parameter.putAll(request.getPathParameter());
        Object requestObject = request.getContent();
        if (requestObject != null) {
            parameter.put("request", requestObject);
        }
        return parameter;
    }
}
