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
package de.etecture.opensource.dynamicresources.test.junit;

import de.etecture.opensource.dynamicrepositories.api.Param;
import de.etecture.opensource.dynamicrepositories.api.Query;
import de.etecture.opensource.dynamicrepositories.api.ResultConverter;
import de.etecture.opensource.dynamicrepositories.spi.QueryExecutorResolver;
import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import de.etecture.opensource.dynamicrepositories.spi.Technology;
import de.etecture.opensource.dynamicresources.extension.DefaultQueryMetaData;
import de.etecture.opensource.dynamicresources.extension.DefaultRequest;
import de.etecture.opensource.dynamicresources.extension.VerbLiteral;
import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import de.etecture.opensource.dynamicresources.test.api.Expect;
import de.etecture.opensource.dynamicresources.test.api.ParamSet;
import de.etecture.opensource.dynamicresources.test.api.ParamSets;
import de.etecture.opensource.dynamicresources.test.api.Request;
import de.etecture.opensource.dynamicresources.test.api.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import junit.framework.AssertionFailedError;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runners.model.FrameworkMethod;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class ResourceTestMethod extends FrameworkMethod {

    private final WeldContainer container;
    private final Request request;
    private final BeanManager bm;
    private final QueryExecutorResolver queryExecutors;
    private final Map<String, Object> requestParameter;
    private final ResourceMethodHandler handler;
    private final Expect expect;

    public ResourceTestMethod(WeldContainer container, Method method) {
        super(method);
        this.container = container;
        this.bm = container.getBeanManager();
        this.queryExecutors = container.instance()
                .select(QueryExecutorResolver.class).get();
        request = method.getAnnotation(Request.class);
        expect = method.getAnnotation(Expect.class);
        try {
            requestParameter = buildParameter();
        } catch (Exception ex) {
            throw new IllegalStateException("cannot build the parameters:", ex);
        }
        final Set<Bean<?>> beans =
                bm.getBeans(ResourceMethodHandler.class,
                new VerbLiteral(request.method()));
        Bean<ResourceMethodHandler> b = (Bean<ResourceMethodHandler>) bm
                .resolve(
                beans);
        this.handler = b.create(bm
                .createCreationalContext(b));
    }

    @Override
    public Object invokeExplosively(final Object target, Object... params)
            throws Throwable {
        try {
            executeQueries("prepare_", request.before());
            final Object[] newParams = new Object[getMethod()
                    .getParameterTypes().length];
            if (params.length > 0) {
                System.arraycopy(params, 0, newParams, 0, params.length);
            }
            int i = -1;
            for (Annotation[] parameterAnnotations : getMethod()
                    .getParameterAnnotations()) {
                i++;
                for (Annotation parameterAnnotation : parameterAnnotations) {
                    if (Response.class.isAssignableFrom(parameterAnnotation
                            .annotationType())) {
                        Class<?> paramType =
                                getMethod().getParameterTypes()[i];
                        final de.etecture.opensource.dynamicresources.api.Response<?> response =
                                requestResource(request.resource());
                        checkExpect(response);
                        if (de.etecture.opensource.dynamicresources.api.Response.class
                                .isAssignableFrom(paramType)) {
                            newParams[i] = response;
                        } else if (request.resource()
                                .isAssignableFrom(paramType)) {
                            try {
                                newParams[i] = response.getEntity();
                            } catch (Exception exception) {
                                newParams[i] = null;
                            }
                        } else if (Exception.class.isAssignableFrom(paramType)) {
                            try {
                                response.getEntity();
                                newParams[i] = null;
                            } catch (Exception ex) {
                                newParams[i] = ex;
                            }
                        } else {
                            throw new IllegalStateException(
                                    "Can only handle a resource-interface or Response as type for response-injection!");
                        }
                    } else if (Param.class.isAssignableFrom(parameterAnnotation
                            .annotationType())) {
                        Param param = (Param) parameterAnnotation;
                        if (requestParameter.containsKey(param.name())) {
                            newParams[i] = ConvertUtils.convert(requestParameter
                                    .get(param.name()), getMethod()
                                    .getParameterTypes()[i]);
                        } else {
                            newParams[i] = param.generator().newInstance()
                                    .generateValue(param);
                        }
                    }
                }
            }
            return super.invokeExplosively(target, newParams);
        } finally {
            executeQueries("cleanup_", request.after());
        }
    }

    private Map<String, Object> getParameterForSet(String name) throws Exception {
        Class<?> testClass = super.getMethod()
                .getDeclaringClass();
        Map<String, Object> parameter = new HashMap<>();
        if (testClass.isAnnotationPresent(ParamSets.class)) {
            for (ParamSet set : testClass.getAnnotation(ParamSets.class).value()) {
                if (set.name().equals(name)) {
                    parameter.putAll(getParameterForParameterSet(set));
                }
            }
        }
        if (testClass.isAnnotationPresent(ParamSet.class)) {
            ParamSet set = testClass.getAnnotation(ParamSet.class);
            if (set.name().equals(name)) {
                parameter.putAll(getParameterForParameterSet(set));
            }
        }
        return parameter;
    }

    private Map<String, Object> getParameterForParameterSet(ParamSet set) throws
            Exception {
        Map<String, Object> parameter = new HashMap<>();
        for (String include : set.includes()) {
            parameter.putAll(getParameterForSet(include));
        }
        for (Param param : set.pathParameter()) {
            parameter.put(param.name(), param.generator()
                    .newInstance().generateValue(param));
        }
        for (Param param : set.queryParameter()) {
            parameter.put(param.name(), param.generator()
                    .newInstance().generateValue(param));
        }
        return parameter;
    }

    private Map<String, Object> buildParameter() throws Exception {
        Map<String, Object> parameter = new HashMap<>();
        if (StringUtils.isNotBlank(request.parameterSet())) {
            parameter.putAll(getParameterForSet(request.parameterSet()));
        }
        for (Param param : request.pathParameter()) {
            parameter.put(param.name(), param.generator()
                    .newInstance().generateValue(param));
        }
        for (Param param : request.queryParameter()) {
            parameter.put(param.name(), param.generator()
                    .newInstance().generateValue(param));
        }
        return parameter;
    }

    private <T> de.etecture.opensource.dynamicresources.api.Response<T> requestResource(
            Class<T> responseType) throws
            Exception {
        return handler.handleRequest(DefaultRequest.fromMethod(responseType,
                request.method()).addParameter(requestParameter)
                .withRequestContent(request.bodyGenerator().newInstance()
                .generateBody(
                request, requestParameter)).build());
    }

    private boolean executeQueries(String prefix, Query... queries) throws
            Exception {
        if (queries.length > 0) {
            for (Query query : request.before()) {
                if (!executeQuery(query.value(), prefix + getName(), query
                        .technology(), query.connection())) {
                    return false;
                }
            }
        } else {
            final ResourceBundle bundle =
                    ResourceBundle.getBundle(super.getMethod()
                    .getDeclaringClass().getName());
            if (bundle.containsKey(prefix + getName())) {
                String technology;
                if (getMethod().getDeclaringClass().isAnnotationPresent(
                        Technology.class)) {
                    technology = getMethod().getDeclaringClass().getAnnotation(
                            Technology.class).value();
                } else {
                    technology = "default";
                }
                return executeQuery(bundle.getString(prefix + getName()), "",
                        technology,
                        "default");
            }
        }
        return true;
    }

    private boolean executeQuery(String query, String queryName,
            String technology,
            String connection) throws Exception {
        final DefaultQueryMetaData qm =
                new DefaultQueryMetaData(Boolean.class,
                QueryMetaData.Kind.RETRIEVE, query, queryName,
                technology,
                connection,
                requestParameter);
        qm.addParameter("request", request.bodyGenerator()
                .newInstance().generateBody(
                request, requestParameter));
        qm.setRepositoryClass(super.getMethod().getDeclaringClass());
        qm.setConverter(new ResultConverter() {
            @Override
            public Object convert(Class returnType, Type genericReturnType,
                    Object result) {
                return result != null;
            }
        });
        if (StringUtils.isBlank(technology) || "default".equalsIgnoreCase(
                technology)) {
            return ((Boolean) queryExecutors
                    .getDefaultExecutor()
                    .execute(qm));
        } else {
            return ((Boolean) queryExecutors.getQueryExecutorForTechnology(
                    technology).execute(qm));
        }
    }

    private void checkExpect(
            de.etecture.opensource.dynamicresources.api.Response<?> response) {
        if (expect != null) {
            if (expect.status() >= 0 && response.getStatus() != expect.status()) {
                throw new AssertionFailedError("expected status is " + expect
                        .status() + " but actual status is " + response
                        .getStatus());
            }
            try {
                Object entity = response.getEntity();
                if (entity != null && !expect.responseType().isAssignableFrom(
                        entity.getClass())) {
                    throw new AssertionFailedError(
                            "response entity not of type " + expect
                            .responseType().getName());
                }
            } catch (Throwable ex) {
                for (Class<? extends Throwable> exc : expect.exception()) {
                    if (exc.isAssignableFrom(ex.getClass())) {
                        return;
                    }
                }
                throw new AssertionFailedError(
                        "response exception is not of type " + Arrays.toString(
                        expect.exception()));
            }
        }
    }
}
