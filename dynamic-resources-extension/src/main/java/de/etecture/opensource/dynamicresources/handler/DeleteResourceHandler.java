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
package de.etecture.opensource.dynamicresources.handler;

import de.etecture.opensource.dynamicrepositories.executor.QueryHints;
import de.etecture.opensource.dynamicrepositories.extension.DefaultQuery;
import de.etecture.opensource.dynamicresources.api.BooleanResult;
import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.etecture.opensource.dynamicresources.api.OldRequest;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.spi.AbstractResourceMethodHandler;
import de.etecture.opensource.dynamicresources.annotations.accessing.Verb;

/**
 *
 * @author rhk
 */
@Verb(HttpMethods.DELETE)
public class DeleteResourceHandler extends AbstractResourceMethodHandler {

    @Override
    public <T> Response<T> handleRequest(OldRequest<T> request) throws
            ResourceException {
        try {
            final DefaultQuery<BooleanResult> query =
                    buildQuery(request, BooleanResult.class);
            query.addHint(QueryHints.LIMIT, 1);
            BooleanResult result = (BooleanResult) executors.execute(query);
            return new DefaultResponse(result.getResult(),
                    request.getResourceMethod().status());
        } catch (Exception ex) {
            return afterFailure(request, null, ex);
        }
    }
}
