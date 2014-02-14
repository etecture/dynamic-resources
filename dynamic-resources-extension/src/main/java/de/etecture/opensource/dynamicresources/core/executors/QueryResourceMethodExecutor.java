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

import de.etecture.opensource.dynamicrepositories.executor.QueryExecutionException;
import de.etecture.opensource.dynamicrepositories.extension.DefaultQueryExecutionContext;
import de.etecture.opensource.dynamicrepositories.extension.QueryExecutors;
import de.etecture.opensource.dynamicrepositories.metadata.QueryDefinition;
import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.herschke.converters.api.Converters;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class QueryResourceMethodExecutor implements ResourceMethodExecutor {

    @Inject
    QueryExecutors executors;
    @Inject
    Converters converters;
    private final QueryDefinition query;

    public QueryResourceMethodExecutor(QueryDefinition query) {
        this.query = query;
    }

    @Override
    public <R, B> Response<R> execute(ExecutionContext<R, B> context) throws
            ResourceException {
        // build the query-execution-context (from repository)
        DefaultQueryExecutionContext<R> queryContext =
                new DefaultQueryExecutionContext(context
                .getResponseMetadata()
                .getResponseType(), context.getResponseMetadata()
                .getResponseType(), query);

        // add all the parameters as query-parameters.
        for (String paramName : context.getParameterNames()) {
            queryContext.addParameter(paramName, context.getParameterValue(
                    paramName));
        }

        // add the request as a query-parameter
        if (context.getBody() != null) {
            queryContext.addParameter("request", context.getBody());
        }
        try {
            // executes the query.
            return new DefaultResponse(executors.execute(queryContext), context
                    .getResponseMetadata()
                    .getStatusCode());
        } catch (QueryExecutionException ex) {
            // return another response for this exception
            return new DefaultResponse(context.getResponseMetadata()
                    .getResponseType(), ex);
        }
    }
}
