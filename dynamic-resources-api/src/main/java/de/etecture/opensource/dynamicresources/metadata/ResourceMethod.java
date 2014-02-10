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

import de.etecture.opensource.dynamicrepositories.executor.Query;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.ResourceMethodInterceptor;
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
     * returns the queries associated with this request.
     *
     * @return
     */
    Set<Query> getQueries();

    /**
     * returns the resourceinterceptors associated with this request;
     *
     * @return
     */
    Set<Class<? extends ResourceMethodInterceptor>> getInterceptors();

    /**
     * returns true, if the role is allowed to execute this resource.
     *
     * @param role
     * @return
     */
    boolean isAllowed(String role);

    /**
     * the requests, this resource-method will consume.
     *
     * @return
     */
    Set<ResourceMethodRequest<?>> getRequests();

    /**
     * returns the request for the given mediatype.
     *
     * If the mediatype is not defined for this request, a
     * MediaTypeNotSupportedException will be thrown.
     *
     * @param mediaType
     * @return
     * @throws MediaTypeNotSupportedException
     */
    ResourceMethodRequest<?> getRequest(MediaType mediaType) throws
            MediaTypeNotSupportedException;

    /**
     * returns the request for the given requestType type.
     *
     * if the requestType is not supported by this resourcemethod, a
     * RequestTypeNotSupportedException is thrown.
     *
     * @param <B> the type of the request
     * @param requestType
     * @return
     * @throws RequestTypeNotSupportedException
     */
    <B> ResourceMethodRequest<B> getRequest(Class<B> requestType) throws
            RequestTypeNotSupportedException;

    /**
     * returns true, if the given response entity is an instance of an available
     * response of this method.
     *
     * @param entity
     * @return
     */
    public boolean isResponseInstance(Object entity);
}
