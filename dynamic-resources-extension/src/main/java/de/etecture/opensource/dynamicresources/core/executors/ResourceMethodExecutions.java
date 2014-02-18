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
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Resource;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.FilterValueGenerator;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodFilter;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import de.etecture.opensource.dynamicresources.utils.ApplicationLiteral;
import de.etecture.opensource.dynamicresources.utils.MethodLiteral;
import de.etecture.opensource.dynamicresources.utils.ResourceLiteral;
import java.util.Map;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;
import javax.enterprise.util.AnnotationLiteral;
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
    Instance<ResourceMethodExecutor> allExecutors;
    @Inject
    Instance<FilterValueGenerator> generators;

    public <R, B> Response<R> execute(ResourceMethodResponse<R> responseMetadata,
            ResourceMethodRequest<B> requestMetadata, B body,
            Map<String, Object> parameters) throws ResourceException {
        // build the execution context
        ExecutionContext<R, B> context = buildExecutionContext(responseMetadata,
                requestMetadata, body, parameters);

        // process the filters for the resource method
        for (ResourceMethodFilter<?> f : responseMetadata.getMethod()
                .getFilters()) {
            // create the generator.
            context.setParameterValue(f.getName(), generators.select(f
                    .getValueGenerator(), new AnnotationLiteral<New>() {
                private static final long serialVersionUID =
                        1L;
            }).get().generate(f, context));
        }

        // resolve the executor
        ResourceMethodExecutor executor = resolve(context);

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

    private <R, B> ResourceMethodExecutor resolve(ExecutionContext<R, B> context) {
        // build the literals
        Application application = new ApplicationLiteral(context
                .getResourceMethod().getResource().getApplication().getName(),
                context.getResourceMethod().getResource().getApplication()
                .getBase());
        Resource resource = new ResourceLiteral(context.getResourceMethod()
                .getResource());
        Method method = new MethodLiteral(context.getResourceMethod().getName());
        return allExecutors.select(application, resource, method).get();
    }
}
