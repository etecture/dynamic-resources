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

import de.etecture.opensource.dynamicresources.annotations.Executes;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import de.etecture.opensource.dynamicresources.utils.AnyLiteral;
import de.etecture.opensource.dynamicresources.utils.ExecutesLiteral;
import de.etecture.opensource.dynamicresources.utils.InjectionPointHelper;
import java.lang.annotation.Annotation;
import java.util.Map;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * this is the base class that is responsible to executes any resource method
 * executions.
 * <p>
 * it builds the execution context, selects the appropriate
 * {@link ResourceMethodExecutor} and delegates the invocation to the selected
 * ResourceMethodExecutor.
 *
 * @author rhk
 * @version
 * @since
 */
@Default
public class ResourceMethodExecutions {

    @Inject
    BeanManager beanManager;
    @Inject
    @Default
    ResourceMethodExecutor defaultExecutor;

    public <R, B> Response<R> execute(ResourceMethodResponse<R> responseMetadata,
            ResourceMethodRequest<B> requestMetadata, B body,
            Map<String, Object> parameters) throws ResourceException {
        // build the execution context
        ExecutionContext<R, B> context = buildExecutionContext(responseMetadata,
                requestMetadata, body, parameters);

        // resolve the executor
        ResourceMethodExecutor executor = resolve(ExecutesLiteral
                .create(context));

        // execute
        return executor.execute(context);
    }

    private <R, B> ExecutionContext<R, B> buildExecutionContext(
            ResourceMethodResponse<R> responseMetadata,
            ResourceMethodRequest<B> requestMetadata, B body,
            Map<String, Object> parameters) {
        return new ExecutionContext(
                responseMetadata,
                requestMetadata, body, parameters);
    }

    @Produces
    @Executes
    public ResourceMethodExecutor produceExecutorForLiteral(InjectionPoint ip) {
        // lookup the @Executes for this ip
        Executes executesToFind = InjectionPointHelper.findQualifier(
                Executes.class,
                ip);
        // resolve...
        return resolve(executesToFind);
    }

    private ResourceMethodExecutor resolve(Executes executesToFind) {
        // now iterate about all the executors-beans to resolve the one we need.
        for (Bean<?> bean : beanManager.getBeans(
                ResourceMethodExecutor.class, new AnyLiteral())) {
            // do only handle ResourceMethodExecutor beans.
            if (ResourceMethodExecutor.class.isAssignableFrom(bean
                    .getBeanClass())) {
                Bean<ResourceMethodExecutor> rmeBean =
                        (Bean<ResourceMethodExecutor>) bean;
                // check all the qualifiers
                for (Annotation annotation : rmeBean.getQualifiers()) {
                    if (Executes.class.isAssignableFrom(annotation
                            .annotationType())) {
                        // it is an @Executes, so compare it.
                        Executes executesOfBean = (Executes) annotation;
                        if (ExecutesLiteral.matches(executesToFind,
                                executesOfBean)) {
                            // found a match, so create it and return it.
                            return rmeBean.create(beanManager
                                    .createCreationalContext(rmeBean));
                        }

                    }
                }
            }
        }
        return defaultExecutor;
    }
}
