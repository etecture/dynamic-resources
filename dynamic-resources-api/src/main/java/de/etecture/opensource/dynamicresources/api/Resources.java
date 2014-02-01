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
package de.etecture.opensource.dynamicresources.api;

import java.util.Map;

/**
 * This interface declares an injection point for Resources.
 * <p>
 * A resources injection point may be used as in the following example:
 * <p>
 * <pre>
 * &#64;Stateless
 * public class MyBean {
 *
 *   &#64;Inject
 *   Resources&lt;MyResource&gt; myresources;
 *
 *   public String getInformation() {
 *     Map&lt;String, Object&gt; params = new HashMao&lt;&gt;;
 *     params.put("id", "1234567890");
 *     MyResource myresource = myresources.select(params).GET();
 *     return myresource.getInformation();
 *   }
 * }
 * </pre>
 *
 * @param <T> the type of the resources to be given here.
 * @author rhk
 * @since 1.0.0
 * @version ${project.version}
 */
public interface Resources<T> {

    /**
     * selects a specific representation of this resource by using the given
     * parameters.
     *
     * @param params
     * @return
     */
    Resources<T> select(Map<String, Object> params);

    /**
     * selects a specific representation of this resource by using the given
     * parameter.
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    Resources<T> select(String paramName, Object paramValue);

    /**
     * specifies the request body to be sent to the resource.
     *
     * @param requestBody
     * @return
     */
    Resources<T> body(Object requestBody);

    /**
     * specifies that a specific status code is expected. If the response is not
     * of the given status code, a ResourceException is thrown on the
     * shortcut-methods.
     *
     * If this method is not called, no status-code check will be performed.
     *
     * @param expectedStatusCode
     * @return
     */
    Resources<T> expect(int expectedStatusCode);

    /**
     * invokes the method and returns the response immediatly.
     *
     * This method does no check for types or exceptions or statuscodes.
     *
     * @param method
     * @return
     * @throws ResourceException
     */
    Response<T> invoke(String method) throws ResourceException;

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
