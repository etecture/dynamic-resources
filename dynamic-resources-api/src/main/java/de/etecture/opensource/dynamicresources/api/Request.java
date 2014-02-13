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
package de.etecture.opensource.dynamicresources.api;

import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import java.util.Set;

/**
 * This interface represents a concrete request.
 *
 * A concrete request is a Resource-Method-Execution-Request filled with
 * concrete path parameters, query parameters and a request body.
 *
 * @param <R> the expected type of the response.
 * @param <B> the type of the body of this request.
 * @author rhk
 * @version
 * @since
 */
public interface Request<R, B> {

    /**
     * returns the metadata request that represents the body of this request.
     * <p>
     * N.B. if the body of this request is null, which means, that no body is
     * associated with this request, then this method may also return null.
     *
     * @return
     */
    ResourceMethodRequest<B> getRequestMetadata();

    /**
     * returns the metadata for the resource method response associated with
     * this request.
     *
     * @return
     */
    ResourceMethodResponse<R> getResponseMetadata();

    /**
     * returns all the names of the parameters for this request.
     *
     * @return
     */
    Set<String> getParameterNames();

    /**
     * returns the value for the parameter with the given name defined for this
     * request.
     *
     * If no parameter with the given name was defined, the defaultValue is
     * returned.
     *
     * @param <T>
     * @param name
     * @param defaultValue
     * @return
     */
    <T> T getParameterValue(String name, T defaultValue);

    /**
     * returns the value for the parameter with the given name defined for this
     * request.
     *
     * If no parameter with the given name was defined, null is returned.
     *
     * @param name
     * @return
     */
    Object getParameterValue(String name);

    /**
     * defines a new value for the parameter with the specified name of this
     * request.
     *
     * @param name
     * @param value
     */
    void setParameterValue(String name, Object value);

    /**
     * returns true, if a parameter with the given name was defined for this
     * request.
     *
     * @param name
     * @return
     */
    boolean hasParameter(String name);

    /**
     * removes a parameter with the specified name from this request.
     *
     * @param name
     */
    void removeParameter(String name);

    /**
     * returns the request body.
     *
     * @return
     */
    B getBody();
}
