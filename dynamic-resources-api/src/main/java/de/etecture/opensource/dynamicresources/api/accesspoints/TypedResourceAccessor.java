/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.api.accesspoints;

import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import java.util.Map;

/**
 * This interface declares an access point for a resource that produces the
 * response-type.
 * <p>
 * A resource access point may be used as in the following example:
 * <p>
 * <pre>
 * &#64;Stateless
 * public class MyBean {
 *
 *   &#64;Inject
 *   &#64;Named("MyResource")
 *   TypedResourceAccessor&lt;MyResource&gt; myresources;
 *
 *   public String getInformation() {
 *     MyResource myresource = myresources.pathParam("id", "1234567890").get();
 *     return myresource.getInformation();
 *   }
 * }
 * </pre>
 * <p>
 * another example for using the &#64;Resource annotation to select a resource
 * by an URI:
 * <pre>
 * public class MyBean {
 *
 *   &#64;Inject
 *   &#64;Application("Customers-Application") // optional.
 *   &#64;Resource("/customers/1234567890/addresses")
 *   TypedResourceAccessor&lt;CustomerAddresses&gt; addressesForACustomer;
 *
 *   public void addAddress(CustomerAddress address) {
 *     addressesForACustomer.put(address);
 *   }
 * }
 * </pre>
 *
 * @param <T> the type of the resources to be given here.
 * @author rhk
 * @since 2.0.0
 * @version ${project.version}
 */
public interface TypedResourceAccessor<T> extends AccessPoint<Resource> {

    /**
     * returns the selected response type for this Accessor.
     *
     * @return
     */
    Class<T> getSelectedResponseType();

    /**
     * selects the TypedResourceAccessor for another response-type of the
     * <b>SAME</b> resource..
     *
     * @param <R>
     * @param responseType
     * @return
     * @throws ResponseTypeNotSupportedException
     */
    <R> TypedResourceAccessor<R> select(Class<R> responseType) throws
            ResponseTypeNotSupportedException;

    /**
     * invokes the method and returns the response immediatly.
     *
     * This is a shortcut for:
     * <code>this.method(method).invoke();</code>
     *
     * This method does not check status codes and does not add query-parameters
     * to invoke the method!
     *
     * @param method
     * @return
     * @throws ResourceException
     */
    Response<T> invoke(String method) throws ResourceException;

    /**
     * selects the method for the resource and returns the responsible accessor.
     *
     * @param methodName
     * @return
     * @throws ResourceMethodNotFoundException
     */
    MethodAccessor<T> method(String methodName) throws
            ResourceMethodNotFoundException;

    /**
     * selects a specific representation of this resource by using the given
     * parameter.
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    TypedResourceAccessor<T> pathParam(String paramName, String paramValue);

    /**
     * add all the path parameters.
     *
     * @param params
     * @return
     */
    TypedResourceAccessor<T> pathParams(Map<String, String> params);

    /**
     * shortcut for: {code}invoke("GET").getEntity(){code}
     *
     * @return
     * @throws ResourceException
     */
    T get() throws ResourceException;

    /**
     * shortcut for: {code}invoke("DELETE").getEntity(){code}
     *
     * @return
     * @throws ResourceException
     */
    T delete() throws ResourceException;

    /**
     * shortcut for: {code}invoke("DELETE").getStatus() == {code}
     *
     * @return
     * @throws ResourceException
     */
    boolean remove() throws ResourceException;

    /**
     * shortcut for: {code}invoke("PUT").getEntity(){code}
     *
     * @return
     * @throws ResourceException
     */
    T put() throws ResourceException;

    /**
     * shortcut for: {code}invoke("POST").getEntity(){code}
     *
     * @return
     * @throws ResourceException
     */
    T post() throws ResourceException;

    /**
     * shortcut for: {code}body(requestBody).invoke("PUT").getEntity(){code}
     *
     * @param requestBody
     * @return
     * @throws ResourceException
     */
    T put(Object requestBody) throws ResourceException;

    /**
     * shortcut for: {code}body(requestBody).invoke("POST").getEntity(){code}
     *
     * @param requestBody
     * @return
     * @throws ResourceException
     */
    T post(Object requestBody) throws ResourceException;
}
