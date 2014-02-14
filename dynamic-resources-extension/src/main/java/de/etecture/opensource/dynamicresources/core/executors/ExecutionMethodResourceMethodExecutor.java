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

import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.enterprise.inject.spi.AnnotatedParameter;

/**
 * executes an {@link ExecutionMethod}
 *
 * @author rhk
 * @version
 * @since
 */
public class ExecutionMethodResourceMethodExecutor<T> implements
        ResourceMethodExecutor {

    private final ExecutionMethod<T> method;

    public ExecutionMethodResourceMethodExecutor(ExecutionMethod<T> method) {
        this.method = method;
    }

    @Override
    public <R, B> Response<R> execute(
            ExecutionContext<R, B> context) throws ResourceException {
        // assemble the arguments...
        Object[] arguments = new Object[method.getParameters().size()];
        if (method.getExecutionContextArgument() != null) {
            arguments[method.getExecutionContextArgument().getPosition()] =
                    context;
        }
        if (method.getBodyArgument() != null) {
            arguments[method.getBodyArgument().getPosition()] = context
                    .getBody();
        }
        for (Map.Entry<AnnotatedParameter<T>, String> e : method
                .getParameterArguments().entrySet()) {
            arguments[e.getKey().getPosition()] = context.getParameterValue(e
                    .getValue());
        }
        // invoke the method now.
        if (!method.getJavaMember().isAccessible()) {
            method.getJavaMember().setAccessible(true);
        }
        Object bean = createBean();
        try {
            Object result = method.getJavaMember().invoke(bean, arguments);
            if (Response.class.isInstance(result)) {
                return Response.class.cast(result);
            } else {
                return new DefaultResponse((R) result, context
                        .getResponseMetadata().getStatusCode());
            }
        } catch (IllegalAccessException | IllegalArgumentException |
                InvocationTargetException ex) {
            throw new ResourceException("cannot call the execution method!", ex);
        } finally {
            disposeBean(bean);
        }
    }

    private Object createBean() {
        // TODO: create bean.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void disposeBean(Object bean) {
        // TODO: destroy bean.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
