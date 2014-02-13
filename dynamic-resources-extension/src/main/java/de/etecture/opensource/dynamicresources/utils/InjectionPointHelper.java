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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public final class InjectionPointHelper {

    private InjectionPointHelper() {
    }

    public static <A extends Annotation> A findAnnotation(
            Class<A> annotationClass,
            InjectionPoint ip) {
        if (ip.getAnnotated().isAnnotationPresent(annotationClass)) {
            return ip.getAnnotated().getAnnotation(annotationClass);
        }
        return null;
    }

    public static <A extends Annotation> A findQualifier(
            Class<A> annotationClass,
            InjectionPoint ip) {
        if (ip.getAnnotated().isAnnotationPresent(annotationClass)) {
            return ip.getAnnotated().getAnnotation(annotationClass);
        } else {
            for (Annotation annotation : ip.getQualifiers()) {
                if (annotationClass.isInstance(annotation)) {
                    return annotationClass.cast(annotation);
                }
            }
        }
        throw new InjectionException("injection point: " + ip
                + " must be qualified with @" + annotationClass.getSimpleName());
    }

    public static Annotation[] findQualifiers(InjectionPoint ip,
            Class<? extends Annotation>... annotationClasses) {
        List<Annotation> qualifiers = new LinkedList<>();
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            if (ip.getAnnotated().isAnnotationPresent(annotationClass)) {
                qualifiers.add(ip.getAnnotated().getAnnotation(annotationClass));
            }
            for (Annotation annotation : ip.getQualifiers()) {
                if (qualifiers.contains(annotation)) {
                    continue;
                }
                if (annotationClass.isInstance(annotation)) {
                    qualifiers.add(annotationClass.cast(annotation));
                }
            }
        }
        return qualifiers.toArray(new Annotation[qualifiers.size()]);
    }

    public static <T> Class<T> getGenericTypeOfInjectionPoint(InjectionPoint ip,
            int number) {
        // try to get the type from the injection point
        if (ip.getType() instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) ip.getType())
                    .getActualTypeArguments()[number];
        } else {
            // not specified, so it is Object.class
            return null;
        }
    }
}
