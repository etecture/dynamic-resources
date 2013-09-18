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
     * retrieves a representation of this resource by using the <b>GET</b>
     * method.
     *
     * @return
     */
    T GET();

    /**
     * updates or creates a representation of this resource by using the
     * <b>PUT</b> method.
     *
     * @param content
     * @return
     */
    T PUT(T content);

    /**
     * creates a representation of this resource by using the <b>POST</b>
     * method.
     *
     * @param content
     * @return
     */
    T POST(T content);

    /**
     * creates a representation of this resource by using the <b>POST</b>
     * method.
     *
     * @return
     */
    T POST();

    /**
     * deletes a representation of this resource by using the <b>DELETE</b>
     * method.
     *
     */
    void DELETE();
}