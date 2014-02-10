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
package de.etecture.opensource.dynamicresources.metadata;

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.RequestReader;
import java.util.Set;

/**
 *
 * @param <B> the type of the body of the request
 * @author rhk
 * @version
 * @since
 */
public interface ResourceMethodRequest<B> {

    /**
     * the request-type of the method.
     *
     * @return
     */
    Class<B> getRequestType();

    /**
     * the filters for this method.
     *
     * @return
     */
    Set<ResourceMethodRequestFilter> getFilters();

    /**
     * the mediatypes, this resource method request will consume.
     *
     * @return
     */
    Set<MediaType> getAcceptedRequestMediaTypes();

    /**
     * the responses, this resource method request will produce.
     *
     * @return
     */
    Set<ResourceMethodResponse<?>> getResponses();

    /**
     * returns the request reader, that is responsible to read this request.
     *
     * @return
     */
    RequestReader<B> getRequestReader();

    /**
     * returns the ResourceMethodResponse that produces the given mediaType.
     *
     * if there is no ResourceMethodResponse defined for this mediaType, a
     * MediaTypeNotAllowedException will be thrown.
     *
     * @param mediaType
     * @return
     * @throws MediaTypeNotAllowedException
     */
    ResourceMethodResponse<?> getResponse(MediaType mediaType) throws
            MediaTypeNotAllowedException;

    /**
     * returns the response for the given responseType type.
     *
     * if the responseType is not supported by this resourcemethod, a
     * ResponseTypeNotSupportedException is thrown.
     *
     * @param <R> the type of the response
     * @param responseType
     * @return
     * @throws ResponseTypeNotSupportedException
     */
    <R> ResourceMethodResponse<R> getResponse(Class<R> responseType) throws
            ResponseTypeNotSupportedException;
}
