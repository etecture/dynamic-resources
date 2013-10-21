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

import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @param <A>
 * @author rhk
 */
public abstract class AbstractReflectiveResourceMethodHandler<A extends Annotation>
        extends AbstractResourceMethodHandler {

    private final Class<A> annotationClass;

    protected AbstractReflectiveResourceMethodHandler(Class<A> annotationClass,
            QueryMetaData.Kind kind) {
        super(annotationClass.getSimpleName(), kind);
        this.annotationClass = annotationClass;
    }

    @Override
    public boolean isAvailable(
            Class<?> resourceClazz) {
        System.out.println("check, if " + annotationClass.getSimpleName()
                + " is available for " + resourceClazz.getSimpleName());
        return resourceClazz.isAnnotationPresent(annotationClass);
    }

    private A getAnnotation(Class<?> resourceClass) {
        return resourceClass.getAnnotation(annotationClass);
    }

    private <T> T getAnnotationValue(String name, Class<?> resourceClass) {
        try {
            return (T) this.annotationClass.getMethod(name).invoke(
                    getAnnotation(
                    resourceClass));
        } catch (NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException ex) {
            throw new RuntimeException(
                    String.format(
                    "bad annotation class for reflective resource method handler! Does not specify a method: %s()!",
                    name),
                    ex);
        }
    }

    @Override
    public String getDescription(
            Class<?> resourceClass) {
        return getAnnotationValue("description", resourceClass);
    }

    @Override
    protected String getTechnology(Class<?> resourceClass) {
        return getAnnotationValue("technology", resourceClass);
    }

    @Override
    protected int getDefaultStatus(Class<?> resourceClass) {
        return getAnnotationValue("status", resourceClass);
    }

    @Override
    protected String getQuery(
            Class<?> resourceClass) {
        return getAnnotationValue("query", resourceClass);
    }

    @Override
    protected String getQueryName(
            Class<?> resourceClass) {
        String queryName = getAnnotationValue("queryName", resourceClass);
        if (StringUtils.isBlank(queryName)) {
            return getClass().getAnnotation(Verb.class).value();
        }
        return queryName;
    }

    @Override
    protected Class<?> getRequestType(
            Class<?> resourceClazz) {
        Class<?> clazz = getAnnotationValue("requestType", resourceClazz);
        if (clazz == Class.class) {
            return resourceClazz;
        } else {
            return clazz;
        }
    }
}
