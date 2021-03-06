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

import com.google.common.io.NullOutputStream;
import de.etecture.opensource.dynamicrepositories.api.annotations.Hint;
import de.etecture.opensource.dynamicrepositories.api.annotations.Param;
import de.etecture.opensource.dynamicrepositories.api.annotations.Query;
import de.etecture.opensource.dynamicrepositories.executor.Technology;
import de.etecture.opensource.dynamicrepositories.extension.DefaultQueryExecutionContext;
import de.etecture.opensource.dynamicrepositories.extension.QueryExecutors;
import de.etecture.opensource.dynamicrepositories.metadata.AnnotatedQueryDefinition;
import de.etecture.opensource.dynamicrepositories.metadata.DefaultQueryDefinition;
import de.etecture.opensource.dynamicrepositories.metadata.QueryDefinition;
import de.etecture.opensource.dynamicresources.api.BooleanResult;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.TypedResourceAccessor;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.test.api.Expect;
import de.etecture.opensource.dynamicresources.test.api.Request;
import de.etecture.opensource.dynamicresources.test.api.Response;
import de.etecture.opensource.dynamicresources.test.utils.Nop;
import de.etecture.opensource.dynamicresources.utils.ResourceLiteral;
import de.herschke.converters.api.Converters;
import de.herschke.testhelper.ConsoleWriter;
import de.herschke.testhelper.ConsoleWriter.Color;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Qualifier;
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

    public static final PrintStream newOut = new PrintStream(
            new NullOutputStream());

    private final Request request;

    private final QueryExecutors executors;

    private final Map<String, String> pathParameter = new HashMap<>();

    private final Map<String, Object> queryParameter = new HashMap<>();

    private final Expect expect;

    private final ConsoleWriter out = new ConsoleWriter(System.out, 80);

    private final WeldContainer container;

    private final TypedResourceAccessor<?> accessor;

    private String getEntityType(Object entity) {
        if (entity instanceof Proxy) {
            Class[] intfces = entity.getClass().getInterfaces();
            String[] names = new String[intfces.length];
            for (int i = 0; i < intfces.length; i++) {
                names[i] = intfces[i].getName();
            }
            return Arrays.toString(names);
        } else {
            return entity.getClass()
                    .getCanonicalName();
        }
    }

    private void handleInjections(Object target) {
        for (Field field : super.getMethod().getDeclaringClass()
                .getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    Set<Annotation> qualifiers = new HashSet<>();
                    for (Annotation annotation : field.getAnnotations()) {
                        if (annotation.annotationType().isAnnotationPresent(
                                Qualifier.class)) {
                            qualifiers.add(annotation);
                        }
                    }
                    Object value = container.instance().select(field.getType(),
                                                               qualifiers
                                                               .toArray(
                                                                       new Annotation[qualifiers
                                                                       .size()]))
                            .get();
                    field.setAccessible(true);
                    field.set(target, value);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalStateException("Cannot inject field: "
                            + field, ex);
                }
            }
        }
    }

    private static enum Status {

        PASSED(Color.GREEN),
        FAILED(Color.BROWN),
        ERROR(Color.RED);

        private final Color color;

        private Status(Color color) {
            this.color = color;
        }

        public Color color() {
            return this.color;
        }
    }

    public ResourceTestMethod(WeldContainer container, Method method) {
        super(method);
        try {
            this.container = container;
            this.executors = container.instance()
                    .select(QueryExecutors.class).get();
            request = method.getAnnotation(Request.class);
            expect = method.getAnnotation(Expect.class);
            accessor = container.instance().select(ResourceAccessor.class,
                                                   //new ApplicationLiteral(request.application()),
                                                   new ResourceLiteral(request
                                                           .resource())).get()
                    .select(request.responseType());
            buildParameter();
        } catch (ResponseTypeNotSupportedException ex) {
            throw new IllegalArgumentException(ex);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Object invokeExplosively(final Object target, Object... params)
            throws Throwable {
        final PrintStream oldOut = System.out;
        final PrintStream oldErr = System.err;
        Status status = Status.PASSED;
        System.setOut(newOut);
        System.setErr(newOut);
        try {
            out.startBox(super.getMethod().getDeclaringClass().getSimpleName()
                    + "#" + super.getName());
            out.printLeft("preparing database");
            executeQueries("prepare_", request.before());
            out.printRight('.', Color.GREEN, "done");
            if (request.beforeRequest() != Nop.class) {
                out.printLeft("invoke: %s", request.beforeRequest()
                              .getSimpleName());
                container.instance().select(request.beforeRequest()).get().run();
                out.printRight('.', Color.GREEN, "done");
            }
            out.printLeft("invoke: %s %s",
                          request.method(), request.resource());
            final de.etecture.opensource.dynamicresources.api.Response<?> response
                    = requestResource(request.responseType());
            out.printRight('.', Color.GREEN, "done");
            out.printLeft("checking response");
            Object entity;
            try {
                entity = checkExpect(response);
                out.printRight('.', Color.GREEN, "done");
            } catch (Throwable t) {
                out.printRight('.', Color.RED, "failed");
                status = Status.FAILED;
                throw t;
            }
            if (response == null) {
                out.println("  --> Status: <null>");
            } else {
                out.println("  --> Status: %d", response.getStatus());
            }
            if (entity != null) {
                out.println("  --> Type: %s", getEntityType(entity));
                out.println("  --> Entity:");
                if (entity instanceof Throwable) {
                    out.println("  %s", StringUtils.abbreviate(
                                ((Throwable) entity).getMessage(), 74));
                } else {
                    out.println("  %s", StringUtils
                                .abbreviate(entity.toString().replaceAll("\n",
                                                                         ""), 74));
                }
            } else {
                out.println("  --> Entity: null");
            }
            out.printLeft("preparing test-method parameters");
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
                        Class<?> paramType = getMethod().getParameterTypes()[i];
                        if (de.etecture.opensource.dynamicresources.api.Response.class
                                .isAssignableFrom(paramType)) {
                            newParams[i] = response;
                        } else if (paramType.isInstance(entity)) {
                            newParams[i] = entity;
                        } else {
                            throw new IllegalStateException(
                                    "Can only handle an entity of type "
                                    + paramType
                                    + " or of type Response as type for response-injection!");
                        }
                    } else if (Param.class.isAssignableFrom(parameterAnnotation
                            .annotationType())) {
                        Param param = (Param) parameterAnnotation;
                        if (pathParameter.containsKey(param.name())) {
                            newParams[i] = container.instance().select(
                                    Converters.class).get().select(getMethod()
                                            .getParameterTypes()[i]).convert(
                                            pathParameter
                                            .get(param.name()));
                        } else if (queryParameter.containsKey(param.name())) {
                            newParams[i] = container.instance().select(
                                    Converters.class).get().select(getMethod()
                                            .getParameterTypes()[i]).convert(
                                            queryParameter
                                            .get(param.name()));
                        } else {
                            newParams[i] = container.instance().select(param
                                    .generator()).get()
                                    .generate(param);
                        }
                    }
                }
            }
            out.printRight('.', Color.GREEN, "done");
            out.printRuler();
            System.setOut(oldOut);
            System.setErr(oldErr);
            handleInjections(target);
            return super.invokeExplosively(target, newParams);
        } catch (AssertionError t) {
            status = Status.FAILED;
            throw t;
        } catch (Throwable t) {
            status = Status.ERROR;
            throw t;
        } finally {
            System.setOut(newOut);
            System.setErr(newOut);
            out.printRuler();
            if (request.afterRequest() != Nop.class) {
                out.printLeft("invoke: %s", request.afterRequest()
                              .getSimpleName());
                container.instance().select(request.afterRequest()).get().run();
                out.printRight('.', Color.GREEN, "done");
            }
            out.printLeft("cleaning up database");
            executeQueries("cleanup_", request.after());
            out.printRight('.', Color.GREEN, "done");
            out.printRuler();
            out.printCentered(status.color(), "%s %s", super.getName(), status
                              .name());
            out.endBox();
            out.println();
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }

    private void buildParameter() throws Exception {
        for (Param param : request.pathParameter()) {
            pathParameter.put(param.name(), (String) container.instance()
                              .select(param.generator()).get().generate(param));
        }
        for (Param param : request.queryParameter()) {
            queryParameter.put(param.name(), container.instance().select(param
                               .generator()).get().generate(param));
        }
    }

    private <T> de.etecture.opensource.dynamicresources.api.Response<T> requestResource(
            Class<T> responseType) throws
            Exception {
        accessor.pathParams(pathParameter);
        MethodAccessor<?> ma = accessor.method(request.method());
        for (Map.Entry<String, Object> entry : queryParameter.entrySet()) {
            ma = ma.queryParam(entry.getKey(), entry.getValue());
        }
        ma.body(container.instance().select(request.bodyGenerator()).get()
                .generateBody(request, pathParameter, queryParameter));
        return (de.etecture.opensource.dynamicresources.api.Response<T>) ma
                .invoke();
    }

    private boolean executeQueries(final String prefix, Query... queries) throws
            Exception {
        if (queries.length > 0) {
            for (Query query : request.before()) {
                AnnotatedQueryDefinition qd
                        = new AnnotatedQueryDefinition(query) {
                            @Override
                            public String getStatement() {
                                return createStatement(getMethod()
                                        .getDeclaringClass(),
                                                       prefix + getName(), super
                                                       .getStatement());
                            }
                        };
                if (!executeQuery(qd)) {
                    return false;
                }
            }
        } else {
            final ResourceBundle bundle = ResourceBundle.getBundle(super
                    .getMethod()
                    .getDeclaringClass().getName());
            if (bundle.containsKey(prefix + getName())) {
                final String technology;
                if (getMethod().getDeclaringClass().isAnnotationPresent(
                        Technology.class)) {
                    technology = getMethod().getDeclaringClass().getAnnotation(
                            Technology.class).value();
                } else {
                    technology = "default";
                }
                return executeQuery(new DefaultQueryDefinition(bundle.getString(
                        prefix + getName())) {
                            @Override
                            public String getTechnology() {
                                return technology;
                            }
                        });
            }
        }
        return true;
    }

    private boolean executeQuery(QueryDefinition qd) throws Exception {
        DefaultQueryExecutionContext<BooleanResult> query
                = new DefaultQueryExecutionContext(BooleanResult.class,
                                                   BooleanResult.class, qd);
        for (Entry<String, String> e : pathParameter.entrySet()) {
            query.addParameter(e.getKey(), e.getValue());
        }
        for (Entry<String, Object> e : queryParameter.entrySet()) {
            query.addParameter(e.getKey(), e.getValue());
        }
        return ((BooleanResult) executors.execute(query)).getResult();
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
            query.addHint(hint.name(), container.instance().select(hint
                          .generator()).get().generate(hint));
        }
    }

    private void addParams(DefaultQueryExecutionContext query, Param... params) {
        for (Param param : params) {
            query.addParameter(param.name(), container.instance()
                               .select(param.generator()).get().generate(param));
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private Object checkExpect(
            de.etecture.opensource.dynamicresources.api.Response<?> response) {
        Object entity;
        Throwable cause;
        try {
            entity = response.getEntity();
            cause = null;
        } catch (Throwable ex) {
            cause = ex;
            entity = ex;
        }
        if (expect == null) {
            return entity;
        } else {
            if (expect.status() >= 0 && response.getStatus() != expect
                    .status()) {
                throw new ExpectationFailedError("expected status is "
                        + expect
                        .status() + " but actual status is " + response
                        .getStatus(), cause);
            }
            if (expect.responseType().isInstance(entity)) {
                return entity;
            } else {
                if (entity instanceof Throwable) {
                    for (Class<? extends Throwable> exc : expect.exception()) {
                        if (exc.isAssignableFrom(entity.getClass())) {
                            return entity;
                        }
                    }
                    throw new ExpectationFailedError(
                            "response exception is not of type " + Arrays
                            .toString(
                                    expect.exception()), cause);
                }
            }
            throw new ExpectationFailedError(
                    "response entity not of type " + expect
                    .responseType().getName(), cause);
        }
    }
}
