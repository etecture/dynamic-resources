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
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotAllowedException;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import java.util.Map;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public interface ResourceAccessor extends AccessPoint<Resource> {

    /**
     * selects the TypedResourceAccessor for the given response-type.
     *
     * @param <R>
     * @param responseType
     * @return
     * @throws ResponseTypeNotSupportedException
     */
    <R> TypedResourceAccessor<R> select(Class<R> responseType) throws
            ResponseTypeNotSupportedException;

    /**
     * selects the TypedResourceAccessor for the given media-type.
     *
     * @param <R>
     * @param mediaType
     * @return
     * @throws MediaTypeNotAllowedException
     */
    <R> TypedResourceAccessor<R> select(MediaType mediaType) throws
            MediaTypeNotAllowedException;

    /**
     * invokes the method and returns the response immediatly.
     *
     * This is a shortcut for:
     * <code>this.method(method, responseType).invoke();</code>
     *
     * This method does not check status codes and does not add query-parameters
     * to invoke the method!
     *
     * @param <R>
     * @param method
     * @param responseType
     * @return
     * @throws ResourceException
     */
    <R> Response<R> invoke(String method, Class<R> responseType) throws
            ResourceException;

    /**
     * selects the method for the resource and returns the responsible accessor.
     *
     * @param <R>
     * @param methodName
     * @param responseType
     * @return
     * @throws ResponseTypeNotSupportedException
     * @throws ResourceMethodNotFoundException
     */
    <R> MethodAccessor<R> method(String methodName,
            Class<R> responseType) throws
            ResponseTypeNotSupportedException,
            ResourceMethodNotFoundException;

    /**
     * selects a specific representation of this resource by using the given
     * parameter.
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    ResourceAccessor pathParam(String paramName, String paramValue);

    /**
     * add all the path parameters.
     *
     * @param params
     * @return
     */
    ResourceAccessor pathParams(Map<String, String> params);

}
