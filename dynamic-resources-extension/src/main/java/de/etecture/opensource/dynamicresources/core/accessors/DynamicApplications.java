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
package de.etecture.opensource.dynamicresources.core.accessors;

import de.etecture.opensource.dynamicrepositories.utils.NamedLiteral;
import de.etecture.opensource.dynamicresources.api.accesspoints.Applications;
import de.etecture.opensource.dynamicresources.api.accesspoints.Methods;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodsForResponse;
import de.etecture.opensource.dynamicresources.api.accesspoints.Resources;
import de.etecture.opensource.dynamicresources.api.accesspoints.Responses;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.ApplicationNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourcePathNotMatchException;
import de.etecture.opensource.dynamicresources.utils.InjectionPointHelper;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * this is the standard implementation for {@link Applications}.
 *
 * It provides the implementation for the
 * <code>select()</code>-methods in the {@link Application} interface as well as
 * producer methods to be able to inject qualified
 * {@link Resources}, {@link MethodsForResponse}s and {@link Responses}s.
 *
 * @author rhk
 * @version
 * @since
 * @see Resources
 */
@Default
public class DynamicApplications implements Applications {

    /**
     * the allApplications.
     */
    @Inject
    Instance<Application> allApplications;
    /**
     * an accesspoint to all available resource-metadatas.
     */
    @Inject
    @Any
    Instance<Resource> allResources;
    /**
     * the bean manager.
     */
    @Inject
    BeanManager beanManager;

    /**
     * produces a MethodsForResponse for an injectionPoint annotated with a
     * {@link Resource} to access an Methods by uri annotation.
     *
     * searches the resourceaccessor for the uri specified with the annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;Application("CustomerResourceApplication") // optional, if unique
     *   &#64;Resource("/resources/customers/1234567890/")
     *   MethodsForResponse&lt;CustomerResource&gt; customerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param ip
     * @return
     */
    @Produces
    @de.etecture.opensource.dynamicresources.annotations.Resource
    public <T> MethodsForResponse<T> produceMethodsForResourceWithUri(
            InjectionPoint ip) {
        // lookup the @Application annotation of this injection point
        de.etecture.opensource.dynamicresources.annotations.Application app =
                InjectionPointHelper.findQualifier(
                de.etecture.opensource.dynamicresources.annotations.Application.class,
                ip);
        // lookup the path from the @Resource annotation of this ip
        String path = InjectionPointHelper.findQualifier(
                de.etecture.opensource.dynamicresources.annotations.Resource.class,
                ip).path();
        Instance<Resource> resources;
        if (app != null) {
            // resolve the application's resource
            resources = allResources.select(app);
        } else {
            // no @Application specified, so we lookup ALL resources.
            resources = allResources;
        }
        Resource resource = null;
        // lookup the resource that match the path
        for (Resource r : resources) {
            if (r.getPath().matches(path)) {
                resource = r;
                break;
            }
        }
        if (resource == null) {
            throw new InjectionException(
                    "no resource found that matches the path: " + path);
        }
        // get the type of the response (which is the 0th element of the
        // MethodsForResponse type)
        Class<T> responseType = InjectionPointHelper
                .getGenericTypeOfInjectionPoint(ip, 0);
        if (responseType == null) {
            throw new InjectionException(
                    "cannot determine the response-type for the TypedResourceAccessor, so cannot find an appropriate resource!");
        } else {
            // check the response type
            for (ResourceMethod method : resource.getMethods().values()) {
                if (method.getResponses().containsKey(responseType)) {
                    return DynamicMethodsForResponse.create(beanManager,
                            responseType, resource);
                }
            }
            throw new InjectionException(
                    "cannot produce a TypedResourceAccessor for the resource: "
                    + resource.getName()
                    + ", due to it does not support the response-type: "
                    + responseType.getName());
        }
    }

    /**
     * produces a MethodsForResponse for an injectionPoint annotated with a
     * {@link Named} annotation. searches the resourceaccessor for the name
     * specified with the annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;Application("CustomerResourceApplication") // optional if unique
     *   &#64;Named("CustomerResource")
     *   MethodsForResponse&lt;CustomerResource&gt; customerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param <T>
     * @param ip
     * @return
     */
    @Produces
    @Named
    public <T> MethodsForResponse<T> produceMethodsForResourceWithName(
            InjectionPoint ip) {
        // lookup the @Named and @Application annotation of this injection point
        Annotation[] annotations = InjectionPointHelper.findQualifiers(ip,
                Named.class,
                de.etecture.opensource.dynamicresources.annotations.Application.class);
        // lookup the resource that match the criteria:
        Resource resource = allResources.select(annotations).get();
        // get the type of the response (which is the 0th element of the
        // MethodsForResponse type)
        Class<T> responseType = InjectionPointHelper
                .getGenericTypeOfInjectionPoint(ip, 0);
        if (responseType == null) {
            throw new InjectionException(
                    "cannot determine the response-type for the TypedResourceAccessor, so cannot find an appropriate resource!");
        } else {
            // check the response type
            for (ResourceMethod method : resource.getMethods().values()) {
                if (method.getResponses().containsKey(responseType)) {
                    return DynamicMethodsForResponse.create(beanManager,
                            responseType, resource);
                }
            }
            throw new InjectionException(
                    "cannot produce a TypedResourceAccessor for the resource: "
                    + resource.getName()
                    + ", due to it does not support the response-type: "
                    + responseType.getName());
        }
    }

    @Produces
    @Named
    public Resources produceResourcesForApplicationWithName(
            InjectionPoint ip) {
        try {
            return selectByName(InjectionPointHelper.findQualifier(Named.class,
                    ip)
                    .value());
        } catch (ApplicationNotFoundException ex) {
            throw new InjectionException(ex);
        }
    }

    @Produces
    @de.etecture.opensource.dynamicresources.annotations.Application
    public Resources produceResourcesForApplicationWithUri(
            InjectionPoint ip) {
        try {
            return selectByPath(InjectionPointHelper.findQualifier(
                    de.etecture.opensource.dynamicresources.annotations.Application.class,
                    ip).base());
        } catch (ApplicationNotFoundException ex) {
            throw new InjectionException(ex);
        }
    }

    @Override
    public Set<String> getApplicationNames() {
        Set<String> applicationNames = new HashSet<>();
        for (Application application : allApplications) {
            applicationNames.add(application.getName());
        }
        return Collections.unmodifiableSet(applicationNames);
    }

    @Override
    public Resources selectByName(String applicationName) throws
            ApplicationNotFoundException {
        // find the applications with the given name.
        final Instance<Application> applicationsWithName = allApplications
                .select(new NamedLiteral(applicationName));
        if (!applicationsWithName.isUnsatisfied() && !applicationsWithName
                .isAmbiguous()) {
            Application application = applicationsWithName.get();
            // return a new Resources Accessor for this application
            return DynamicResources.create(beanManager, application);
        } else if (applicationsWithName.isAmbiguous()) {
            throw new ApplicationNotFoundException(
                    "the application name is not unique: " + applicationName);
        } else {
            throw new ApplicationNotFoundException("an application with name: "
                    + applicationName + " does not exists.");
        }
    }

    @Override
    public Resources selectByPath(String basePath) throws
            ApplicationNotFoundException {
        // lookup the allApplications for the baseUri
        for (Application application : allApplications) {
            if (application.getBase().equals(basePath)) {
                // found, so return a new Resources Accessor for this application
                return DynamicResources.create(beanManager, application);
            }
        }
        throw new ApplicationNotFoundException(
                "There is no application registered, that is responsible for the given path: "
                + basePath);
    }

    @Override
    public Methods findForCompleteUri(String uri) throws
            ResourceNotFoundException, ApplicationNotFoundException {
        // lookup the allApplications baseUri...
        for (Application application : allApplications) {
            if (uri.startsWith(application.getBase())) {
                Resource resource = application.findResource(uri);
                try {
                    // found, so return a new Resources Accessor for this application
                    return DynamicMethods.create(beanManager, resource)
                            .pathParams(
                            resource.getPath().getPathParameterValues(uri));
                } catch (ResourcePathNotMatchException ex) {
                    throw new ResourceNotFoundException(ex);
                }
            }
        }
        throw new ApplicationNotFoundException(
                "there is no application that holds any resource matching the path: "
                + uri);
    }
}
