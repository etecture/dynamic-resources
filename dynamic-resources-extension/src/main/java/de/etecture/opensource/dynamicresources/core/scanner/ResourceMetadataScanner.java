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
import de.etecture.opensource.dynamicresources.api.RequestReader;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
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
import de.etecture.opensource.dynamicresources.metadata.annotated.AnnotatedApplication;
import de.etecture.opensource.dynamicresources.metadata.annotated.AnnotatedResource;
import de.etecture.opensource.dynamicresources.metadata.annotated.AnnotatedResourceMethod;
import de.etecture.opensource.dynamicresources.utils.AnnotatedTypeDelegate;
import de.etecture.opensource.dynamicresources.utils.ApplicationLiteral;
import de.etecture.opensource.dynamicresources.utils.BeanBuilder;
import de.etecture.opensource.dynamicresources.utils.ConsumesLiteral;
import de.etecture.opensource.dynamicresources.utils.MethodLiteral;
import de.etecture.opensource.dynamicresources.utils.ProducesLiteral;
import de.etecture.opensource.dynamicresources.utils.ResourceLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
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
                // found, add it
                executionMethods.add(new ExecutionMethod<>(am));
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
                // create the metadata
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
                abd.addBean(createResourceMethodExecutorBean(beanManager,
                        resourceMethod, method));
                // remember that we've created an executor for this method!
                resourceMethodsWithExecution.add(resourceMethod);
            }
        }

        // now build a query executor for each annotated resource method, that does not have an executor.
        for (de.etecture.opensource.dynamicresources.metadata.Application application
                : applications) {
            for (de.etecture.opensource.dynamicresources.metadata.Resource resource
                    : application.getResources().values()) {
                for (ResourceMethod method : resource.getMethods().values()) {
                    if (!resourceMethodsWithExecution.contains(method)
                            && method instanceof AnnotatedResourceMethod) {
                        // it's an annotated resource method, so build a queryexecutor for it.
                        abd.addBean(
                                createResourceMethodExecutorBean(beanManager,
                                (AnnotatedResourceMethod) method));
                    }
                }
            }
        }
    }

    Bean<ResourceMethodExecutor> createResourceMethodExecutorBean(
            BeanManager beanManager, ResourceMethod resourceMethod,
            final ExecutionMethod<?> executionMethod) {
        return BeanBuilder.forClass(beanManager, ResourceMethodExecutor.class)
                .withName(createResourceMethodExecutorName(resourceMethod))
                .withQualifier(new ApplicationLiteral(resourceMethod
                .getResource().getApplication()))
                .withQualifier(new ResourceLiteral(resourceMethod.getResource()
                .getPath().toString()))
                .withQualifier(new MethodLiteral(resourceMethod.getName()))
                .withAny()
                .applicationScoped()
                .createdBy(new ExecutionMethodResourceMethodExecutorCreator(
                executionMethod))
                .build();
    }

    Bean<ResourceMethodExecutor> createResourceMethodExecutorBean(
            BeanManager beanManager,
            final AnnotatedResourceMethod resourceMethod) {
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
                .withQualifier(new ResourceLiteral(resourceMethod.getResource()
                .getPath().toString()))
                .withQualifier(new MethodLiteral(resourceMethod.getName()))
                .withAny()
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
                foundApplication = true;
                checkExistingResource(application, method);
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
                foundResource = true;
                checkExistingResourceMethod(resource, method);
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
        // create a new response defintion
        BasicResourceMethodRequest request = new BasicResourceMethodRequest(
                resourceMethod, executes.requestType());
        // add the mimetypes for each responsible request reader to this request
        for (String consumedMime : getConsumedMimeTypes(executes.requestType())) {
            request.addAcceptedRequestMediaType(new MediaTypeExpression(
                    consumedMime));
        }
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
}
