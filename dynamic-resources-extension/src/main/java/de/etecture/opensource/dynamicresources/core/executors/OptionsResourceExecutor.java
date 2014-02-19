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
import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.HttpHeaders;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class OptionsResourceExecutor {

    @Executes(
            resource = ".*(?<!Root)$",
            method = HttpMethods.OPTIONS,
            responseType = Resource.class)
    public Response<Resource> createOptions(ExecutionContext<?, ?> context) {
        DefaultResponse<Resource> response = new DefaultResponse(context
                .getResourceMethod().getResource(),
                context.getResponseMetadata().getStatusCode());
        final Set<String> methodNames =
                context.getResourceMethod().getResource().getMethods().keySet();
        response.addHeader(HttpHeaders.ALLOW, Arrays.toString(methodNames
                .toArray(new String[methodNames.size()])));
        return response;
    }

    @Executes(
            resource = "^.*Root$",
            method = HttpMethods.OPTIONS,
            responseType = Application.class)
    public Application createApplicationOptions(ExecutionContext<?, ?> context) {
        return context.getResourceMethod().getResource().getApplication();
    }
}
