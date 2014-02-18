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
package de.etecture.opensource.dynamicresources.core.executors;

import de.etecture.opensource.dynamicresources.annotations.Application;
import de.etecture.opensource.dynamicresources.annotations.Failed;
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Resource;
import de.etecture.opensource.dynamicresources.annotations.Succeed;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.events.AfterExecutionEvent;
import de.etecture.opensource.dynamicresources.api.events.BeforeExecutionEvent;
import de.etecture.opensource.dynamicresources.utils.ApplicationLiteral;
import de.etecture.opensource.dynamicresources.utils.MethodLiteral;
import de.etecture.opensource.dynamicresources.utils.ResourceLiteral;
import javax.enterprise.event.Event;
import javax.enterprise.inject.New;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public abstract class AbstractResourceMethodExecutor implements
        ResourceMethodExecutor {

    @Inject
    @New
    BeforeExecutionEventBean beforeEvent;
    @Inject
    @New
    AfterExecutionEventBean afterEvent;
    @Inject
    Event<BeforeExecutionEvent> beforeEvents;
    @Inject
    @Succeed
    Event<AfterExecutionEvent> afterSuccessEvents;
    @Inject
    @Failed
    Event<AfterExecutionEvent> afterFailedEvents;

    protected abstract <R, B> R getEntity(ExecutionContext<R, B> context) throws
            Exception;

    @Override
    public <R, B> Response<R> execute(
            ExecutionContext<R, B> context) throws ResourceException {
        // build the literals for the event selection
        Application application = new ApplicationLiteral(context
                .getResourceMethod().getResource().getApplication().getName(),
                context.getResourceMethod().getResource().getApplication()
                .getBase());
        Resource resource = new ResourceLiteral(
                context.getResourceMethod().getResource());
        Method method = new MethodLiteral(context.getResourceMethod().getName());
        // init the event
        beforeEvent.init(context);
        // select and fire the before event.
        beforeEvents.select(application, resource, method).fire(beforeEvent);
        // check if canceled
        if (beforeEvent.wasCanceled()) {
            // canceled, so return immediately
            return (Response<R>) beforeEvent.getCancelingResponse();
        } else {
            // not canceled, so get the entity.
            Object originalEntity;
            boolean succeed;
            try {
                originalEntity = getEntity(context);
                succeed = true;
            } catch (Exception ex) {
                originalEntity = ex;
                succeed = false;
            }
            // init the after event.
            afterEvent.init(context, originalEntity);
            // select and fire the event.
            if (succeed) {
                afterSuccessEvents.select(application, resource, method).fire(
                        afterEvent);
            } else {
                afterFailedEvents.select(application, resource, method).fire(
                        afterEvent);
            }
            // the after event is the response, so return it.
            return (Response<R>) afterEvent;
        }

    }
}
