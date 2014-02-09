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
package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Resources;
import java.util.Map;
import javax.enterprise.inject.spi.BeanManager;

/**
 *
 * @param <T>
 * @author rhk
 */
public class ResourcesWithParametersImpl<T> extends ResourcesImpl<T> {

    public ResourcesWithParametersImpl(BeanManager bm,
            Class<T> resourceClass, Object body, int expectedStatus,
            Map<String, String> pathParams, Map<String, String[]> queryParams) {
        super(bm, resourceClass, body, expectedStatus, pathParams, queryParams);
    }

    @Override
    public Resources<T> withPathParam(String paramName, String paramValue) {
        this.pathParams.put(paramName, paramValue);
        return this;
    }

    @Override
    public Resources<T> withQueryParam(String paramName, String... paramValue) {
        if (this.queryParams.containsKey(paramName)) {
            String[] values = this.queryParams.get(paramName);
            String[] newValues = new String[values.length + paramValue.length];
            System.arraycopy(values, 0, newValues, 0, values.length);
            System.arraycopy(paramValue, 0, newValues, values.length,
                    paramValue.length);
        } else {
            this.queryParams.put(paramName, paramValue);
        }
        return this;
    }
}
