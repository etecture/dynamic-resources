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

import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import java.util.Map;

/**
 * This interface declares an access point for all Resources.
 * <p>
 * A resources access point may be used as in the following example:
 * <p>
 * <pre>
 * &#64;Stateless
 * public class MyBean {
 *
 *   &#64;Inject
 *   Resources myresources;
 *
 *   public String getInformation() {
 *     MyResource myresource = myresources
 *         .select(MyResource.class)
 *         .pathParam("id", "1234567890")
 *         .method(HttpMethods.GET)
 *         .invoke().getEntity();
 *     return myresource.getInformation();
 *   }
 *
 *   public String getSimplifiedInformation() {
 *     MyResource myresource = myresources
 *         .select("/resources/my/1234567890", HttpMethods.GET)
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
public interface Resources {

    /**
     * returns the metadata for the application that contains the resources.
     *
     * @return
     */
    Application getMetadata();

    /**
     * select a specific resource for a given resourceClass.
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   Resources resources;
     *
     *   public void someMethod() {
     *     ResourceAccessor&lt;Customers&gt; customersAccessor =
     *       resources.select(Customers.class);
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param resourceClass
     * @return
     * @throws ResourceNotFoundException
     */
    <X> ResourceAccessor<X> select(Class<X> resourceClass) throws
            ResourceNotFoundException;

    /**
     * select a specific resource for a given URI.
     *
     * N.B. the path-parameter extracted from the given uri are automatically
     * set in the returned ResourceAccessor.
     *
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   Resources resources;
     *
     *   public void someMethod() {
     *     ResourceAccessor customerAccessor =
     *       resources.select("/resources/customers/1234567890");
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param uri
     * @return
     * @throws ResourceNotFoundException
     */
    ResourceAccessor<?> select(String uri) throws ResourceNotFoundException;

    /**
     * selects the specific resource for a given class and it's method.
     *
     * N.B. the pathParamValues must be specified in the order they appear in
     * the uri template of the resource.
     *
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   Resources resources;
     *
     *   public void someMethod() {
     *     ResourceMethodAccessor&lt;Customer&gt; addCustomerAccessor =
     *       resources.select(Customer.class, HttpMethods.PUT, "1234567890");
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param resourceClass
     * @param method
     * @param pathParamValues
     * @return
     * @throws ResourceMethodNotFoundException
     * @throws ResourceNotFoundException
     */
    <X> ResourceMethodAccessor<X> select(Class<X> resourceClass, String method,
            String... pathParamValues)
            throws ResourceMethodNotFoundException, ResourceNotFoundException;

    /**
     * selects the specific resource for a given class and it's method.
     *
     * N.B. the pathParams are applied to the ResourceMethodAccessor.
     *
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   Resources resources;
     *
     *   public void someMethod() {
     *     ResourceMethodAccessor&lt;Customer&gt; addCustomerAccessor =
     *       resources.select(Customer.class, HttpMethods.PUT,
     *           Collections.singletonMap("id", "1234567890"));
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param <X>
     * @param resourceClass
     * @param method
     * @param pathParams
     * @return
     * @throws ResourceMethodNotFoundException
     * @throws ResourceNotFoundException
     */
    <X> ResourceMethodAccessor<X> select(Class<X> resourceClass, String method,
            Map<String, String> pathParams)
            throws ResourceMethodNotFoundException, ResourceNotFoundException;

    /**
     * selects the specific resource for a given class and it's method.
     *
     * N.B. the path-parameter extracted from the given uri are automatically
     * set in the returned ResourceMethodAccessor.
     *
     * <p>
     * Example:
     * <pre>
     * public class MyBean {
     *   &#64;Inject
     *   Resources resources;
     *
     *   public void someMethod() {
     *     ResourceMethodAccessor removeCustomerAccessor =
     *       resources.select("/resources/customers/1234567890", HttpMethods.DELETE);
     *     // ...
     *   }
     * }
     * </pre>
     *
     * @param uri
     * @param method
     * @return
     * @throws ResourceMethodNotFoundException
     * @throws ResourceNotFoundException
     */
    ResourceMethodAccessor<?> select(String uri, String method) throws
            ResourceMethodNotFoundException, ResourceNotFoundException;
}
