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

import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;

/**
 * This interface declares an access point for a resource method execution.
 * <p>
 * A resource method access point may be used as in the following * example:
 * <p>
 * <pre>
 * &#64;Stateless
 * public class MyBean {
 *
 *   &#64;Inject
 *   &#64;Verb(HttpMethods.PUT)
 *   &#64;ResourceWithURI("/resources/my/1234567890")
 *   ResourceMethodAccessor&lt;MyResource&gt; addMyResource;
 *
 *   public MyResource addMyResource(String firstName, String lastName) {
 *     MyName myName = new MyName(firstName, lastName);
 *     MyResource myresource = addMyResource()
 *         .queryParam("createIfNotExists", "true")
 *         .body(myName)
 *         .expectStatusCode(StatusCodes.CREATED)
 *         .invokeAndCheck();
 *     return myresource;
 *   }
 * }
 * </pre>
 *
 * @param <T> the type of the resources to be given here.
 * @author rhk
 * @since 2.0.0
 * @version ${project.version}
 */
public interface ResourceMethodAccessor<T> {

    /**
     * returns the metadata for the resource method, this accessor represents.
     *
     * @return
     */
    ResourceMethod getMetadata();

    /**
     * returns the resourceAccessor for the resource of this method.
     *
     * @return
     */
    ResourceAccessor getResource();

    /**
     * add a specific query param to the resourcemethod accessor
     *
     * @param name
     * @param value
     * @return
     */
    ResourceMethodAccessor<T> queryParam(String name, Object... value);

    /**
     * add a specific body to the resourcemethod accessor
     *
     * @param body
     * @return
     */
    ResourceMethodAccessor<T> body(Object body);

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
    ResourceMethodAccessor<T> expect(int expectedStatusCode);

    /**
     * invokes the method and returns the response immediatly.
     *
     * This method does no check for types or exceptions or statuscodes.
     *
     * @return
     * @throws ResourceException
     */
    Response<T> invoke() throws ResourceException;

    /**
     * invokes the method and checks, if the type of the response is the
     * expected type and if the returned status code is the expected status
     * code.
     *
     * If the check fails, a ResponseException is thrown. Otherwise the entity
     * of the response is returned.
     *
     * @return
     * @throws ResourceException
     */
    T invokeAndCheck() throws ResourceException;
}
