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
import de.etecture.opensource.dynamicrepositories.spi.QueryExecutor;
import de.etecture.opensource.dynamicrepositories.spi.QueryExecutorResolver;
import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.api.Filter;
import de.etecture.opensource.dynamicresources.api.FilterConverter;
import de.etecture.opensource.dynamicresources.api.Global;
import de.etecture.opensource.dynamicresources.api.Method;
import de.etecture.opensource.dynamicresources.api.PathParamSubstitution;
import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.UriBuilder;
import de.etecture.opensource.dynamicresources.extension.DefaultQueryMetaData;
import de.etecture.opensource.dynamicresources.extension.RequestReaderResolver;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
public abstract class AbstractResourceMethodHandler implements
        ResourceMethodHandler {

    @Inject
    RequestReaderResolver requestReaderResolver;
    @Inject
    ResponseWriterResolver responseWriterResolver;
    @Inject
    @Global
    Instance<ResourceInterceptor> globalInterceptors;
    @Inject
    @Any
    Instance<ResourceInterceptor> anyInterceptors;
    @Inject
    @Any
    Instance<FilterConverter> anyFilterConverters;
    @Inject
    QueryExecutorResolver queryExecutors;
    @Inject
    @Default
    UriBuilder uriBuilder;
    private final QueryMetaData.Kind kind;

    protected AbstractResourceMethodHandler(
            QueryMetaData.Kind kind) {
        this.kind = kind;
    }

    protected Response before(Request request) {
        Response response = null;
        for (Class<? extends ResourceInterceptor> ric : request
                .getResourceMethod()
                .interceptors()) {
            response = anyInterceptors.select(ric).get().before(request);
            if (response != null) {
                break;
            }
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

    protected Response afterSuccess(Request request, Response response) {
        for (Class<? extends ResourceInterceptor> ric : request
                .getResourceMethod().interceptors()) {
            response = anyInterceptors.select(ric).get().afterSuccess(
                    request, response);
        }
        for (ResourceInterceptor ri : globalInterceptors) {
            response = ri.afterSuccess(request,
                    response);
        }
        return response;
    }

    protected Response afterFailure(Request request, Response response,
            Throwable exception) {
        for (Class<? extends ResourceInterceptor> ric : request
                .getResourceMethod().interceptors()) {
            response = anyInterceptors.select(ric).get().afterFailure(
                    request, response, exception);
        }
        for (ResourceInterceptor ri : globalInterceptors) {
            response = ri.afterSuccess(request,
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
    public <T> Response<T> handleRequest(Request<T> request) throws
            ResourceException {
        Response<T> response;
        response = before(request);
        if (response == null) {
            final Class<?> seeOther = request.getResourceMethod().seeOther();
            if (seeOther == Class.class || seeOther == null) {
                try {
                    QueryMetaData qmd = buildMetaData(request, request
                            .getResourceClass(), QueryMetaData.Type.SINGLE);
                    response =
                            executeQuery(request, qmd);
                } catch (Throwable t) {
                    return afterFailure(request, new DefaultResponse(request
                            .getResourceClass(), t),
                            t);
                }

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
                    } catch (Throwable ex) {
                        return afterFailure(request, new DefaultResponse(request
                                .getResourceClass(), ex), ex);
                    }
                } else {
                    throw new ResourceException("specified see-other-class: "
                            + seeOther + " is not a resource!");
                }
            }
            response = afterSuccess(request, response);
        }
        return response;
    }

    protected <T> DefaultQueryMetaData<T> buildMetaData(Request<?> request,
            Class<T> queryType, QueryMetaData.Type type) throws
            ResourceException {

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

    protected <T> Response<T> executeQuery(
            Request<T> request,
            QueryMetaData<T> queryMetaData) throws Exception {
        return new DefaultResponse(
                getExecutorByTechnology(queryMetaData
                .getQueryTechnology()).execute(queryMetaData),
                request.getResourceMethod().status());
    }

    protected QueryExecutor getExecutorByTechnology(String technology) {
        if (StringUtils.isBlank(technology) || "default".equalsIgnoreCase(
                technology)) {
            return queryExecutors.getDefaultExecutor();
        } else {
            return queryExecutors.getQueryExecutorForTechnology(technology);
        }
    }

    protected Map<String, Object> buildParameterMap(Request<?> request) throws
            ResourceException {
        Map<String, Object> parameter = new HashMap<>();
        parameter.putAll(request.getParameter());
        parameter.putAll(request.getPathParameter());
        for (Filter filter : request.getResourceMethod().filters()) {
            anyFilterConverters.select(filter.converter()).get().convert(filter,
                    request,
                    parameter);
        }
        Object requestObject = request.getContent();
        System.out.println("request is: " + requestObject);
        if (requestObject != null) {
            parameter.put("request", requestObject);
        }
        request.getParameter().putAll(parameter);
        return parameter;
    }
}
