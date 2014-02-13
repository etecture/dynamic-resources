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

import de.etecture.opensource.dynamicrepositories.api.HintValueGenerator;
import de.etecture.opensource.dynamicrepositories.api.ParamValueGenerator;
import de.etecture.opensource.dynamicrepositories.api.annotations.Hint;
import de.etecture.opensource.dynamicrepositories.api.annotations.Hints;
import de.etecture.opensource.dynamicrepositories.api.annotations.Param;
import de.etecture.opensource.dynamicrepositories.api.annotations.Params;
import de.etecture.opensource.dynamicrepositories.executor.QueryExecutionContext;
import de.etecture.opensource.dynamicrepositories.api.DefaultQueryHints;
import de.etecture.opensource.dynamicrepositories.extension.DefaultQueryExecutionContext;
import de.etecture.opensource.dynamicrepositories.extension.QueryExecutors;
import de.etecture.opensource.dynamicresources.annotations.accessing.Verb;
import de.etecture.opensource.dynamicresources.annotations.Filter;
import de.etecture.opensource.dynamicresources.annotations.Global;
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Resource;
import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.api.FilterConverter;
import de.etecture.opensource.dynamicresources.api.OldRequest;
import de.etecture.opensource.dynamicresources.api.OldResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.PathParamSubstitution;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.UriBuilder;
import de.etecture.opensource.dynamicresources.core.mapping.RequestReaderResolver;
import de.etecture.opensource.dynamicresources.core.mapping.ResponseWriterResolver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 *
 * @author rhk
 */
@Deprecated
public abstract class AbstractResourceMethodHandler implements
        ResourceMethodHandler {

    @Inject
    RequestReaderResolver requestReaderResolver;
    @Inject
    ResponseWriterResolver responseWriterResolver;
    @Inject
    @Global
    Instance<OldResourceInterceptor> globalInterceptors;
    @Inject
    @Any
    Instance<OldResourceInterceptor> anyInterceptors;
    @Inject
    @Any
    Instance<FilterConverter> anyFilterConverters;
    @Inject
    protected QueryExecutors executors;
    @Inject
    @Default
    UriBuilder uriBuilder;
    @Inject
    Instance<HintValueGenerator> hintValueGenerators;
    @Inject
    Instance<ParamValueGenerator> paramValueGenerators;
    @Inject
    Instance<Object> instances;

    protected Response before(OldRequest request) {
        Response response = null;
        for (Class<? extends OldResourceInterceptor> ric : request
                .getResourceMethod()
                .interceptors()) {
            response = anyInterceptors.select(ric).get().before(request);
            if (response != null) {
                break;
            }
        }
        if (response == null) {
            for (OldResourceInterceptor ri : globalInterceptors) {
                response = ri.before(request);
                if (response != null) {
                    break;
                }
            }
        }
        return response;
    }

    protected Response afterSuccess(OldRequest request, Response response) {
        for (Class<? extends OldResourceInterceptor> ric : request
                .getResourceMethod().interceptors()) {
            response = anyInterceptors.select(ric).get().afterSuccess(
                    request, response);
        }
        for (OldResourceInterceptor ri : globalInterceptors) {
            response = ri.afterSuccess(request,
                    response);
        }
        return response;
    }

    protected Response afterFailure(OldRequest request, Response response,
            Throwable exception) {
        for (Class<? extends OldResourceInterceptor> ric : request
                .getResourceMethod().interceptors()) {
            response = anyInterceptors.select(ric).get().afterFailure(
                    request, response, exception);
        }
        for (OldResourceInterceptor ri : globalInterceptors) {
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
    public <T> Response<T> handleRequest(OldRequest<T> request) throws
            ResourceException {
        Response<T> response;
        response = before(request);
        if (response == null) {
            final Class<?> seeOther = request.getResourceMethod().seeOther();
            if (seeOther == Class.class || seeOther == null) {
                try {
                    DefaultQueryExecutionContext<T> query = buildQuery(request, request
                            .getResourceClass());
                    query.addHint(DefaultQueryHints.LIMIT, 1);
                    response =
                            executeQuery(request, query);
                } catch (Throwable t) {
                    return afterFailure(request, new DefaultResponse(request
                            .getResourceClass(), t),
                            t);
                }

            } else {
                if (seeOther.isAnnotationPresent(Resource.class)) {
                    try {
                        DefaultQueryExecutionContext<PathParamSubstitution> query =
                                buildQuery(request,
                                PathParamSubstitution.class);
                        query.addHint(DefaultQueryHints.LIMIT, -1);
                        final List<PathParamSubstitution> substitutions =
                                (List<PathParamSubstitution>) executors.execute(
                                query);
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

    protected <T> DefaultQueryExecutionContext<T> buildQuery(OldRequest<?> request,
            Class<T> queryType) throws
            ResourceException {
        final de.etecture.opensource.dynamicrepositories.api.annotations.Query qa =
                request.getResourceMethod().query();

        DefaultQueryExecutionContext<T> query = new DefaultQueryExecutionContext(queryType,
                qa.technology(),
                qa.connection(),
                createStatement(request.getRequestType(), request
                .getMethodName(), qa.statement()),
                qa.converter());

        if (request.getResourceClass().isAnnotationPresent(Param.class)) {
            addParams(query, request.getResourceClass().getAnnotation(
                    Param.class));
        } else if (request.getResourceClass().isAnnotationPresent(Params.class)) {
            addParams(query, request.getResourceClass().getAnnotation(
                    Params.class).value());
        }
        if (request.getResourceClass().isAnnotationPresent(Hint.class)) {
            addHints(query, request.getResourceClass().getAnnotation(
                    Hint.class));
        } else if (request.getResourceClass().isAnnotationPresent(Hints.class)) {
            addHints(query, request.getResourceClass().getAnnotation(
                    Hints.class).value());
        }
        addParams(query, qa.params());
        addHints(query, qa.hints());
        for (Entry<String, String> e : request.getPathParameter().entrySet()) {
            query.addParameter(e.getKey(), e.getValue());
        }
        for (Entry<String, String[]> e : request.getQueryParameter().entrySet()) {
            if (e.getValue() != null && e.getValue().length == 1) {
                query.addParameter(e.getKey(), e.getValue()[0]);
            } else {
                query.addParameter(e.getKey(), e.getValue());
            }
        }
        for (Filter filter : request.getResourceMethod().filters()) {
            query.addParameter(filter.name(), anyFilterConverters.select(filter
                    .converter()).get().convert(filter, request, request
                    .getAllParameter()));
        }
        Object requestObject = request.getContent();
        if (requestObject != null) {
            query.addParameter("request", requestObject);
        }

        return query;
    }

    private String createStatement(Class<?> type, String name,
            String statement) {
        if (statement == null || statement.trim().isEmpty()) {
            statement = name;
        }
        try {
            return ResourceBundle.getBundle(type
                    .getName()).getString(statement);
        } catch (MissingResourceException e) {
            return statement;
        }
    }

    private void addHints(DefaultQueryExecutionContext query, Hint... hints) {
        for (Hint hint : hints) {
            if (hintValueGenerators == null) {
                query.addHint(hint.name(), hint.value());
            } else {
                query.addHint(hint.name(), hintValueGenerators.select(hint
                        .generator()).get().generate(hint));
            }
        }
    }

    private void addParams(DefaultQueryExecutionContext query, Param... params) {
        for (Param param : params) {
            if (paramValueGenerators == null) {
                query.addParameter(param.name(), param.value());
            } else {
                query.addParameter(param.name(), paramValueGenerators
                        .select(param.generator()).get().generate(param));
            }
        }
    }

    protected <T> Response<T> executeQuery(
            OldRequest<T> request,
            QueryExecutionContext<T> query) throws Exception {
        return new DefaultResponse(
                executors.execute(query),
                request.getResourceMethod().status());
    }
}
