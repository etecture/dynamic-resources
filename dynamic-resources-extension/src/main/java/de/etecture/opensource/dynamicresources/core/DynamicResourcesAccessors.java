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
package de.etecture.opensource.dynamicresources.core;

import de.etecture.opensource.dynamicrepositories.utils.DefaultLiteral;
import de.etecture.opensource.dynamicrepositories.utils.NamedLiteral;
import de.etecture.opensource.dynamicresources.annotations.accessing.ResourceWithType;
import de.etecture.opensource.dynamicresources.annotations.accessing.ResourceWithURI;
import de.etecture.opensource.dynamicresources.annotations.accessing.ResourceWithURITemplate;
import de.etecture.opensource.dynamicresources.annotations.accessing.Verb;
import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.ResourceMethodExecutor;
import de.etecture.opensource.dynamicresources.api.ResourceMethodInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ResponseException;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceMethodAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.Resources;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.utils.GlobalLiteral;
import de.etecture.opensource.dynamicresources.utils.InjectionPointHelper;
import de.etecture.opensource.dynamicresources.utils.ResourceWithURILiteral;
import de.etecture.opensource.dynamicresources.utils.ResourceWithURITemplateLiteral;
import de.etecture.opensource.dynamicresources.utils.VerbLiteral;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * this is the standard implementation for {@link Resources}.
 *
 * It provides the implementation for the
 * <code>select()</code>-methods in the {@link Resources} interface as well as
 * producer methods to be able to inject qualified {@link ResourceAccessor}s and
 * {@link ResourceMethodAccessor}s.
 *
 * @author rhk
 * @version
 * @since
 * @see Resources
 */
@Default
public class DynamicResourcesAccessors implements Resources {

    /**
     * the application we process here.
     */
    @Inject
    Application application;
    /**
     * an accesspoint to all available resource-metadatas.
     */
    @Inject
    @Any
    Instance<Resource> resources;
    /**
     * an accesspoint to all available resource-method-executors.
     */
    @Inject
    @Any
    Instance<ResourceMethodExecutor> executors;
    /**
     * an accesspoint to all available resource-method-interceptors.
     */
    @Inject
    @Any
    Instance<ResourceMethodInterceptor> interceptors;

    /**
     * produces a ResourceAccessor for an injectionPoint annotated with a
     * {@link ResourceWithType} annotation.
     *
     * searches the resourceaccessor for the type specified with the annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;ResourceWithType(CustomerResource.class)
     *   ResourceAccessor&lt;CustomerResource&gt; customerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param ip
     * @return
     */
    @Produces
    @ResourceWithType(Object.class)
    public ResourceAccessor produceResourceAccessorForResourceWithType(
            InjectionPoint ip) {
        try {
            // lookup the resourceType for the @ResourceWithType annotation of this injection point
            Class<?> resourceType = InjectionPointHelper.findQualifier(
                    ResourceWithType.class, ip).value();
            // select the ResourceAccessor with this resourceType
            return select(resourceType);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            throw new InjectionException(resourceNotFoundException);
        }
    }

    /**
     * produces a ResourceAccessor for an injectionPoint annotated with a
     * {@link ResourceWithURI} annotation.
     *
     * searches the resourceaccessor for the uri specified with the annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;ResourceWithURI("/resources/customers/1234567890/")
     *   ResourceAccessor&lt;CustomerResource&gt; customerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param ip
     * @return
     */
    @Produces
    @ResourceWithURI("")
    public ResourceAccessor produceResourceAccessorForResourceWithURI(
            InjectionPoint ip) {
        try {
            // lookup the uri for the @ResourceWithURI annotation of this injection point
            String uri = InjectionPointHelper.findQualifier(
                    ResourceWithURI.class, ip).value();
            // select the ResourceAccessor with this uri
            return select(uri);
        } catch (ResourceNotFoundException resourceNotFoundException) {
            throw new InjectionException(resourceNotFoundException);
        }
    }

    /**
     * produces a ResourceAccessor for an injectionPoint annotated with a
     * {@link ResourceWithURITemplate} annotation.
     *
     * searches the resourceaccessor for the uri-template specified with the
     * annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;ResourceWithURITemplate("/resources/customers/{custNo}/")
     *   ResourceAccessor&lt;CustomerResource&gt; customerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param ip
     * @return
     */
    @Produces
    @ResourceWithURITemplate("")
    public ResourceAccessor produceResourceAccessorForResourceWithURITemplate(
            InjectionPoint ip) {
        // lookup the uri-template for the @ResourceWithURITemplate annotation of this injection point
        String uriTemplate = InjectionPointHelper.findQualifier(
                ResourceWithURITemplate.class, ip).value();
        // get the resource metadata from the application that is defined with this uri-template
        Resource resource = application.getResources().get(uriTemplate);
        if (resource == null) {
            // there is no resource defined for the uri-template.
            throw new InjectionException(
                    "cannot find a resource for uri-template: " + uriTemplate);
        }
        // else: build a ResourceAccessor for this resource metadata.
        return new ResourceAccessorImpl(resource);
    }

    /**
     * produces a ResourceAccessor for an injectionPoint annotated with a
     * {@link Named} annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;Named("CustomerResource")
     *   ResourceAccessor customerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param ip
     * @return
     */
    @Produces
    @Named
    public ResourceAccessor produceResourceAccessorForNamedResource(
            InjectionPoint ip) {
        // lookup the name for the @Named annotation of this injection point
        Resource resource = resources.select(InjectionPointHelper.findQualifier(
                Named.class, ip)).get();
        // else: build a ResourceAccessor for this resource metadata.
        return new ResourceAccessorImpl(resource);
    }

    /**
     * produces a ResourceMethodAccessor for an injectionPoint annotated with a
     * {@link Verb} and a {@link ResourceWithURI} annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;ResourceWithURI("/resources/customers/1234567890/")
     *   &#64;Verb(HttpMethods.GET)
     *   ResourceMethodAccessor getCustomerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param ip
     * @return
     */
    @Produces
    @ResourceWithURI("")
    @Verb("")
    public ResourceMethodAccessor produceResourceMethodAccessorForResourceWithURI(
            InjectionPoint ip) {
        try {
            // lookup the uri for the @ResourceWithURI annotation of this injection point
            String uri = InjectionPointHelper.findQualifier(
                    ResourceWithURI.class, ip).value();
            // lookup the methodName for the @Verb annotation of this injection point
            String methodName = InjectionPointHelper.findQualifier(Verb.class,
                    ip).value();
            // select the ResourceMethodAccessor for the defined uri and defined methodName
            return select(uri, methodName);
        } catch (ResourceMethodNotFoundException | ResourceNotFoundException ex) {
            throw new InjectionException(ex);
        }
    }

    @Override
    public Application getMetadata() {
        return application;
    }

    @Override
    public <X> ResourceAccessor<X> select(
            Class<X> resourceClass) throws ResourceNotFoundException {
        // find the resource metadata for the given class in the application.
        Resource resource = application.getResource(resourceClass);
        // build a ResourceAccessor for the metadata.
        return new ResourceAccessorImpl(resource);
    }

    @Override
    public ResourceAccessor<?> select(String uri) throws
            ResourceNotFoundException {
        // find the resource metadata that matches the given uri in the application.
        Resource resource = application.findResource(uri);
        // build a ResourceAccessor for the metadata.
        return new ResourceAccessorImpl(resource);
    }

    @Override
    public <X> ResourceMethodAccessor<X> select(
            Class<X> resourceClass, String method, String... pathParamValues)
            throws ResourceMethodNotFoundException, ResourceNotFoundException {
        // select the ResourceAccessor by the given class.
        ResourceAccessor resourceAccessor = select(resourceClass);
        // iterate about the parameter names that comes from the metadata of the resource that is represented by the resourceAccessor
        for (ListIterator<String> it = resourceAccessor.getMetadata()
                .getPathParameterNames().listIterator(); it.hasNext();) {
            int index = it.nextIndex();
            if (index >= 0 && index < pathParamValues.length) {
                String name = it.next();
                // the value is the value with the next index in the given pathParamValues - array.
                String value = pathParamValues[index];
                // add the pathParameter.
                resourceAccessor = resourceAccessor.pathParam(name, value);
            }
        }
        // select the ResourceMethodAccessor from the filled resourceAccessor.
        return resourceAccessor.method(method);
    }

    @Override
    public <X> ResourceMethodAccessor<X> select(
            Class<X> resourceClass, String method,
            Map<String, String> pathParams) throws
            ResourceMethodNotFoundException, ResourceNotFoundException {
        // select the ResourceAccessor, add the pathParams and select the ResourceMethodAccessor from the ResourceAccessor.
        return select(resourceClass).pathParams(pathParams).method(method);
    }

    @Override
    public ResourceMethodAccessor<?> select(String uri, String method) throws
            ResourceMethodNotFoundException, ResourceNotFoundException {
        // select the ResourceAccessor by the uri and select the ResourceMethodAccessor from this ResourceAccessor
        return select(uri).method(method);
    }

    private class ResourceAccessorImpl<T> implements ResourceAccessor<T> {

        private final Map<String, String> pathParameters = new HashMap<>();
        private final Resource resource;

        ResourceAccessorImpl(Resource resource) {
            this.resource = resource;
        }

        @Override
        public Resource getMetadata() {
            return resource;
        }

        @Override
        public ResourceAccessor<T> pathParam(String paramName, String paramValue) {
            this.pathParameters.put(paramName, paramValue);
            return this;
        }

        @Override
        public ResourceAccessor<T> pathParams(
                Map<String, String> params) {
            this.pathParameters.putAll(params);
            return this;
        }

        @Override
        public ResourceMethodAccessor<T> method(String methodName) throws
                ResourceMethodNotFoundException {
            // lookup the resourcemethod-metadata in the resource metadata for the given methodName
            ResourceMethod resourceMethod = resource.getMethod(methodName);
            // build a ResourceMethodAccessor for the resourcemethod-metadata
            return new ResourceMethodAccessorImpl(this, resourceMethod,
                    pathParameters);
        }

        @Override
        public Response<T> invoke(String method) throws ResourceException {
            return method(method).invoke();
        }

        @Override
        public T get() throws ResourceException {
            return method("GET").invoke().getEntity();
        }

        @Override
        public T delete() throws ResourceException {
            return method("DELETE").invoke().getEntity();
        }

        @Override
        public boolean remove() throws ResourceException {
            return method("DELETE").invoke().getStatus() == StatusCodes.OK;
        }

        @Override
        public T put() throws ResourceException {
            return method("PUT").invoke().getEntity();
        }

        @Override
        public T post() throws ResourceException {
            return method("POST").invoke().getEntity();
        }

        @Override
        public T put(Object requestBody) throws ResourceException {
            return method("PUT").body(requestBody).invoke().getEntity();
        }

        @Override
        public T post(Object requestBody) throws ResourceException {
            return method("POST").body(requestBody).invoke().getEntity();
        }
    }

    private class ResourceMethodAccessorImpl<T> implements
            ResourceMethodAccessor<T>, Request {

        private final ResourceAccessor resourceAccessor;
        private final ResourceMethod resourceMethod;
        private final Map<String, List<Object>> queryParameter = new HashMap<>();
        private final Map<String, String> pathParameters;
        private Object requestBody;
        private int expectedStatusCode = -1; // all status codes are acceptable

        ResourceMethodAccessorImpl(ResourceAccessor resourceAccessor,
                ResourceMethod resourceMethod,
                Map<String, String> pathParameters) {
            this.resourceAccessor = resourceAccessor;
            this.resourceMethod = resourceMethod;
            this.pathParameters = pathParameters;
        }

        @Override
        public ResourceMethod getMetadata() {
            return resourceMethod;
        }

        @Override
        public ResourceAccessor getResource() {
            return this.resourceAccessor;
        }

        @Override
        public ResourceMethodAccessor<T> queryParam(String name,
                Object... values) {
            List<Object> valueList = queryParameter.get(name);
            if (valueList == null) {
                valueList = new ArrayList<>();
                queryParameter.put(name, valueList);
            }
            valueList.addAll(Arrays.asList(values));
            return this;
        }

        @Override
        public ResourceMethodAccessor<T> body(Object body) {
            this.requestBody = body;
            return this;
        }

        @Override
        public ResourceMethodAccessor<T> expect(int expectedStatusCode) {
            this.expectedStatusCode = expectedStatusCode;
            return this;
        }

        private Response<?> before() {
            // lookup the interceptors defined for this method.
            for (Class<? extends ResourceMethodInterceptor> interceptorClass
                    : resourceMethod.getInterceptors()) {
                // call the before method for this interceptor
                Response<?> response = interceptors.select(interceptorClass)
                        .get().before(this);
                if (response != null) {
                    // break the execution.
                    return response;
                }
            }
            // lookup the global interceptors
            for (ResourceMethodInterceptor interceptor : interceptors.select(
                    new GlobalLiteral())) {
                // call the before method for this interceptor
                Response<?> response = interceptor.before(this);
                if (response != null) {
                    // break the execution.
                    return response;
                }
            }
            return null;
        }

        private Response<?> afterSuccess(Response<?> response) {
            // lookup the interceptors defined for this method
            for (Class<? extends ResourceMethodInterceptor> interceptorClass
                    : resourceMethod.getInterceptors()) {
                // call the afterSuccess method of the interceptor
                response = interceptors.select(interceptorClass).get()
                        .afterSuccess(this, response);
            }
            for (ResourceMethodInterceptor interceptor : interceptors.select(
                    new GlobalLiteral())) {
                // call the afterSuccess method of the interceptor
                response = interceptor.afterSuccess(this, response);
            }
            return response;
        }

        private Response<?> afterFailure(Response<?> response,
                Throwable exception) {
            // lookup the interceptors defined for this method
            for (Class<? extends ResourceMethodInterceptor> interceptorClass
                    : resourceMethod.getInterceptors()) {
                // call the afterFailure method of the interceptor
                response = interceptors.select(interceptorClass).get()
                        .afterFailure(this, response, exception);
            }
            for (ResourceMethodInterceptor interceptor : interceptors.select(
                    new GlobalLiteral())) {
                // call the afterFailure method of the interceptor
                response = interceptor.afterFailure(this, response, exception);
            }
            return response;
        }

        @Override
        public Response<T> invoke() throws ResourceException {
            // call all the interceptors before resource execution.
            Response<?> response = before();
            if (response != null) {
                // interceptor decided to break the execution. So return the response from interceptor.
                return (Response<T>) response;
            }
            // lookup the executor that is responsible for this resource method.
            ResourceMethodExecutor executor = resolve();
            // execute the resource method.
            response = executor.execute(this);
            try {
                // check for success or failure. (responseexception will be raised in case of an error.
                response.getEntity();
                // call all the interceptors after successful resource execution.
                return (Response<T>) afterSuccess(response);
            } catch (ResponseException ex) {
                // call all the interceptors after failed resource execution.
                return (Response<T>) afterFailure(response, ex.getCause());
            }
        }

        @Override
        public T invokeAndCheck() throws ResourceException {
            // call the resource and get the response.
            final Response<T> response = invoke();
            // check for expected status code. An expected status-code less than 0 means, all status codes are accepted.
            if (expectedStatusCode < 0 || response.getStatus()
                    == expectedStatusCode) {
                // is the same, so get the entity of the response.
                Object entity = response.getEntity();
                if (entity == null) {
                    // the entity is null, so no further check needed here.
                    return null;
                }
                // check the type of the entity
                if (resourceMethod.isResponseInstance(entity)) {
                    // correct, so return the entity
                    return (T) entity;
                } else if (entity instanceof String) {
                    // the entity is a string, so we assume it is an error message.
                    throw new ResourceException(((String) entity));
                } else {
                    // not a correct type.
                    throw new ResourceException(String.format(
                            "Returned entity of %S %s is not the expected type. Instead it is: %s",
                            resourceMethod.getName(), resourceMethod
                            .getResource().getName(),
                            entity.getClass().getName()));
                }
            } else {
                // the actual statuscode does not match the expected status code
                throw new ResourceException(String.format(
                        "Response of %S %s is not of expected status: %s. Actual status is: %s",
                        resourceMethod.getName(), resourceMethod.getResource()
                        .getName(), expectedStatusCode,
                        response.getStatus()));
            }
        }

        private ResourceMethodExecutor resolve() {
            final Resource resource = resourceMethod.getResource();
            final String verb = resourceMethod.getName();
            final VerbLiteral verbLiteral = new VerbLiteral(verb);
            // lookup resource method executor for name:
            Instance<ResourceMethodExecutor> executorsByName =
                    executors.select(new NamedLiteral(resource.getName()),
                    verbLiteral);
            if (executorsByName.isAmbiguous()) {
                throw new InjectionException(
                        "a ResourceMethodExecutor must be unique for name: "
                        + resource.getName() + " and verb: " + verb);
            } else if (!executorsByName.isUnsatisfied()) {
                return executorsByName.get();
            } else {
                // lookup resource method executor for uri-template
                Instance<ResourceMethodExecutor> executorsByUriTemplate =
                        executors.select(new ResourceWithURITemplateLiteral(
                        resource.getUriTemplate()), verbLiteral);
                if (executorsByUriTemplate.isAmbiguous()) {
                    throw new InjectionException(
                            "a ResourceMethodExecutor must be unique for uri-template: "
                            + resource.getUriTemplate() + " and verb: " + verb);
                } else if (!executorsByUriTemplate.isUnsatisfied()) {
                    return executorsByUriTemplate.get();
                } else {
                    // lookup resource method executor for complete uri
                    String completeUri = resource.buildUri(pathParameters);
                    Instance<ResourceMethodExecutor> executorsByUri =
                            executors.select(new ResourceWithURILiteral(
                            completeUri), verbLiteral);
                    if (executorsByUri.isAmbiguous()) {
                        throw new InjectionException(
                                "a ResourceMethodExecutor must be unique for uri: "
                                + completeUri + " and verb: " + verb);
                    } else if (!executorsByUri.isUnsatisfied()) {
                        return executorsByUri.get();
                    } else {
                        // lookup resource method executor for verb.
                        Instance<ResourceMethodExecutor> executorForVerb =
                                executors.select(verbLiteral);
                        if (executorForVerb.isAmbiguous()) {
                            throw new InjectionException(
                                    "found more than one ResourceMethodExecutors for verb: "
                                    + verb);
                        } else if (!executorForVerb.isUnsatisfied()) {
                            return executorForVerb.get();
                        } else {
                            // lookup default resource method executor
                            if (executors.isAmbiguous()) {
                                return executors.select(new DefaultLiteral())
                                        .get();
                            } else if (executors.isUnsatisfied()) {
                                throw new InjectionException(
                                        "must provide any ResourceMethodExecutors to handle: "
                                        + resourceMethod);
                            } else {
                                return executors.get();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public Map<String, String> getPathParameters() {
            return pathParameters;
        }

        @Override
        public Map<String, List<Object>> getQueryParameters() {
            return queryParameter;
        }

        @Override
        public Object getBody() {
            return requestBody;
        }
    }
}
