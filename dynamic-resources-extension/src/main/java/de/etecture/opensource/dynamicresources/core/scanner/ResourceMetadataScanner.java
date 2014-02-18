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

import de.etecture.opensource.dynamicrepositories.metadata.AnnotatedQueryDefinition;
import de.etecture.opensource.dynamicrepositories.metadata.DefaultQueryDefinition;
import de.etecture.opensource.dynamicrepositories.metadata.QueryDefinition;
import de.etecture.opensource.dynamicresources.annotations.Application;
import de.etecture.opensource.dynamicresources.annotations.Consumes;
import de.etecture.opensource.dynamicresources.annotations.Executes;
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Produces;
import de.etecture.opensource.dynamicresources.annotations.Resource;
import de.etecture.opensource.dynamicresources.annotations.URI;
import de.etecture.opensource.dynamicresources.api.RequestReader;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.accesspoints.AccessPoint;
import de.etecture.opensource.dynamicresources.api.accesspoints.ApplicationAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.TypedResourceAccessor;
import de.etecture.opensource.dynamicresources.core.accessors.DynamicApplicationAccessor;
import de.etecture.opensource.dynamicresources.core.accessors.DynamicApplicationAccessorCreator;
import de.etecture.opensource.dynamicresources.core.accessors.DynamicMethodAccessor;
import de.etecture.opensource.dynamicresources.core.accessors.DynamicMethodAccessorCreator;
import de.etecture.opensource.dynamicresources.core.accessors.DynamicResourceAccessor;
import de.etecture.opensource.dynamicresources.core.accessors.DynamicResourceAccessorCreator;
import de.etecture.opensource.dynamicresources.core.accessors.DynamicTypedResourceAccessor;
import de.etecture.opensource.dynamicresources.core.accessors.DynamicTypedResourceAccessorCreator;
import de.etecture.opensource.dynamicresources.core.executors.ExecutionMethod;
import de.etecture.opensource.dynamicresources.core.executors.ExecutionMethodResourceMethodExecutorCreator;
import de.etecture.opensource.dynamicresources.core.executors.QueryResourceMethodExecutorCreator;
import de.etecture.opensource.dynamicresources.core.executors.ResourceMethodExecutor;
import de.etecture.opensource.dynamicresources.core.mapping.mime.MediaTypeExpression;
import de.etecture.opensource.dynamicresources.metadata.AbstractResource;
import de.etecture.opensource.dynamicresources.metadata.BasicResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.BasicResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.BasicResourceMethodResponse;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.annotated.AnnotatedApplication;
import de.etecture.opensource.dynamicresources.metadata.annotated.AnnotatedResource;
import de.etecture.opensource.dynamicresources.metadata.annotated.AnnotatedResourceMethod;
import de.etecture.opensource.dynamicresources.utils.AnnotatedTypeDelegate;
import de.etecture.opensource.dynamicresources.utils.ApplicationLiteral;
import de.etecture.opensource.dynamicresources.utils.BeanBuilder;
import de.etecture.opensource.dynamicresources.utils.BeanCreator;
import de.etecture.opensource.dynamicresources.utils.ConsumesLiteral;
import de.etecture.opensource.dynamicresources.utils.MethodLiteral;
import de.etecture.opensource.dynamicresources.utils.ProducesLiteral;
import de.etecture.opensource.dynamicresources.utils.ResourceLiteral;
import de.etecture.opensource.dynamicresources.utils.TypedLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.DefinitionException;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;

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

    private static final Pattern DISTINCT_METHOD_NAME = Pattern.compile(
            "[a-zA-Z]+");
    private static final Logger LOG = Logger.getLogger(
            "ResourceMetadataScanner");
    private final Map<Application, Set<Class<?>>> resourceTypes =
            new HashMap<>();
    private Map<Class<?>, Set<String>> consumedMimeTypes = new HashMap<>();
    private Map<Class<?>, Set<String>> producedMimeTypes = new HashMap<>();
    private Set<ExecutionMethod<?>> executionMethods = new HashSet<>();
    private Set<ResourceMethod> resourceMethodsWithExecution = new HashSet<>();

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
                LOG.log(Level.INFO, "found application: {0}", at.getAnnotation(
                        Application.class).name());
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
            LOG.log(Level.INFO, "found resource interface type: {0}",
                    at.getJavaClass().getName());
            // lookup the annotated package
            Package pack = at.getJavaClass().getPackage();
            if (pack.isAnnotationPresent(Application.class)) {
                Application annotation = pack.getAnnotation(Application.class);
                LOG.log(Level.INFO, "found application: {0} for resource: {1}",
                        new Object[]{annotation.name(),
                    at.getJavaClass().getName()});
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
                    "text/plain"});
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
                LOG.log(Level.INFO,
                        "found request reader for type: {0} with mimetypes: {1} in class: {2}",
                        new Object[]{consumes.requestType(),
                    Arrays.toString(consumes.mimeType()),
                    at.getJavaClass().getName()});
            } else {
                // ... found, so add mimetypes.
                Consumes consumes = at.getAnnotation(Consumes.class);
                addConsumedMimeType(consumes.requestType(), consumes.mimeType());
                LOG.log(Level.INFO,
                        "found request reader for type: {0} with mimetypes: {1} in class: {2}",
                        new Object[]{consumes.requestType(),
                    Arrays.toString(consumes.mimeType()),
                    at.getJavaClass().getName()});
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
            if (!at.isAnnotationPresent(Produces.class)) {
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
                    "text/plain"});
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
                LOG.log(Level.INFO,
                        "found response writer for type: {0} with mimetypes: {1} in class: {2}",
                        new Object[]{produces.contentType(),
                    Arrays.toString(produces.mimeType()),
                    at.getJavaClass().getName()});
            } else {
                // ... found, so add mimetypes.
                Produces produces = at.getAnnotation(Produces.class);
                addProducedMimeType(produces.contentType(), produces.mimeType());
                LOG.log(Level.INFO,
                        "found response writer for type: {0} with mimetypes: {1} in class: {2}",
                        new Object[]{produces.contentType(),
                    Arrays.toString(produces.mimeType()),
                    at.getJavaClass().getName()});
            }
        }
    }

    /**
     * scans for any type that has methods annotated with &#64;{@link Executes}
     * <p>
     * will be called by the CDI-container to inform about an annotated type
     * that we have to scan.
     *
     * @param <T>
     * @param pat
     * @throws Exception
     * @see Executes
     */
    <T> void scanForExecutionMethods(@Observes ProcessAnnotatedType<T> pat)
            throws Exception {
        AnnotatedType<T> at = pat.getAnnotatedType();
        for (AnnotatedMethod<? super T> am : at.getMethods()) {
            if (am.isAnnotationPresent(Executes.class)) {
                final ExecutionMethod<? super T> executionMethod =
                        new ExecutionMethod<>(am);
                // found, add it
                executionMethods.add(executionMethod);
                LOG.log(Level.INFO,
                        "found execution method for: application={0}, resource={1}, method={2}",
                        new Object[]{executionMethod.application(),
                    executionMethod.resource(),
                    executionMethod.method()});
            }
        }
    }

    /**
     * buildVerbose all the applications from the scanned metadata.
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
        Set<de.etecture.opensource.dynamicresources.metadata.Application> applications =
                new LinkedHashSet<>();
        for (Map.Entry<Application, Set<Class<?>>> e : resourceTypes.entrySet()) {
            // buildVerbose the application metadata
            LOG.log(Level.INFO, "build application: {0} with base: {1}",
                    new Object[]{e.getKey().name(),
                e.getKey().base()});
            AnnotatedApplication application = new AnnotatedApplication(null, e
                    .getKey());
            applications.add(application);

            // for each scanned resource type...
            for (Class<?> resourceType : e.getValue()) {
                // get the mimetypes for the resourceType
                Iterable<String> producedMimes = getProducedMimeTypes(
                        resourceType);
                Iterable<String> consumedMimes = getConsumedMimeTypes(
                        resourceType);
                final Resource annotation = resourceType.getAnnotation(
                        Resource.class);
                // create the metadata
                LOG.log(Level.INFO,
                        "build resource: {0} with path: {1} for class: {2}",
                        new Object[]{StringUtils.defaultIfBlank(annotation
                    .name(), resourceType.getSimpleName()),
                    resourceType.getAnnotation(Resource.class).path(),
                    resourceType.getName()});
                AnnotatedResource resource = AnnotatedResource
                        .createAndAddMethods(
                        application,
                        resourceType,
                        producedMimes,
                        consumedMimes);
                application.addResource(resource);

                // buildVerbose a bean for this resource.
                abd.addBean(createResourceBean(beanManager, resource));

            }

            // now buildVerbose a bean for this application.
            abd.addBean(createApplicationBean(beanManager, application));

        }

        // process the executionMethods
        for (ExecutionMethod<?> method : executionMethods) {
            // check, if it is bound.
            checkExistingApplication(applications, method);

            // iterate about all resource methods that matches the execution method
            for (ResourceMethod resourceMethod : getMatchingResourceMethods(
                    applications, method)) {
                // create a bean that will be used to execute this execution method for this resource method.
                LOG.log(Level.INFO,
                        "build execution method: {2} for resource: {0} in application: {1}",
                        new Object[]{resourceMethod.getResource().getName(),
                    resourceMethod.getResource().getApplication().getName(),
                    resourceMethod.getName()});
                abd.addBean(createResourceMethodExecutorBean(beanManager,
                        resourceMethod, method));
                // remember that we've created an executor for this method!
                resourceMethodsWithExecution.add(resourceMethod);
            }
        }

        // now build a query executor for each annotated resource method, that does not have an executor.
        for (de.etecture.opensource.dynamicresources.metadata.Application application
                : applications) {
            // create ApplicationAccessorBean for this application
            abd.addBean(createAccessPointBean(beanManager, application));
            for (de.etecture.opensource.dynamicresources.metadata.Resource resource
                    : application.getResources().values()) {
                // create ResourceAccessorBean for this resource
                abd.addBean(createAccessPointBean(beanManager, resource));
                Set<Class<?>> supportedResponseTypes = new HashSet<>();
                for (ResourceMethod method : resource.getMethods().values()) {
                    if (!resourceMethodsWithExecution.contains(method)
                            && method instanceof AnnotatedResourceMethod) {
                        LOG.log(Level.INFO,
                                "build query execution method: {2} for resource: {0} in application: {1}",
                                new Object[]{method.getResource().getName(),
                            method.getResource().getApplication().getName(),
                            method.getName()});
                        // it's an annotated resource method, so build a queryexecutor for it.
                        abd.addBean(
                                createResourceMethodExecutorBean(beanManager,
                                (AnnotatedResourceMethod) method));
                    }
                    for (ResourceMethodResponse<?> response : method
                            .getResponses().values()) {
                        supportedResponseTypes.add(response.getResponseType());
                        abd.addBean(createAccessPointBean(beanManager,
                                response));
                    }
                }
                for (Class<?> responseType : supportedResponseTypes) {
                    // create a TypedResourceAccessorBean for this resource and this response type
                    abd.addBean(createAccessPointBean(beanManager,
                            resource, responseType));
                }
            }
        }
    }

    Bean<ApplicationAccessor> createAccessPointBean(BeanManager beanManager,
            de.etecture.opensource.dynamicresources.metadata.Application application) {
        LOG.log(Level.INFO,
                "create accesspoint: @Application(name = \"{0}\") ApplicationAccessor",
                application.getName());
        return createAccessPointBean(
                beanManager,
                ApplicationAccessor.class,
                DynamicApplicationAccessor.class,
                Collections.<Annotation>singleton(new ApplicationLiteral(
                application)),
                "" + application.hashCode(),
                new DynamicApplicationAccessorCreator(application));
    }

    Bean<ResourceAccessor> createAccessPointBean(BeanManager beanManager,
            de.etecture.opensource.dynamicresources.metadata.Resource resource) {
        LOG.log(Level.INFO,
                "create accesspoint: @Resource(name = \"{0}\") ResourceAccessor",
                resource.getName());
        return createAccessPointBean(
                beanManager,
                ResourceAccessor.class,
                DynamicResourceAccessor.class,
                Collections.<Annotation>singleton(new ResourceLiteral(resource)),
                "" + resource.hashCode(),
                new DynamicResourceAccessorCreator(resource));
    }

    <T> Bean<TypedResourceAccessor<T>> createAccessPointBean(
            BeanManager beanManager,
            de.etecture.opensource.dynamicresources.metadata.Resource resource,
            Class<T> type) {
        LOG.log(Level.INFO,
                "create accesspoint: @Resource(name = \"{0}\") TypedResourceAccessor<{1}>",
                new Object[]{resource.getName(),
            type.getSimpleName()});
        Set<Annotation> qualifier = new HashSet<>();
        qualifier.add(new ResourceLiteral(resource));
        qualifier.add(new TypedLiteral(type));
        return createAccessPointBean(
                beanManager,
                TypedResourceAccessor.class,
                DynamicTypedResourceAccessor.class,
                qualifier,
                "" + resource.hashCode() + "" + type.getSimpleName(),
                new DynamicTypedResourceAccessorCreator(resource, type),
                buildType(TypedResourceAccessor.class, type));
    }

    private Type buildType(final Class<?> rawType, final Class<?>... args) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return args;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    <T> Bean<MethodAccessor<T>> createAccessPointBean(
            BeanManager beanManager,
            ResourceMethodResponse<T> response) {
        LOG.log(Level.INFO,
                "create accesspoint: @Resource(name = \"{0}\") @Method(name = \"{2}\") MethodAccessor<{1}>",
                new Object[]{response.getMethod().getResource().getName(),
            response.getResponseType().getSimpleName(),
            response.getMethod().getName()});
        Set<Annotation> qualifier = new HashSet<>();
        qualifier.add(new ResourceLiteral(response.getMethod().getResource()));
        qualifier.add(new MethodLiteral(response.getMethod().getName()));
        qualifier.add(new TypedLiteral(response.getResponseType()));
        return createAccessPointBean(
                beanManager,
                MethodAccessor.class,
                DynamicMethodAccessor.class,
                qualifier,
                "" + response.hashCode(),
                new DynamicMethodAccessorCreator(response),
                buildType(MethodAccessor.class, response.getResponseType()));
    }

    private <M, A extends AccessPoint<M>> Bean<A> createAccessPointBean(
            BeanManager beanManager,
            Class<A> type,
            Class<? extends A> concreteType,
            Set<Annotation> qualifier,
            String prefix,
            BeanCreator creator, Type... types) {
        BeanBuilder<A> builder =
                BeanBuilder.forClass(beanManager, type)
                .withName(concreteType.getSimpleName() + "For" + prefix);
        if (types.length > 0) {
            builder = builder.withTypes(types);
        }
        return builder
                .withQualifier(qualifier)
                .withDefault()
                .withAny()
                .applicationScoped()
                .createdBy(creator)
                .build();
    }

    Bean<ResourceMethodExecutor> createResourceMethodExecutorBean(
            BeanManager beanManager, ResourceMethod resourceMethod,
            final ExecutionMethod<?> executionMethod) {
        LOG.info(String.format(
                "create Bean: @Application(name = \"%s\") @Resource(name = \"%s\") @Method(name = \"%s\") ExecutionMethodResourceMethodExecutor",
                resourceMethod.getResource().getApplication().getName(),
                resourceMethod.getResource().getName(), resourceMethod.getName()));
        return BeanBuilder.forClass(beanManager, ResourceMethodExecutor.class)
                .withName(createResourceMethodExecutorName(resourceMethod))
                .withQualifier(new ApplicationLiteral(resourceMethod
                .getResource().getApplication()))
                .withQualifier(new ResourceLiteral(resourceMethod.getResource()))
                .withQualifier(new MethodLiteral(resourceMethod.getName()))
                .withAny()
                .withDefault()
                .applicationScoped()
                .createdBy(new ExecutionMethodResourceMethodExecutorCreator(
                executionMethod))
                .build();
    }

    Bean<ResourceMethodExecutor> createResourceMethodExecutorBean(
            BeanManager beanManager,
            final AnnotatedResourceMethod resourceMethod) {
        LOG.info(String.format(
                "create Bean: @Application(name = \"%s\") @Resource(name = \"%s\") @Method(name = \"%s\") QueryResourceMethodExecutor",
                resourceMethod.getResource().getApplication().getName(),
                resourceMethod.getResource().getName(), resourceMethod.getName()));
        Method annotation = resourceMethod.getAnnotation();
        QueryDefinition queryDefinition;
        if (annotation.query().length > 0) {
            queryDefinition =
                    new AnnotatedQueryDefinition(
                    annotation.query()[0]) {
                @Override
                public String getStatement() {
                    return createStatement((Class) resourceMethod
                            .getAnnotatedElement(), resourceMethod.getName(),
                            super.getStatement());
                }
            };
        } else {
            queryDefinition = new DefaultQueryDefinition(createStatement(
                    (Class) resourceMethod.getAnnotatedElement(), resourceMethod
                    .getName(), ""));
        }

        return BeanBuilder.forClass(beanManager, ResourceMethodExecutor.class)
                .withName(createResourceMethodExecutorName(resourceMethod))
                .withQualifier(new ApplicationLiteral(resourceMethod
                .getResource().getApplication()))
                .withQualifier(new ResourceLiteral(resourceMethod.getResource()))
                .withQualifier(new MethodLiteral(resourceMethod.getName()))
                .withAny()
                .withDefault()
                .applicationScoped()
                .createdBy(new QueryResourceMethodExecutorCreator(
                queryDefinition))
                .build();
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
                .withQualifier(new ApplicationLiteral(resource.getApplication()
                .getName()))
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
            if (intfce instanceof ParameterizedType) {
                ParameterizedType pt = ((ParameterizedType) intfce);
                if (pt.getRawType() == RequestReader.class
                        && pt.getActualTypeArguments().length > 0) {
                    return (Class<?>) pt.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

    private Class<?> findTypeForResponseWriter(
            Class<? extends ResponseWriter> clazz) {
        for (Type intfce : clazz.getGenericInterfaces()) {
            if (intfce instanceof ParameterizedType) {
                ParameterizedType pt = ((ParameterizedType) intfce);
                if (pt.getRawType() == ResponseWriter.class
                        && pt.getActualTypeArguments().length > 0) {
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

    /**
     * collects all the mime-types for which a response writer exists, that is
     * responsible to write the given type.
     *
     * @param responsibleTypeForRequestReader
     * @return
     */
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

    /**
     * collects all the mime-types for which a request reader exists, that is
     * responsible to read the given type.
     *
     * @param responsibleTypeForRequestReader
     * @return
     */
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

    /**
     * checks, if the execution method is specified for an actually existing
     * application.
     * <p>
     * An applicatio exists, if it's name matches the pattern specified in
     * {@link Executes#application()} of the execution method.
     * <p>
     * If a matching application is not found, then a
     * {@link DefinitionException} will be thrown.
     *
     *
     * @param applications
     * @param method
     * @throws DefinitionException
     */
    private void checkExistingApplication(
            Set<de.etecture.opensource.dynamicresources.metadata.Application> applications,
            ExecutionMethod<?> method) throws DefinitionException {
        boolean foundApplication = false;
        for (de.etecture.opensource.dynamicresources.metadata.Application application
                : applications) {
            // is the method responsible for this application?
            if (method.matches(application)) {
                try {
                    checkExistingResource(application, method);
                    foundApplication = true;
                } catch (DefinitionException definitionException) {
                    // slurp.
                }
            }
        }
        if (!foundApplication) {
            throw new DefinitionException(
                    "cannot find an application that match the criteria given in the @Executes annotation of the method: "
                    + method);

        }
    }

    /**
     * checks, if the execution method is specified for an actually existing
     * resource.
     * <p>
     * A resource exists, if it is a part of the given application and if it
     * matches the pattern specified in {@link Executes#resource()} of the
     * execution method.
     * <p>
     * If a matching resource is not found in the given application, then a
     * {@link DefinitionException} will be thrown.
     *
     * @param application
     * @param method
     * @throws DefinitionException
     */
    private void checkExistingResource(
            de.etecture.opensource.dynamicresources.metadata.Application application,
            ExecutionMethod<?> method) throws DefinitionException {
        // check the resources
        boolean foundResource = false;
        for (de.etecture.opensource.dynamicresources.metadata.Resource resource
                : application.getResources().values()) {
            // is the method responsible for this resource?
            if (method.matches(resource)) {
                try {
                    checkExistingResourceMethod(resource, method);
                    foundResource = true;
                } catch (DefinitionException definitionException) {
                    // slurp...
                }
            }
        }
        if (!foundResource) {
            throw new DefinitionException(
                    "cannot find a resource that match the criteria given in the @Executes annotation of the method: "
                    + method);
        }
    }

    /**
     * checks, if the execution method is specified for an actually existing
     * resource method.
     * <p>
     * A method exists, if it is a part of the given resource and if it matches
     * the pattern specified in {@link Executes#method()} of the execution
     * method.
     * <p>
     * If a matching resource method is not found in the given resource, then
     * this method does the following:
     * <p>
     * If the {@link Executes#method()} definition is <b>NOT</b> a regular
     * expression and is a <b>correct</b> name for a resource method, then a
     * resource method definition is created with this name in the given
     * resource.
     * <p>
     * A correct resource method name is a string, containing only letters A-Z
     * or a-z, no whitespaces, no numbers and no other characters.
     *
     * @param resource
     * @param method
     * @throws DefinitionException
     */
    private void checkExistingResourceMethod(
            de.etecture.opensource.dynamicresources.metadata.Resource resource,
            ExecutionMethod<?> method) throws DefinitionException {
        // check the methods
        boolean foundMethod = false;
        for (ResourceMethod resourceMethod : resource
                .getMethods().values()) {
            // is the method responsible for this resource method?
            if (method.matches(resourceMethod)) {
                foundMethod = true;
            }
        }
        if (!foundMethod) {
            // if the method pattern is distinct
            if (resource instanceof AbstractResource
                    && DISTINCT_METHOD_NAME
                    .matcher(method.method()).matches()) {
                LOG.log(Level.INFO,
                        "create a new resource method: {0} for resource: {1} due to existing execution method in: {2}",
                        new Object[]{method.method(),
                    resource.getName(),
                    method});
                // create a new resource method for this execution method.
                createResourceMethodForExecution(resource,
                        method);
            } else {
                throw new DefinitionException(
                        "cannot find a resource method that match the criteria given in the @Executes annotation of the method: "
                        + method);

            }
        }
    }

    /**
     * creates a new method for an execution that is defined with
     * &#64;{@link Executes} and does not be specified in the existing resources
     * metadata.
     *
     * @param resource
     * @param executes
     */
    private void createResourceMethodForExecution(
            de.etecture.opensource.dynamicresources.metadata.Resource resource,
            Executes executes) {
        // create a new executes definition.
        BasicResourceMethod resourceMethod = new BasicResourceMethod(resource,
                executes.method(), "");
        // create a new response defintion
        BasicResourceMethodResponse response = new BasicResourceMethodResponse(
                resourceMethod, executes.responseType(), StatusCodes.OK);
        // add the mimetypes for each responsible response reader to this response
        for (String producedMime : getProducedMimeTypes(executes.responseType())) {
            response.addSupportedResponseMediaType(new MediaTypeExpression(
                    producedMime));
        }
        resourceMethod.addResponse(response);

        // create a new response defintion
        BasicResourceMethodRequest request = new BasicResourceMethodRequest(
                resourceMethod, executes.requestType());
        // add the mimetypes for each responsible request reader to this request
        for (String consumedMime : getConsumedMimeTypes(executes.requestType())) {
            request.addAcceptedRequestMediaType(new MediaTypeExpression(
                    consumedMime));
        }
        resourceMethod.addRequest(request);

        // add the executes.
        ((AbstractResource) resource).addMethod(resourceMethod);
    }

    private Iterable<ResourceMethod> getMatchingResourceMethods(
            Iterable<de.etecture.opensource.dynamicresources.metadata.Application> applications,
            ExecutionMethod<?> execution) {
        Set<ResourceMethod> methods = new HashSet<>();
        for (de.etecture.opensource.dynamicresources.metadata.Application application
                : applications) {
            for (de.etecture.opensource.dynamicresources.metadata.Resource resource
                    : application.getResources().values()) {
                for (ResourceMethod method : resource.getMethods().values()) {
                    if (execution.matches(method)) {
                        methods.add(method);
                    }
                }
            }
        }
        return methods;
    }

    private Iterable<AnnotatedResourceMethod> getAnnotatedResourceMethods(
            Iterable<de.etecture.opensource.dynamicresources.metadata.Application> applications) {
        Set<AnnotatedResourceMethod> methods = new HashSet<>();
        return methods;
    }

    private String createResourceMethodExecutorName(
            ResourceMethod resourceMethod) {
        return String.format("ExecutorFor%sInResource%sAndApplication%s",
                resourceMethod.getName(), resourceMethod.getResource().getName(),
                resourceMethod.getResource().getApplication().getName());
    }

    private String createStatement(Class<?> resourceClass,
            String methodName, String statement) {
        if (statement == null || statement.trim().isEmpty()) {
            statement = methodName;
        }
        try {
            return ResourceBundle.getBundle(resourceClass.getName()).getString(
                    statement);
        } catch (MissingResourceException e) {
            return statement;
        }
    }

    private de.etecture.opensource.dynamicresources.metadata.Application findApplicationByName(
            String name,
            Iterable<de.etecture.opensource.dynamicresources.metadata.Application> applications) {
        for (de.etecture.opensource.dynamicresources.metadata.Application application
                : applications) {
            if (name.equals(application.getName())) {
                return application;
            }
        }
        throw new DefinitionException(
                "There is no application defined with name: " + name);
    }

    private de.etecture.opensource.dynamicresources.metadata.Application findApplicationByURI(
            String uri,
            Iterable<de.etecture.opensource.dynamicresources.metadata.Application> applications) {
        for (de.etecture.opensource.dynamicresources.metadata.Application application
                : applications) {
            if (uri.equals(application.getBase())) {
                return application;
            }
        }
        throw new DefinitionException(
                "There is no application defined with uri: " + uri);
    }

    private de.etecture.opensource.dynamicresources.metadata.Application findApplicationStartingWithURI(
            String uri,
            Iterable<de.etecture.opensource.dynamicresources.metadata.Application> applications) {
        for (de.etecture.opensource.dynamicresources.metadata.Application application
                : applications) {
            if (uri.startsWith(application.getBase())) {
                return application;
            }
        }
        throw new DefinitionException(
                "There is no application defined that is root of uri: " + uri);
    }

    private de.etecture.opensource.dynamicresources.metadata.Resource findResourceInApplicationForInjectionPoint(
            InjectionPoint ip,
            de.etecture.opensource.dynamicresources.metadata.Application application)
            throws DefinitionException, ResourceNotFoundException {
        if (ip.getAnnotated().isAnnotationPresent(Resource.class)) {
            return application.getResources().get(ip.getAnnotated()
                    .getAnnotation(Resource.class).name());
        } else if (ip.getAnnotated().isAnnotationPresent(Named.class)) {
            return application.getResources().get(ip.getAnnotated()
                    .getAnnotation(Named.class).value());
        } else if (ip.getAnnotated().isAnnotationPresent(URI.class)) {
            return application.findResource(ip.getAnnotated()
                    .getAnnotation(URI.class).value());
        } else if (application.getResources().isEmpty()) {
            throw new DefinitionException(
                    "Ambigiuous resources found for " + ip);
        } else {
            return application.getResources().values().iterator()
                    .next();
        }
    }

    private de.etecture.opensource.dynamicresources.metadata.Resource findResourceWithinApplicationsForInjectionPoint(
            InjectionPoint ip,
            Set<de.etecture.opensource.dynamicresources.metadata.Application> applications)
            throws ResourceNotFoundException, DefinitionException {
        // find the application
        de.etecture.opensource.dynamicresources.metadata.Application application;
        if (ip.getAnnotated().isAnnotationPresent(Application.class)) {
            // get the @Application
            application = findApplicationByName(ip.getAnnotated()
                    .getAnnotation(Application.class).name(), applications);
            return findResourceInApplicationForInjectionPoint(ip,
                    application);
        } else if (ip.getAnnotated().isAnnotationPresent(URI.class)) {
            application = findApplicationStartingWithURI(ip.getAnnotated()
                    .getAnnotation(URI.class).value(), applications);
            return application.findResource(ip.getAnnotated()
                    .getAnnotation(URI.class).value());
        } else if (applications.size() == 1) {
            application = applications.iterator().next();
            return findResourceInApplicationForInjectionPoint(ip,
                    application);
        } else {
            throw new DefinitionException(
                    "Ambigiuous applications found for " + ip);
        }
    }
}
