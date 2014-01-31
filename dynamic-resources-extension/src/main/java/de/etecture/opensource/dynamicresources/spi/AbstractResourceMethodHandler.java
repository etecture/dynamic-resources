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
import de.etecture.opensource.dynamicresources.api.PathParamSubstitution;
import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.UriBuilder;
import de.etecture.opensource.dynamicresources.extension.DefaultQueryMetaData;
import de.etecture.opensource.dynamicresources.extension.RequestReaderResolver;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

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
    @Global
    Instance<ResourceInterceptor> globalInterceptors;
    @Inject
    @Default
    UriBuilder uriBuilder;
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
                ResourceInterceptor ri = (ResourceInterceptor) ric
                        .newInstance();
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
        try {
            for (Class<? extends ResourceInterceptor> ric : request
                    .getResourceMethod().interceptors()) {
                final Bean<? extends Object> resolved =
                        beanManager.resolve(beanManager.getBeans(ric));
                if (resolved != null) {
                    ResourceInterceptor ri =
                            (ResourceInterceptor) beanManager
                            .getReference(resolved, ric,
                            beanManager
                            .createCreationalContext(resolved));
                    response = ri.after(request,
                            response);
                    if (response != null) {
                        break;
                    }
                } else {
                    ResourceInterceptor ri = (ResourceInterceptor) ric
                            .newInstance();
                    response = ri.after(request, response);
                    if (response != null) {
                        break;
                    }

                }
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("cannot invoke resource interceptor", ex);
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
    public <T> Response<T> handleRequest(Request<T> request) throws IOException {
        Response<T> response;
        response = before(request);
        if (response == null) {
            final Class<?> seeOther = request.getResourceMethod().seeOther();
            if (seeOther == Class.class || seeOther == null) {
                QueryMetaData qmd = buildMetaData(request, request
                        .getResourceClass(), QueryMetaData.Type.SINGLE);
                response =
                        executeQuery(request, qmd);

            } else {
                if (seeOther.isAnnotationPresent(Resource.class)) {
                    try {
                        DefaultQueryMetaData qmd = buildMetaData(request,
                                PathParamSubstitution.class,
                                QueryMetaData.Type.LIST);
                        final List<PathParamSubstitution> substitutions =
                                (List<PathParamSubstitution>) getExecutorByTechnology(
                                qmd.getQueryTechnology()).execute(qmd);
                        Map<String, String> pathValues = new HashMap<>();
                        for (PathParamSubstitution substitution : substitutions) {
                            pathValues.put(substitution.getParamName(),
                                    substitution.getParamValue());
                        }
                        response = new DefaultResponse((T) null,
                                StatusCodes.SEE_OTHER);
                        ((DefaultResponse) response).addHeader("Location",
                                uriBuilder.build(
                                seeOther, pathValues));
                    } catch (Exception ex) {
                        response = handleException(ex, request);
                    }
                } else {
                    throw new IOException("specified see-other-class: "
                            + seeOther + " is not a resource!");
                }
            }

            response = after(request, response);
        }
        return response;
    }

    protected <T> DefaultQueryMetaData<T> buildMetaData(Request<?> request,
            Class<T> queryType, QueryMetaData.Type type) throws IOException {

        Map<String, Object> parameter = buildParameterMap(request);
        DefaultQueryMetaData<T> queryMetaData = new DefaultQueryMetaData(
                request.getResourceMethod().query(),
                request.getResourceMethod().name(),
                queryType,
                type,
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

    protected <T> Response<T> handleException(Throwable exception,
            Request<T> request) {
        System.out.printf(
                "handle Exception: %s, message: %s, resource: %s, method: %s%n",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
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
                return exh.handleException(request, exception);
            }
        }
        return new DefaultResponse(request.getRequestType(), exception);
    }

    protected <T> Response<T> executeQuery(
            Request<T> request,
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

    protected QueryExecutor getExecutorByTechnology(String technology) {
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

    protected Map<String, Object> buildParameterMap(Request<?> request) throws
            IOException {
        Map<String, Object> parameter = new HashMap<>();
        parameter.putAll(request.getParameter());
        parameter.putAll(request.getPathParameter());
        for (Filter filter : request.getResourceMethod().filters()) {
            try {
                filter.converter().newInstance().convert(filter, request,
                        parameter);
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IOException("cannot convert filter parameter "
                        + filter.name() + " for request " + request
                        .getMethodName() + "!", ex);
            }
        }
        Object requestObject = request.getContent();
        System.out.println("request is: " + requestObject);
        if (requestObject != null) {
            parameter.put("request", requestObject);
        }
        return parameter;
    }
}
