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
import java.util.Map;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public interface ResourceMethod {

    /**
     * the resource for which this method is defined.
     *
     * @return
     */
    Resource getResource();

    /**
     * the name of this method
     *
     * @return
     */
    String getName();

    /**
     * the description of this method
     *
     * @return
     */
    String getDescription();

    /**
     * returns the allowed roles names for this method.
     *
     * @return
     */
    Set<String> getAllowedRoleNames();

    /**
     * the requests, this resource-method will consume.
     * <p>
     * Hint: see the description for {@linkplain #getResponses()}. The described
     * possibilities of the returned map do also apply for the returned map of
     * this method.
     *
     * @return
     */
    Map<Class<?>, ResourceMethodRequest<?>> getRequests();

    /**
     * the filters for this method.
     *
     * @return
     */
    Set<ResourceMethodFilter<?>> getFilters();

    /**
     * returns the possible requests for the given mediatype.
     * <p>
     * If the mediatype is not defined for this request, a
     * MediaTypeNotAllowedException will be thrown.
     *
     * @param mediaType
     * @return
     * @throws MediaTypeNotAllowedException
     */
    Set<ResourceMethodRequest<?>> getRequests(MediaType mediaType) throws
            MediaTypeNotAllowedException;

    /**
     * returns the specific request for the given mediatype.
     * <p>
     * If the mediatype is not defined for this request, a
     * MediaTypeNotAllowedException will be thrown.
     * <p>
     * If there are more then one requests specified that consumes the given
     * mediatype, then a MediaTypeAmbigiousException is thrown.
     *
     * @param mediaType
     * @return
     * @throws MediaTypeNotAllowedException
     * @throws MediaTypeAmbigiousException
     */
    ResourceMethodRequest<?> getRequest(MediaType mediaType) throws
            MediaTypeNotAllowedException, MediaTypeAmbigiousException;

    /**
     * the responses, this resource method will produce.
     * <p>
     * Hint: the returned map is aware of polymorphism and class hierarchy. This
     * means, that a
     * <code>getResponses().get(Object.class)</code> will return the first
     * response (if any), due to
     * <code>Object.class</code> is assignable from ANY type.
     * <p>
     * In other words: Assume the method produces
     * <code>String.class</code> and
     * <code>Number.class</code> responses. Then a call of
     * <code>getResponses().get(Integer.class)</code> will return the
     * <code>Number.class</code> response.
     * <p>
     * You can also call the
     * <code>get()</code> method of the returned map with an instance of a class
     * and the
     * <code>get()</code> will return the response for which the given key is an
     * instance of.
     * <p>
     * The same is true for
     * <code>getResponses().containsKey()</code>.
     *
     * @return
     */
    Map<Class<?>, ResourceMethodResponse<?>> getResponses();

    /**
     * returns the ResourceMethodResponse that produces the given mediaType.
     *
     * if there is no ResourceMethodResponse defined for this mediaType, a
     * MediaTypeNotSupportedException will be thrown.
     *
     * @param mediaType
     * @return
     * @throws MediaTypeNotSupportedException
     */
    Set<ResourceMethodResponse<?>> getResponses(MediaType mediaType) throws
            MediaTypeNotSupportedException;

    /**
     * returns the specific response for the given mediatype.
     * <p>
     * If the mediatype is not defined for this response, a
     * MediaTypeNotSupportedException will be thrown.
     * <p>
     * If there are more then one responses specified that produces the given
     * mediatype, then a MediaTypeAmbigiousException is thrown.
     *
     * @param mediaType
     * @return
     * @throws MediaTypeNotSupportedException
     * @throws MediaTypeAmbigiousException
     */
    ResourceMethodResponse<?> getResponse(MediaType mediaType) throws
            MediaTypeNotSupportedException, MediaTypeAmbigiousException;
}
