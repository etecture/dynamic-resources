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

/**
 * handles exceptions.
 *
 * @author rhk
 */
public interface ExceptionHandler {

    /**
     * called by the dynamic resource service to check, if this exception
     * handler is responsible for a specific type of exceptions.
     * <p>
     * implementors must return true, if the dynamic resource service should use
     * this exception handler by calling it's
     * {@link ExceptionHandler#handleException(java.lang.Class, java.lang.String, java.lang.Throwable)}
     * method.
     *
     * @param request
     * @param exceptionClass
     * @return
     */
    boolean isResponsibleFor(Request request,
            Class<? extends Throwable> exceptionClass);

    /**
     * called by the dynamic resource service when an exception was thrown while
     * requesting a resource.
     * <p>
     * implementors must return an instance of {@link ResponseImpl} that is
     * later     * writen to the desired media type with the corresponding
     * {@link ResponseWriter}
     *
     * @param request
     * @param exception the exception that was raised
     * @return
     */
    Response<?> handleException(Request request,
            Throwable exception);
}
