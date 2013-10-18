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

import de.etecture.opensource.dynamicrepositories.api.Param;
import de.etecture.opensource.dynamicrepositories.api.Params;
import de.etecture.opensource.dynamicrepositories.extension.TechnologyLiteral;
import de.etecture.opensource.dynamicrepositories.spi.QueryExecutor;
import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import de.etecture.opensource.dynamicresources.api.DELETE;
import de.etecture.opensource.dynamicresources.api.ExceptionHandler;
import de.etecture.opensource.dynamicresources.api.GET;
import de.etecture.opensource.dynamicresources.api.POST;
import de.etecture.opensource.dynamicresources.api.PUT;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.Response;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 *
 * @author rhk
 */
@ApplicationScoped
public class DynamicResourceExecutor {

    @Inject
    @Any
    Instance<QueryExecutor> queryExecutors;
    @Inject
    @Any
    Instance<ExceptionHandler> exceptionHandlers;

    private QueryExecutor getExecutorByTechnology(String technology) {
        return queryExecutors.select(new TechnologyLiteral(technology)).get();
    }

    public String getOptions(Resource resource, Class<?> resourceClazz) {
        return "options";
    }

    public String getInformation(Resource resource, Class<?> resourceClazz) {
        return resourceClazz.getSimpleName();
    }

    public <T> Response<?> GET(Resource resource, Class<T> resourceClazz,
            Map<String, Object> values) {
        if (resourceClazz.isAnnotationPresent(GET.class)) {
            GET get = resourceClazz.getAnnotation(GET.class);
            DefaultQueryMetaData<T> queryMetaData = new DefaultQueryMetaData(
                    resourceClazz, QueryMetaData.Kind.RETRIEVE, get.query(),
                    null, values);
            return execute(resourceClazz, queryMetaData, "GET", get.technology(),
                    get
                    .status());
        } else {
            return new Response("cannot find GET method for this resource.", 405);
        }
    }

    public <T> Response<?> PUT(Resource resource, Class<T> resourceClazz,
            Map<String, Object> values, Object content) {
        if (resourceClazz.isAnnotationPresent(PUT.class)) {
            PUT put = resourceClazz.getAnnotation(PUT.class);
            DefaultQueryMetaData<T> queryMetaData = new DefaultQueryMetaData(
                    resourceClazz, QueryMetaData.Kind.UPDATE, put.query(),
                    null, values);

            return execute(resourceClazz, queryMetaData, "PUT", put.technology(),
                    put
                    .status());
        } else {
            return new Response("cannot find PUT method for this resource.", 405);
        }
    }

    public <T> Response<?> POST(Resource resource, Class<T> resourceClazz,
            Map<String, Object> values, Object content) {
        if (resourceClazz.isAnnotationPresent(POST.class)) {
            POST post = resourceClazz.getAnnotation(POST.class);
            DefaultQueryMetaData<T> queryMetaData = new DefaultQueryMetaData(
                    resourceClazz, QueryMetaData.Kind.CREATE, post.query(),
                    null, values);
            return execute(resourceClazz, queryMetaData, "POST", post
                    .technology(), post
                    .status());
        } else {
            return new Response("cannot find POST method for this resource.",
                    405);
        }
    }

    public <T> Response<?> DELETE(Resource resource, Class<T> resourceClazz,
            Map<String, Object> values) {
        if (resourceClazz.isAnnotationPresent(DELETE.class)) {
            DELETE delete = resourceClazz.getAnnotation(DELETE.class);
            DefaultQueryMetaData<T> queryMetaData = new DefaultQueryMetaData(
                    resourceClazz, QueryMetaData.Kind.DELETE, delete.query(),
                    null, values);
            execute(resourceClazz, queryMetaData, "DELETE", delete.technology(),
                    delete
                    .status());
            return new Response(null, delete.status());
        } else {
            return new Response("cannot find DELETE method for this resource.",
                    405);
        }
    }

    private <T> Response<?> execute(
            Class<T> resourceClazz, DefaultQueryMetaData<T> queryMetaData,
            String method, String technology, int status) {
        if (resourceClazz.isAnnotationPresent(Param.class)) {
            Param param = resourceClazz.getAnnotation(Param.class);
            queryMetaData.addParameter(param);
        } else if (resourceClazz.isAnnotationPresent(Params.class)) {
            for (Param param : resourceClazz.getAnnotation(Params.class)
                    .value()) {
                queryMetaData.addParameter(param);
            }
        }
        try {
            return new Response(getExecutorByTechnology(technology).execute(
                    queryMetaData), status);
        } catch (Throwable ex) {
            return handleException(ex, resourceClazz, method);
        }
    }

    private <T> Response<?> handleException(Throwable exception,
            Class<T> resourceClazz, String method) {
        System.out.printf("handle Exception: %s, resource: %s, method: %s%n",
                exception.getClass().getSimpleName(),
                resourceClazz.getSimpleName(),
                method);
        for (ExceptionHandler exh : exceptionHandlers) {
            System.out.printf("check handler: %s%n", exh.getClass()
                    .getSimpleName());
            if (exh
                    .isResponsibleFor(resourceClazz, method, exception
                    .getClass())) {
                System.out.printf("call exception handler: %s%n", exh.getClass()
                        .getSimpleName());
                return exh.handleException(resourceClazz, method, exception);
            }
        }
        return new Response(exception, 500);
    }
}
