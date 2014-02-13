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
package de.etecture.opensource.dynamicresources.metadata.annotated;

import de.etecture.opensource.dynamicrepositories.metadata.AnnotatedQueryDefinition;
import de.etecture.opensource.dynamicrepositories.metadata.DefaultQueryDefinition;
import de.etecture.opensource.dynamicrepositories.metadata.QueryDefinition;
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.contexts.ExecutionContext;
import de.etecture.opensource.dynamicresources.contexts.QueryExecutionContext;
import de.etecture.opensource.dynamicresources.metadata.AbstractResourceMethodResponse;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class AnnotatedResourceMethodResponse<R> extends AbstractResourceMethodResponse<R> {

    private ExecutionContext<R> executionContext;

    AnnotatedResourceMethodResponse(
            ResourceMethod method,
            Class<R> resourceClass, int statusCode, MediaType... mediaTypes) {
        super(method, resourceClass, statusCode, mediaTypes);
    }

    @Override
    public <EC extends ExecutionContext<R>> EC getExecutionContext() {
        return (EC) executionContext;
    }

    public static <R> AnnotatedResourceMethodResponse<R> createWithQueryExecution(
            final ResourceMethod method,
            final Class<R> resourceClass, Method annotation,
            MediaType... mediaTypes) {
        AnnotatedResourceMethodResponse<R> response =
                new AnnotatedResourceMethodResponse(method, resourceClass,
                annotation.status(), mediaTypes);
        if (annotation.query().length > 0) {
            QueryDefinition queryDefinition = new AnnotatedQueryDefinition(
                    annotation.query()[0]) {
                @Override
                public String getStatement() {
                    return createStatement(resourceClass, method.getName(),
                            super.getStatement());
                }
            };
            response.executionContext = new QueryExecutionContext(response,
                    queryDefinition);
        } else {
            response.executionContext = new QueryExecutionContext(response,
                    new DefaultQueryDefinition(createStatement(resourceClass,
                    method.getName(), "")));
        }
        return response;
    }

    protected static String createStatement(Class<?> resourceClass,
            String methodName, String statement) {
        if (statement == null || statement.trim().isEmpty()) {
            statement = methodName;
        }
        try {
            return ResourceBundle.getBundle(resourceClass.getName()).getString(
                    statement);
        } catch (MissingResourceException e) {
            return statement;
        }
    }
}
