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
package de.etecture.opensource.dynamicresources.api.events;

import de.etecture.opensource.dynamicresources.annotations.Failed;
import de.etecture.opensource.dynamicresources.annotations.Succeed;
import de.etecture.opensource.dynamicresources.api.Response;
import java.util.Date;

/**
 * this is a CDI-event that is fired after the Resource Method was executed.
 * <p>
 * Observers of this event may use {@link Succeed} or {@link Failed} to catch
 * only succeed or failed resource method executions.
 * <p>
 * @author rhk
 * @version
 * @since
 */
public interface AfterExecutionEvent extends
        ResourceMethodExecutionEvent, Response {

    /**
     * returns the original response that was produced by the method execution.
     * <p>
     * @return
     */
    Object getOriginalEntity();

    /**
     * specifies the new response entity, that should be returned for this
     * method execution other then the original entity.
     * <p>
     * @param entity
     */
    void setNewEntity(Object entity);

    /**
     * specifies the new status code.
     * <p>
     * @param statusCode
     */
    void setStatusCode(int statusCode);

    /**
     * adds a string header value.
     * <p>
     * @param name
     * @param value
     */
    void addHeaderValue(String name, String value);

    /**
     * adds a number header value.
     * <p>
     * @param name
     * @param number
     */
    void addHeaderValue(String name, Number number);

    /**
     * adds a date header value.
     * <p>
     * @param name
     * @param date
     */
    void addHeaderValue(String name, Date date);

    /**
     * sets a single string header value.
     * <p>
     * @param name
     * @param value
     */
    void setHeaderValue(String name, String value);

    /**
     * sets a single number header value.
     * <p>
     * @param name
     * @param number
     */
    void setHeaderValue(String name, Number number);

    /**
     * sets a single date header value.
     * <p>
     * @param name
     * @param date
     */
    void setHeaderValue(String name, Date date);
}
