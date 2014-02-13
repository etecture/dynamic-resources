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
package de.etecture.opensource.dynamicresources.core.scanner;

import de.etecture.opensource.dynamicresources.annotations.Application;
import de.etecture.opensource.dynamicresources.annotations.Consumes;
import de.etecture.opensource.dynamicresources.annotations.Produces;
import de.etecture.opensource.dynamicresources.annotations.Resource;
import de.etecture.opensource.dynamicresources.api.RequestReader;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.metadata.annotated.AnnotatedApplication;
import de.etecture.opensource.dynamicresources.metadata.annotated.AnnotatedResource;
import de.etecture.opensource.dynamicresources.utils.AnnotatedTypeDelegate;
import de.etecture.opensource.dynamicresources.utils.ApplicationLiteral;
import de.etecture.opensource.dynamicresources.utils.BeanBuilder;
import de.etecture.opensource.dynamicresources.utils.ConsumesLiteral;
import de.etecture.opensource.dynamicresources.utils.ProducesLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * this CDI-{@link Extension} is responsible for scanning all classes to find
 * {@link Resource}s.
 *
 * As a result, an {@link Application} is built and registered as a
 * {@link Bean}.
 *
 * @author rhk
 * @version
 * @since
 */
public class ResourceMetadataScanner implements Extension {

    private static final Logger LOG = Logger.getLogger(
            "ResourceMetadataScanner");
    private final Map<Application, Set<Class<?>>> resourceTypes =
            new HashMap<>();
    private Map<Class<?>, Set<String>> consumedMimeTypes = new HashMap<>();
    private Map<Class<?>, Set<String>> producedMimeTypes = new HashMap<>();

    /**
     * scans for any type that is annotated with &#64;{@link Application}
     * <p>
     * will be called by the CDI-container to inform about an annotated type
     * that we have to scan.
     *
     * @param <T>
     * @param pat
     * @throws Exception
     */
    <T> void scanForApplicationAnnotation(@Observes ProcessAnnotatedType<T> pat)
            throws Exception {

        AnnotatedType<T> at = pat.getAnnotatedType();

        if (at.isAnnotationPresent(Application.class)) {
            if (!resourceTypes.containsKey(at.getAnnotation(Application.class))) {
                resourceTypes.put(at.getAnnotation(Application.class),
                        new HashSet<Class<?>>());
            }
        }
    }

    /**
     * scans for any type that is annotated with &#64;{@link Resource}
     * <p>
     * will be called by the CDI-container to inform about an annotated type
     * that we have to scan.
     *
     * @param <T>
     * @param pat
     * @throws Exception
     */
    <T> void scanForResourceAnnotation(@Observes ProcessAnnotatedType<T> pat)
            throws Exception {

        AnnotatedType<T> at = pat.getAnnotatedType();

        // look if the type is annotated with @Resource
        if (at.isAnnotationPresent(Resource.class)) {
            LOG.log(Level.FINER, "found resource interface type: {0}",
                    at.getJavaClass().getName());
            // lookup the annotated package
            Package pack = at.getJavaClass().getPackage();
            if (pack.isAnnotationPresent(Application.class)) {
                Application annotation = pack.getAnnotation(Application.class);
                // add the type as a resource to the application.
                addResourceType(annotation, at.getJavaClass());
                // and add a produced mime-type for it:
                addProducedMimeType(at.getJavaClass(), "text/plain");
            }
        }
    }

    /**
     * scans for any type that is assignable from {@link RequestReader}
     * <p>
     * will be called by the CDI-container to inform about an annotated type
     * that we have to scan.
     *
     * @param <T>
     * @param pat
     * @throws Exception
     */
    <T> void scanForRequestReaders(@Observes ProcessAnnotatedType<T> pat) throws
            Exception {

        final AnnotatedType<T> at = pat.getAnnotatedType();

        // look, if the type is a subtype of RequestReader.
        if (RequestReader.class.isAssignableFrom(at.getJavaClass())) {

            // look if @Consumes is present ...
            if (!at.isAnnotationPresent(Consumes.class)) {
                // lookup the class from the generics.
                Class<?> responsibleTypeForRequestReader =
                        findTypeForRequestReader(
                        (Class<? extends RequestReader>) at
                        .getJavaClass());
                // if it is still not found, assume it is Object.class
                if (responsibleTypeForRequestReader == null) {
                    responsibleTypeForRequestReader = Object.class;
                }
                addConsumedMimeType(responsibleTypeForRequestReader,
                        "text/plain");
                final Consumes consumes = new ConsumesLiteral(
                        responsibleTypeForRequestReader, new String[]{
                    "text/plain"}, 0);
                // delegate the new @Consumes
                pat.setAnnotatedType(new AnnotatedTypeDelegate<T>(at) {
                    @Override
                    public Set<Annotation> getAnnotations() {
                        Set<Annotation> result = new HashSet<>();
                        result.addAll(super.getAnnotations());
                        result.add(consumes);
                        return result;
                    }
                });
            } else {
                // ... found, so add mimetypes.
                Consumes consumes = at.getAnnotation(Consumes.class);
                addConsumedMimeType(consumes.requestType(), consumes.mimeType());
            }
        }
    }

    /**
     * scans for any type that is assignable from {@link ResponseWriter}
     * <p>
     * will be called by the CDI-container to inform about an annotated type
     * that we have to scan.
     *
     * @param <T>
     * @param pat
     * @throws Exception
     */
    <T> void scanForResponseWriters(@Observes ProcessAnnotatedType<T> pat)
            throws Exception {

        final AnnotatedType<T> at = pat.getAnnotatedType();

        // look, if the type is a subtype of RequestReader.
        if (ResponseWriter.class.isAssignableFrom(at.getJavaClass())) {

            // look if @Consumes is present ...
            if (!at.isAnnotationPresent(Consumes.class)) {
                // lookup the class from the generics.
                Class<?> responsibleTypeForResponseWriter =
                        findTypeForResponseWriter(
                        (Class<? extends ResponseWriter>) at
                        .getJavaClass());
                // if it is still not found, assume it is Object.class
                if (responsibleTypeForResponseWriter == null) {
                    responsibleTypeForResponseWriter = Object.class;
                }
                addProducedMimeType(responsibleTypeForResponseWriter,
                        "text/plain");
                final Produces produces = new ProducesLiteral(
                        responsibleTypeForResponseWriter, new String[]{
                    "text/plain"}, 0);
                // delegate the new @Produces
                pat.setAnnotatedType(new AnnotatedTypeDelegate<T>(at) {
                    @Override
                    public Set<Annotation> getAnnotations() {
                        Set<Annotation> result = new HashSet<>();
                        result.addAll(super.getAnnotations());
                        result.add(produces);
                        return result;
                    }
                });
            } else {
                // ... found, so add mimetypes.
                Produces produces = at.getAnnotation(Produces.class);
                addProducedMimeType(produces.contentType(), produces.mimeType());
            }
        }
    }

    /**
     * build all the applications from the scanned metadata.
     * <p>
     * will be called by the CDI-Container to let us register all of our scanned
     * beans.
     *
     * @param abd
     * @param bm
     */
    void buildApplicationsMetadata(@Observes AfterBeanDiscovery abd,
            BeanManager beanManager) throws
            Exception {
        for (Map.Entry<Application, Set<Class<?>>> e : resourceTypes.entrySet()) {
            // build the application metadata
            AnnotatedApplication application = new AnnotatedApplication(e
                    .getKey());

            // for each scanned resource type...
            for (Class<?> resourceType : e.getValue()) {
                // get the mimetypes for the resourceType
                Iterable<String> producedMimes = getProducedMimeTypes(
                        resourceType);
                Iterable<String> consumedMimes = getConsumedMimeTypes(
                        resourceType);
                // create the metadata
                AnnotatedResource resource = AnnotatedResource
                        .createAndAddMethods(
                        application,
                        resourceType,
                        producedMimes,
                        consumedMimes);
                application.addResource(resource);

                // build a bean for this resource.
                abd.addBean(createResourceBean(beanManager, resource));
            }

            // now build a bean for this application.
            abd.addBean(createApplicationBean(beanManager, application));
        }
    }

    Bean<de.etecture.opensource.dynamicresources.metadata.Application> createApplicationBean(
            BeanManager beanManager, AnnotatedApplication application) {
        return BeanBuilder.forInstanceWithName(beanManager,
                de.etecture.opensource.dynamicresources.metadata.Application.class,
                application, application.getName())
                .withDefault()
                .withAny()
                .applicationScoped()
                .build();
    }

    Bean<de.etecture.opensource.dynamicresources.metadata.Resource> createResourceBean(
            BeanManager beanManager, AnnotatedResource resource) {
        return BeanBuilder.forInstanceWithName(beanManager,
                de.etecture.opensource.dynamicresources.metadata.Resource.class,
                resource, resource.getName())
                .withDefault()
                .withAny()
                .withQualifier(new ApplicationLiteral(resource
                .getApplication().getName(), resource.getApplication().getBase()))
                .applicationScoped()
                .build();
    }

    private Set<Class<?>> getResourceTypesFor(Application annotation) {
        Set<Class<?>> types;
        // if there's no such application...
        if (!resourceTypes.containsKey(annotation)) {
            // ... create one.
            types = new HashSet<>();
            resourceTypes.put(annotation, types);
        } else {
            types = resourceTypes.get(annotation);
        }
        return types;
    }

    private void addResourceType(Application annotation, Class<?> resourceType) {
        getResourceTypesFor(annotation).add(resourceType);
    }

    private Class<?> findTypeForRequestReader(
            Class<? extends RequestReader> clazz) {
        for (Type intfce : clazz
                .getGenericInterfaces()) {
            if (intfce == RequestReader.class) {
                ParameterizedType pt = ((ParameterizedType) intfce);
                if (pt.getActualTypeArguments().length > 0) {
                    return (Class<?>) pt.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

    private Class<?> findTypeForResponseWriter(
            Class<? extends ResponseWriter> clazz) {
        for (Type intfce : clazz
                .getGenericInterfaces()) {
            if (intfce == ResponseWriter.class) {
                ParameterizedType pt = ((ParameterizedType) intfce);
                if (pt.getActualTypeArguments().length > 0) {
                    return (Class<?>) pt.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

    private void addConsumedMimeType(
            Class<?> responsibleTypeForRequestReader, String... mimeTypes) {
        Set<String> mimes =
                getConsumedMimeTypes(responsibleTypeForRequestReader);
        if (mimes == null) {
            mimes = new HashSet<>();
            consumedMimeTypes.put(responsibleTypeForRequestReader,
                    mimes);
        }
        // remove the standard mime-type if available
        if (mimes.size() == 1 && mimes.contains("*/*")) {
            mimes.clear();
        }
        mimes.addAll(Arrays.asList(mimeTypes));
    }

    private void addProducedMimeType(
            Class<?> responsibleTypeForRequestReader, String... mimeTypes) {
        Set<String> mimes =
                getProducedMimeTypes(responsibleTypeForRequestReader);
        if (mimes == null) {
            mimes = new HashSet<>();
            producedMimeTypes.put(responsibleTypeForRequestReader,
                    mimes);
        }
        // remove the standard mime-type if available
        if (mimes.size() == 1 && mimes.contains("*/*")) {
            mimes.clear();
        }
        mimes.addAll(Arrays.asList(mimeTypes));
    }

    private Set<String> getConsumedMimeTypes(
            Class<?> responsibleTypeForRequestReader) {
        Set<String> mimes = null;
        for (Map.Entry<Class<?>, Set<String>> e : consumedMimeTypes.entrySet()) {
            if (e.getKey().isAssignableFrom(responsibleTypeForRequestReader)) {
                mimes = e.getValue();
                break;
            }
        }
        return mimes;
    }

    private Set<String> getProducedMimeTypes(
            Class<?> responsibleTypeForRequestReader) {
        Set<String> mimes = null;
        for (Map.Entry<Class<?>, Set<String>> e : producedMimeTypes.entrySet()) {
            if (e.getKey().isAssignableFrom(responsibleTypeForRequestReader)) {
                mimes = e.getValue();
                break;
            }
        }
        return mimes;
    }
}
