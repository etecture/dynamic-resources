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
package de.etecture.opensource.dynamicresources.core.executors;

import de.etecture.opensource.dynamicrepositories.api.annotations.ParamName;
import de.etecture.opensource.dynamicresources.annotations.Body;
import de.etecture.opensource.dynamicresources.annotations.Executes;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.DefinitionException;

/**
 *
 * @param <T>
 * @author rhk
 * @version
 * @since
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class ExecutionMethod<T> implements AnnotatedMethod<T>, Executes {

    private final AnnotatedMethod<T> method;
    private final AnnotatedParameter<T> executionContextArgument;
    private final AnnotatedParameter<T> bodyArgument;
    private final Map<AnnotatedParameter<T>, String> parameterArgument =
            new HashMap<>();

    public ExecutionMethod(
            AnnotatedMethod<T> method) throws DefinitionException {
        this.method = method;
        // check the basics
        if (Modifier.isFinal(method.getJavaMember().getModifiers())) {
            throw new DefinitionException(
                    String.format(
                    "a method annotated with @Executes must not be static, but %s is.",
                    method));
        }
        if (Modifier.isStatic(method.getJavaMember().getModifiers())) {
            throw new DefinitionException(
                    String.format(
                    "a method annotated with @Executes must not be static, but%s is.",
                    method));
        }
        // fetch the arguments
        Set<AnnotatedParameter<T>> executionContextArguments = new HashSet<>();
        Set<AnnotatedParameter<T>> bodyArguments = new HashSet<>();
        for (AnnotatedParameter<T> param : method.getParameters()) {
            if (param.getBaseType() instanceof ParameterizedType) {
                if (ExecutionContext.class == ((ParameterizedType) param
                        .getBaseType()).getRawType()) {
                    executionContextArguments.add(param);
                }
                if (param.isAnnotationPresent(Body.class)) {
                    bodyArguments.add(param);
                }
                if (param.isAnnotationPresent(ParamName.class)) {
                    parameterArgument.put(param, param.getAnnotation(
                            ParamName.class).value());
                }
            }
        }
        if (executionContextArguments.size() > 1) {
            throw new DefinitionException(
                    String.format(
                    "a method annotated with @Executes may contain zero or exactly one arguments with type ExecutionContext. But the %s contains: %s",
                    method, Arrays.toString(executionContextArguments.toArray())));
        } else if (executionContextArguments.isEmpty()) {
            this.executionContextArgument = null;
        } else {
            this.executionContextArgument = executionContextArguments.iterator()
                    .next();
        }
        if (bodyArguments.size() > 1) {
            throw new DefinitionException(
                    String.format(
                    "a method annotated with @Executes may contain zero or exactly one arguments annotated with @Body. But the %s contains: %s",
                    method, Arrays.toString(bodyArguments.toArray())));
        } else if (bodyArguments.isEmpty()) {
            this.bodyArgument = null;
        } else {
            this.bodyArgument = bodyArguments.iterator()
                    .next();
        }
        if (this.executionContextArgument == this.bodyArgument
                && this.bodyArgument != null) {
            throw new DefinitionException(String.format(
                    "execution context argument must not be annotated with @Body, but %s is!",
                    bodyArgument));
        }
    }

    @Override
    public Method getJavaMember() {
        return method.getJavaMember();
    }

    @Override
    public List<AnnotatedParameter<T>> getParameters() {
        return method.getParameters();
    }

    @Override
    public boolean isStatic() {
        return method.isStatic();
    }

    @Override
    public AnnotatedType<T> getDeclaringType() {
        return method.getDeclaringType();
    }

    @Override
    public Type getBaseType() {
        return method.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure() {
        return method.getTypeClosure();
    }

    @Override
    public <T extends Annotation> T getAnnotation(
            Class<T> annotationType) {
        return method.getAnnotation(annotationType);
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return method.getAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(
            Class<? extends Annotation> annotationType) {
        return method.isAnnotationPresent(annotationType);
    }

    public AnnotatedParameter<T> getBodyArgument() {
        return bodyArgument;
    }

    public AnnotatedParameter<T> getExecutionContextArgument() {
        return executionContextArgument;
    }

    public Map<AnnotatedParameter<T>, String> getParameterArguments() {
        return parameterArgument;
    }

    @Override
    public String method() {
        return method.getAnnotation(Executes.class).method();
    }

    @Override
    public String resource() {
        return method.getAnnotation(Executes.class).resource();
    }

    @Override
    public String application() {
        return method.getAnnotation(Executes.class).application();
    }

    @Override
    public Class<?> responseType() {
        if (method.getJavaMember().getReturnType() != Void.class && method
                .getJavaMember().getReturnType() != Void.TYPE && !Response.class
                .isAssignableFrom(method.getJavaMember().getReturnType())) {
            return method.getJavaMember().getReturnType();
        }
        return method.getAnnotation(Executes.class).responseType();
    }

    @Override
    public Class<?> requestType() {
        if (bodyArgument != null) {
            return method.getJavaMember().getParameterTypes()[bodyArgument
                    .getPosition()];
        }
        return method.getAnnotation(Executes.class).requestType();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Executes.class;
    }

    public boolean matches(Application application) {
        return application.getName().matches(application());
    }

    public boolean matches(Resource resource) {
        return resource.getName().matches(resource());
    }

    public boolean matches(ResourceMethod method) {
        return matches(method.getResource().getApplication())
                && matches(method.getResource())
                && method.getName().matches(method())
                && method.getResponses().containsKey(responseType())
                && method.getRequests().containsKey(requestType());
    }

    @Override
    public String toString() {
        return method.toString();
    }
}
