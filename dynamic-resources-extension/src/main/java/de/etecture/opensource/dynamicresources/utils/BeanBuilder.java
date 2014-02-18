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
package de.etecture.opensource.dynamicresources.utils;

import de.etecture.opensource.dynamicrepositories.utils.DefaultLiteral;
import de.etecture.opensource.dynamicrepositories.utils.NamedLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 *
 * @param <T>
 * @author rhk
 * @version
 * @since
 */
public class BeanBuilder<T> {

    private final Logger LOG = Logger.getLogger("BeanBuilder");
    private final BeanManager beanManager;
    private final Class<T> beanClass;
    private String name;
    private Set<Type> beanTypes = new HashSet<>();
    private Set<Annotation> qualifiers = new HashSet<>();
    private Set<Class<? extends Annotation>> stereotypes = new HashSet<>();
    private Class<? extends Annotation> scope;
    private boolean alternative;
    private boolean nullable;
    private BeanCreator creator = new NullBeanCreator();
    private BeanDestroyer destroyer = new DefaultBeanDestroyer();

    private BeanBuilder(BeanManager beanManager, Class<T> beanClass) {
        this.beanManager = beanManager;
        this.beanClass = beanClass;
        this.beanTypes.addAll(Arrays.asList(beanClass.getGenericInterfaces()));
        if (beanClass.getGenericSuperclass() != null) {
            this.beanTypes.add(beanClass.getGenericSuperclass());
        }
        this.beanTypes.add(beanClass);
        this.name = beanClass.getSimpleName();
        this.alternative = false;
        this.nullable = false;
    }

    public static <X> BeanBuilder<X> forClass(BeanManager beanManager,
            Class<X> beanClass) {
        return new BeanBuilder(beanManager, beanClass);
    }

    public static <X> BeanBuilder<X> forInstance(BeanManager beanManager,
            X beanInstance) {
        final BeanBuilder beanBuilder = forClass(beanManager, beanInstance
                .getClass());
        beanBuilder.creator = new SingletonBeanCreator(beanInstance);
        return beanBuilder;
    }

    public static <X> BeanBuilder<X> forInstanceWithName(BeanManager beanManager,
            X beanInstance,
            String name) {
        final BeanBuilder beanBuilder = forClassAndName(beanManager,
                beanInstance.getClass(),
                name);
        beanBuilder.qualifiers.add(new NamedLiteral(name));
        beanBuilder.creator = new SingletonBeanCreator(beanInstance);
        return beanBuilder;
    }

    public static <X> BeanBuilder<X> forInstance(BeanManager beanManager,
            Class<X> beanClass,
            X beanInstance) {
        final BeanBuilder beanBuilder = forClass(beanManager, beanClass);
        beanBuilder.creator = new SingletonBeanCreator(beanInstance);
        return beanBuilder;
    }

    public static <X> BeanBuilder<X> forInstanceWithName(BeanManager beanManager,
            Class<X> beanClass,
            X beanInstance,
            String name) {
        final BeanBuilder beanBuilder = forClassAndName(beanManager, beanClass,
                name);
        beanBuilder.qualifiers.add(new NamedLiteral(name));
        beanBuilder.creator = new SingletonBeanCreator(beanInstance);
        return beanBuilder;
    }

    public static <X> BeanBuilder<X> forClassAndName(BeanManager beanManager,
            Class<X> beanClass,
            String name) {
        final BeanBuilder beanBuilder = new BeanBuilder(beanManager, beanClass);
        beanBuilder.qualifiers.add(new NamedLiteral(name));
        beanBuilder.name = name;
        return beanBuilder;
    }

    public BeanBuilder<T> withName(String name) {
        this.name = name;
        return this;
    }

    public BeanBuilder<T> withNamed(String name) {
        this.qualifiers.add(new NamedLiteral(name));
        return this;
    }

    public BeanBuilder<T> withTypes(Type type, Type... moreTypes) {
        this.beanTypes.add(type);
        this.beanTypes.addAll(Arrays.asList(moreTypes));
        return this;
    }

    public BeanBuilder<T> withTypes(Type[] moreTypes) {
        this.beanTypes.addAll(Arrays.asList(moreTypes));
        return this;
    }

    public BeanBuilder<T> withQualifier(Annotation qualifier,
            Annotation... moreQualifiers) {
        this.qualifiers.add(qualifier);
        this.qualifiers.addAll(Arrays.asList(moreQualifiers));
        return this;
    }

    public BeanBuilder<T> withQualifier(Annotation[] moreQualifiers) {
        this.qualifiers.addAll(Arrays.asList(moreQualifiers));
        return this;
    }

    public BeanBuilder<T> withQualifier(Set<Annotation> qualifiers) {
        this.qualifiers.addAll(qualifiers);
        return this;
    }

    public BeanBuilder<T> withDefault() {
        this.qualifiers.add(new DefaultLiteral());
        return this;
    }

    public BeanBuilder<T> withAny() {
        this.qualifiers.add(new AnyLiteral());
        return this;
    }

    public BeanBuilder<T> withStereotype(Class<? extends Annotation> stereotype,
            Class<? extends Annotation>... moreStereotypes) {
        this.stereotypes.add(stereotype);
        this.stereotypes.addAll(Arrays.asList(moreStereotypes));
        return this;
    }

    public BeanBuilder<T> withScope(Class<? extends Annotation> scope) {
        this.scope = scope;
        return this;
    }

    public BeanBuilder<T> applicationScoped() {
        return withScope(ApplicationScoped.class);
    }

    public BeanBuilder<T> requestScoped() {
        return withScope(RequestScoped.class);
    }

    public BeanBuilder<T> conversationScoped() {
        return withScope(ConversationScoped.class);
    }

    public BeanBuilder<T> dependent() {
        return withScope(Dependent.class);
    }

    public BeanBuilder<T> createdByReflection() {
        this.creator = new ReflectionBeanCreator();
        return this;
    }

    public BeanBuilder<T> createdBy(BeanCreator creator) {
        this.creator = creator;
        return this;
    }

    public BeanBuilder<T> destroyedBy(BeanDestroyer destroyer) {
        this.destroyer = destroyer;
        return this;
    }

    public BeanBuilder<T> use(BeanCreator creator, BeanDestroyer destroyer) {
        this.creator = creator;
        this.destroyer = destroyer;
        return this;
    }

    public Bean<T> build(Class<? extends Annotation> scope) {
        this.scope = scope;
        return build();
    }

    public Bean<T> build() {
        AbstractBean<T> bean = new AbstractBean<T>(beanManager, beanClass, name,
                scope,
                alternative, nullable, beanTypes.toArray(new Type[beanTypes
                .size()])) {
            @Override
            public T create(
                    CreationalContext<T> creationalContext) {
                final T instance =
                        BeanBuilder.this.creator.create(beanManager, this,
                        creationalContext);
                creationalContext.push(instance);
                return instance;
            }

            @Override
            public void destroy(T instance,
                    CreationalContext<T> creationalContext) {
                BeanBuilder.this.destroyer.destroy(beanManager, this, instance,
                        creationalContext);
                creationalContext.release();
            }
        };
        for (Annotation qualifier : qualifiers) {
            bean.addQualifier(qualifier);
        }
        for (Class<? extends Annotation> stereotype : stereotypes) {
            bean.addStereotype(stereotype);
        }
        return bean;
    }

    public BeanBuilder<T> asAlternative() {
        this.alternative = true;
        return this;
    }

    public BeanBuilder<T> nullable() {
        this.nullable = true;
        return this;
    }

    public BeanBuilder<T> notNullable() {
        this.nullable = false;
        return this;
    }
}
