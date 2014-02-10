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
package de.etecture.opensource.dynamicresources.scanner;

import de.etecture.opensource.dynamicresources.annotations.declaration.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.utils.BeanBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * this CDI-{@link Extension} is responsible for scanning all classes to find
 * {@link ResponseWriter}s.
  *
 * @author rhk
 * @version
 * @since
 */
public class ResponseWriterScanner implements Extension {

    private static final Logger LOG = Logger.getLogger(
            "ResponseWriterScanner");
    private Set<Bean<? extends ResponseWriter<?>>> responseWriterBeans =
            new HashSet<>();

    /**
     * will be called by the CDI-container to inform about an annotated type
     * that we have to scan.
     *
     * @param <T>
     * @param pat
     * @param bm
     * @throws Exception
     */
    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat,
            BeanManager bm) throws
            Exception {

        // look, if the type is a subtype of ResponseWriter.
        if (ResponseWriter.class
                .isAssignableFrom(pat.getAnnotatedType().getJavaClass())) {

            // check, if it is an enum.
            if (pat.getAnnotatedType().getJavaClass().isEnum()) {
                processResponseWriterEnumLiteral(pat, bm);
            } else if (pat.getAnnotatedType()
                    .isAnnotationPresent(Produces.class)) {
                // else: it's a normal class, so create a bean for it.
                addResponseWriterBean((Class<? extends ResponseWriter<?>>) pat
                        .getAnnotatedType().getJavaClass(), pat
                        .getAnnotatedType().getAnnotation(Produces.class), bm);
            }
        }
    }

    /**
     * will be called by the CDI-Container to let us register all of our scanned
     * beans.
     *
     * @param abd
     * @param bm
     */
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        // register the responseWriter beans
        for (Bean<?> rwb : responseWriterBeans) {
            LOG.log(Level.FINE, "register responsewriter: {0}", rwb.getName());
            abd.addBean(rwb);
        }
    }

    private void addResponseWriterBean(
            Class<? extends ResponseWriter<?>> responseWriterType,
            Produces produces, BeanManager bm) {
        addResponseWriterBean(responseWriterType, responseWriterType
                .getSimpleName(), produces, bm);
    }

    private void addResponseWriterBean(
            Class<? extends ResponseWriter<?>> responseWriterType,
            String name,
            Produces produces, BeanManager bm) {
        this.responseWriterBeans.add(
                BeanBuilder.forClassAndName(responseWriterType, name)
                .withQualifier(produces)
                .withAny()
                .withTypes(Object.class, responseWriterType,
                ResponseWriter.class)
                .applicationScoped()
                .createdByReflectionAndInjection(bm)
                .build());
    }

    private void addResponseWriterBean(
            ResponseWriter<?> responseWriter,
            String name,
            Produces produces) {
        this.responseWriterBeans.add(
                BeanBuilder.forInstanceWithName(responseWriter, name)
                .withQualifier(produces)
                .withAny()
                .withTypes(Object.class,
                ResponseWriter.class)
                .applicationScoped()
                .build());
    }

    /**
     * processes a type that is a ResponseWriter defined as an enum literal.
     *
     * @param <T>
     * @param pat
     * @param bm
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private <T> void processResponseWriterEnumLiteral(
            ProcessAnnotatedType<T> pat, BeanManager bm) throws
            IllegalAccessException,
            IllegalArgumentException {

        // scan the enum literals. these are represented as fields in the type.
        for (AnnotatedField af : pat.getAnnotatedType().getFields()) {

            // check if the field is annotated with @Produces
            if (af.isAnnotationPresent(Produces.class)) {
                final String name = String.format("%s_%s",
                        pat.getAnnotatedType().getJavaClass().
                        getSimpleName(), af.getJavaMember().getName());
                LOG.log(Level.FINER,
                        "found ResponseWriter for Entity: {0} with name: {1}",
                        new Object[]{
                    af.getAnnotation(Produces.class).contentType().getName(),
                    name});

                // get the ResponseWriter instance, the enum literal represents.
                final ResponseWriter responseWriter =
                        (ResponseWriter) af.getJavaMember().get(pat
                        .getAnnotatedType().getJavaClass());

                // add the ResponseWriter bean.
                addResponseWriterBean(responseWriter, name, af
                        .getAnnotation(
                        Produces.class));
            }
        }
    }

}
