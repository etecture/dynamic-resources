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
package de.etecture.opensource.dynamicresources.scanner;

import de.etecture.opensource.dynamicresources.annotations.declaration.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.api.UriBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
public class ResponseWriterBean<T> implements Bean<ResponseWriter<T>> {

    private final String name;
    private final ResponseWriter<T> instance;
    private final Produces producesLiteral;
    private final BeanManager beanManager;

    public ResponseWriterBean(
            BeanManager beanManager,
            Produces producesLiteral,
            ResponseWriter<T> instance) {
        this(beanManager, producesLiteral, instance, getNameOfBean(instance));
    }

    public ResponseWriterBean(
            BeanManager beanManager,
            Produces producesLiteral,
            ResponseWriter<T> instance,
            String name) {
        this.beanManager = beanManager;
        this.instance = instance;
        this.producesLiteral = producesLiteral;
        this.name = name;
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<>();
        types.add(ResponseWriter.class);
        types.add(Object.class);
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(new AnnotationLiteral<Any>() {
            private static final long serialVersionUID = 1L;
        });
        qualifiers.add(producesLiteral);
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getBeanClass() {
        return ResponseWriter.class;
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public ResponseWriter<T> create(CreationalContext ctx) {
        inject(instance.getClass(), instance);
        return instance;
    }

    @Override
    public void destroy(
            ResponseWriter<T> instance,
            CreationalContext<ResponseWriter<T>> ctx) {
        ctx.release();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ResponseWriterBean '").
                append(name).
                append("' for entities with type ").
                append(producesLiteral.contentType().getSimpleName()).
                append(" that produces ");
        for (int i = 0; i < producesLiteral.mimeType().length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(producesLiteral.mimeType()[i]);
        }
        if (StringUtils.isNotBlank(producesLiteral.version())) {
            sb.append(" in version: ").append(producesLiteral.version());
        }
        return sb.toString();
    }

    private static String getNameOfBean(ResponseWriter<?> instance) {
        if (instance.getClass().isAnnotationPresent(Named.class)) {
            return instance.getClass().getAnnotation(Named.class).value();
        } else {
            return instance.getClass().getName();
        }
    }

    protected void inject(Class<?> clazz, ResponseWriter<?> instance) throws
            CreationException, SecurityException {
        if (clazz.getSuperclass() != null && clazz.getSuperclass()
                != Object.class) {
            inject(clazz.getSuperclass(), instance);
        }
        for (Field field : clazz.getDeclaredFields()) {
            checkFieldWithType(field, UriBuilder.class, instance);
            checkFieldWithType(field, HttpServletRequest.class, instance);
            checkFieldWithType(field, HttpServletResponse.class, instance);
        }
    }

    protected void checkFieldWithType(Field field,
            final Class<?> beanType,
            ResponseWriter<?> instance) throws CreationException,
            SecurityException {
        if (field.isAnnotationPresent(Inject.class) && beanType
                .isAssignableFrom(field.getType())) {
            List<Annotation> qualifiers = new ArrayList<>();
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation.annotationType().isAnnotationPresent(
                        Qualifier.class)) {
                    qualifiers.add(annotation);
                }
            }
            Set<Bean<?>> beans =
                    beanManager.getBeans(beanType, qualifiers
                    .toArray(
                    new Annotation[qualifiers.size()]));
            Bean bean = beanManager.resolve(beans);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                field.set(instance, bean.create(beanManager
                        .createCreationalContext(bean)));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new CreationException(ex);
            }
        }
    }
}
