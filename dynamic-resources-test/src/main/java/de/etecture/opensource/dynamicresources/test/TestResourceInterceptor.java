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
package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicresources.api.Global;
import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import java.util.Map;

/**
 *
 * @author rhk
 */
@Global
public class TestResourceInterceptor implements
        ResourceInterceptor {

    @Override
    public <T> Response<T> before(Request<T> request) {
        System.out.printf("BEFORE: %s %s for: %s%n", request
                .getMethodName(),
                request.getResource().uri(), request.getResourceClass()
                .getSimpleName());
        for (Map.Entry<String, String> entry : request.getPathParameter()
                .entrySet()) {
            System.out.printf("\t%s = %s%n", entry.getKey(), entry.getValue());
        }
        return null;
    }

    @Override
    public <T> Response<T> after(Request<T> request,
            Response<T> response) {
        System.out.printf("AFTER: %s %s for: %s%n", request
                .getMethodName(),
                request.getResource().uri(), request.getResourceClass()
                .getSimpleName());
        for (Map.Entry<String, String> entry : request.getPathParameter()
                .entrySet()) {
            System.out.printf("\t%s = %s%n", entry.getKey(), entry.getValue());
        }
        System.out.printf("\tStatus : %d%n", response.getStatus());
        try {
            System.out.printf("\tEntity : %s%n", response.getEntity());
        } catch (Exception ex) {
            System.out.printf("\tException : %s%n", ex);
        }
        return response;
    }
}
