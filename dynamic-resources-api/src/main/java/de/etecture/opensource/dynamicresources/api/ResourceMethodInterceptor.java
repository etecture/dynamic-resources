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

import de.etecture.opensource.dynamicresources.api.events.AfterResourceMethodExecution;
import de.etecture.opensource.dynamicresources.api.events.BeforeResourceMethodExecution;

/**
 *
 * @deprecated this type is deprecated. Intercepting resource method execution
 * is now possible by observing resource execution events. See
 * {@link BeforeResourceMethodExecution} and {@link AfterResourceMethodExecution}
 * @author rhk
 */
@Deprecated
public interface ResourceMethodInterceptor {

    /**
     * called by the dynamicresources handler before the resource is invoked.
     * <p>
     * Implementors must return null to proceed or an instance of
     * {@link ResponseImpl} to return with this response immediately (without
     * calling the resource).
     *
     * @param <T>
     * @param request
     * @return
     */
    <T> Response<T> before(Request request);

    /**
     * called by the dynamicresources handler afterSuccess the resource was
     * invoked.
     *
     * @param <T>
     * @param request
     * @param response
     * @return
     */
    <T> Response<T> afterSuccess(Request request, Response<T> response);

    /**
     * called by the dynamic resource service when an exception was thrown while
     * requesting a resource.
     * <p>
     * implementors must return an instance of {@link ResponseImpl} that is
     * later * writen to the desired media type with the corresponding
     * {@link ResponseWriter}
     *
     * @param <T>
     * @param request
     * @param originalResponse
     * @param exception the exception that was raised
     * @return
     */
    <T> Response<T> afterFailure(Request request,
            Response<T> originalResponse, Throwable exception);
}
