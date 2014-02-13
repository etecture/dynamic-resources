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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class BeanInstanceBuilder<T> {

    private final InjectionTarget<T> injectionTarget;
    private final Class<T> type;
    private Constructor<T> constructor = null;
    private final Map<Field, Object> fieldsToInject = new HashMap<>();
    private Object[] initArguments = new Object[0];
    private CreationalContext<T> ctx = null;

    private BeanInstanceBuilder(Class<T> type,
            InjectionTarget<T> injectionTarget) {
        this.injectionTarget = injectionTarget;
        this.type = type;
    }

    public BeanInstanceBuilder<T> usingCreationalContext(
            CreationalContext<T> ctx) {
        this.ctx = ctx;
        return this;
    }

    public BeanInstanceBuilder<T> usingConstructor(Class<?>... parameterTypes)
            throws NoSuchMethodException {
        this.constructor = type.getConstructor(parameterTypes);
        return this;
    }

    public BeanInstanceBuilder<T> injectField(String fieldName, Object value)
            throws NoSuchFieldException {
        fieldsToInject.put(this.type.getDeclaredField(fieldName), value);
        return this;
    }

    public BeanInstanceBuilder<T> withInitArguments(Object... initArguments) {
        this.initArguments = initArguments;
        return this;
    }

    private Constructor lookupConstructor() throws NoSuchMethodException {
        outer:
        for (Constructor ctr : type.getDeclaredConstructors()) {
            Class[] parameterTypes = ctr.getParameterTypes();
            if (parameterTypes.length == initArguments.length) {
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!parameterTypes[i].isInstance(initArguments[0])) {
                        continue outer;
                    }
                }
                return ctr;
            }
        }
        throw new NoSuchMethodException("cannot find a constructor!");
    }

    private T produce() throws InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        if (this.constructor == null) {
            this.constructor = lookupConstructor();
        }
        return this.constructor.newInstance(initArguments);
    }

    public T build(Object... parameterValues) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        withInitArguments(parameterValues);
        T instance = produce();
        for (Map.Entry<Field, Object> fieldToInject : fieldsToInject.entrySet()) {
            fieldToInject.getKey().setAccessible(true);
            fieldToInject.getKey().set(instance, fieldToInject.getValue());
        }
        if (injectionTarget != null && ctx != null) {
            injectionTarget.inject(instance, ctx);
            injectionTarget.postConstruct(instance);
        }
        return instance;
    }

    public static <X> BeanInstanceBuilder<X> forType(Class<X> type) {
        return new BeanInstanceBuilder(type, null);
    }

    public static <X> BeanInstanceBuilder<X> forBeanType(Class<X> beanType,
            BeanManager beanManager) {
        return new BeanInstanceBuilder(beanType, beanManager
                .createInjectionTarget(beanManager.createAnnotatedType(beanType)));
    }
}
