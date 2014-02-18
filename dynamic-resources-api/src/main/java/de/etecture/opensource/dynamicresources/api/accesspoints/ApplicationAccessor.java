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
package de.etecture.opensource.dynamicresources.api.accesspoints;

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeAmbigiousException;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import java.util.Map;

/**
 * This interface declares an access point for all ApplicationAccessor.
 * <p>
 * A resources access point may be used as in the following example:
 * <p>
 * <pre>
 * &#64;Stateless
 * public class MyBean {
 *
 *   &#64;Inject
 *   &#64;Named("MyApplication")
 *   ApplicationAccessor myresources;
 *
 *   public String getInformation() {
 *     MyResource myresource = myresources
 *         .select("MyResource", MyResource.class)
 *         .pathParam("id", "1234567890")
 *         .method(HttpMethods.GET)
 *         .invoke().getEntity();
 *     return myresource.getInformation();
 *   }
 *
 *   public String getSimplifiedInformation() {
 *     MyResource myresource = myresources
 *         .select(URI.create("/resources/my/1234567890"), HttpMethods.GET, MyResource.class)
 *         .expectStatusCode(StatusCodes.OK)
 *         .invokeAndCheck();
 *     return myresource.getInformation();
 *   }
 *
 *   public String getDescription() {
 *     return myresources
 *         .getApplication()
 *         .getDescription();
 *   }
 * }
 * </pre>
 *
 * @author rhk
 * @version ${project.version}
 * @since 2.0.0
 */
public interface ApplicationAccessor extends AccessPoint<Application> {

    /**
     * select a specific resource for the given name and the responseType.
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor resources;
     *
     *   public void someMethod() {
     *     TypedResourceAccessor&lt;Customers&gt; customersAccessor =
     *       resources.selectByName("Customers", Customers.class);
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param name the name of the resource to select
     * @param responseType the expected responsetype
     * @return
     * @throws ResourceNotFoundException
     * @throws ResponseTypeNotSupportedException
     */
    <X> TypedResourceAccessor<X> selectByName(String name,
            Class<X> responseType)
            throws
            ResourceNotFoundException, ResponseTypeNotSupportedException;

    /**
     * select a specific resource for a given path.
     * <p>
     * N.B. the path-parameter extracted from the given path are automatically
     * set in the returned TypedResourceAccessor.
     * <p>
     * The path is the directly decendant of the applications path.
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor resources;
     *
     *   public void someMethod() {
     *     TypedResourceAccessor&lt;Customer&gt; customerAccessor =
     *       resources.selectByPath(
     *         "/resources/customers/1234567890",
     *         Customer.class);
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param path
     * @param responseType
     * @return
     * @throws ResourceNotFoundException
     * @throws ResponseTypeNotSupportedException
     */
    <X> TypedResourceAccessor<X> selectByPath(String path,
            Class<X> responseType)
            throws
            ResourceNotFoundException, ResponseTypeNotSupportedException;

    /**
     * selects a specific resource with the given name.
     *
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor resources;
     *
     *   public void someMethod() {
     *     ResourceAccessor customerAccessor =
     *       resources.selectByName("Customer");
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param name
     * @return
     * @throws ResourceNotFoundException
     */
    ResourceAccessor selectByName(String name) throws ResourceNotFoundException;

    /**
     * selects a specific resource with the given path.
     *
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor resources;
     *
     *   public void someMethod() {
     *     ResourceAccessor customerAccessor =
     *       resources.selectByPath("/customers/1234567890");
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param path
     * @return
     * @throws ResourceNotFoundException
     */
    ResourceAccessor selectByPath(String path) throws ResourceNotFoundException;

    /**
     * selects the specific resource for a given path, the specified
     * response-type and it's method.
     * <p>
     * N.B.the path-parameter extracted from the given path are automatically
     * set in the returned MethodAccessor.
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor resources;
     *
     *   public void someMethod() {
     *     MethodAccessor&lt;CustomerAddress&gt; addCustomerAddressAccessor =
     *       resources.selectByPath(
     *         "/customer/1234567890/addresses/address1"
     *         HttpMethods.PUT,
     *         CustomerAddress.class);
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param path
     * @param responseType
     * @param method
     * @return
     * @throws ResourceMethodNotFoundException
     * @throws ResourceNotFoundException
     * @throws ResponseTypeNotSupportedException
     */
    <X> MethodAccessor<X> selectByPath(String path, String method,
            Class<X> responseType)
            throws ResourceMethodNotFoundException, ResourceNotFoundException,
            ResponseTypeNotSupportedException;

    /**
     * selects the specific resource for a given path, the specified
     * response-type and it's method.
     * <p>
     * N.B.the path-parameter extracted from the given path are automatically
     * set in the returned MethodAccessor.
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor resources;
     *
     *   public void someMethod() {
     *     MethodAccessor&lt;CustomerAddress&gt; addCustomerAddressAccessor =
     *       resources.selectByPath(
     *         "/customer/1234567890/addresses/address1"
     *         HttpMethods.PUT,
     *         CustomerAddress.class);
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param path
     * @param acceptedMediaType
     * @param method
     * @return
     * @throws ResourceMethodNotFoundException
     * @throws ResourceNotFoundException
     * @throws MediaTypeNotSupportedException
     * @throws MediaTypeAmbigiousException
     */
    <X> MethodAccessor<X> selectByPathAndMime(String path, String method,
            MediaType acceptedMediaType)
            throws ResourceMethodNotFoundException, ResourceNotFoundException,
            MediaTypeNotSupportedException, MediaTypeAmbigiousException;

    /**
     * selects the specific resource for a given response-type and it's method.
     * <p>
     * N.B. the pathParamValues must be specified in the order they appear in
     * the uri template of the resource.
     *
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor resources;
     *
     *   public void someMethod() {
     *     MethodAccessor&lt;CustomerAddress&gt; addCustomerAddressAccessor =
     *       resources.select(
     *         "CustomerAddress"
     *         HttpMethods.PUT,
     *         CustomerAddress.class, "1234567890", "address1");
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param name
     * @param responseType
     * @param method
     * @param pathParamValues
     * @return
     * @throws ResourceMethodNotFoundException
     * @throws ResourceNotFoundException
     * @throws ResponseTypeNotSupportedException
     */
    <X> MethodAccessor<X> selectByName(String name, String method,
            Class<X> responseType,
            String... pathParamValues)
            throws ResourceMethodNotFoundException, ResourceNotFoundException,
            ResponseTypeNotSupportedException;

    /**
     * selects the specific resource for a given name, the specified
     * response-type and it's method.
     * <p>
     * N.B. the pathParams are applied to the MethodAccessor.
     *
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor resources;
     *
     *   public void someMethod() {
     *     MethodAccessor&lt;Customer&gt; addCustomerAccessor =
     *       resources.selectByName(
     *         "Customer",
     *         HttpMethods.PUT,
     *         Customer.class,
     *         Collections.singletonMap("id", "1234567890"));
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param name
     * @param responseType
     * @param method
     * @param pathParams
     * @return
     * @throws ResourceMethodNotFoundException
     * @throws ResourceNotFoundException
     * @throws ResponseTypeNotSupportedException
     */
    <X> MethodAccessor<X> selectByName(String name, String method,
            Class<X> responseType,
            Map<String, String> pathParams)
            throws ResourceMethodNotFoundException, ResourceNotFoundException,
            ResponseTypeNotSupportedException;
}
