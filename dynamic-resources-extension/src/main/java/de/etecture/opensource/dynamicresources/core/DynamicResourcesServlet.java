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
package de.etecture.opensource.dynamicresources.core;

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ResponseException;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceMethodAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.Resources;
import de.etecture.opensource.dynamicresources.extension.MediaTypeExpression;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import de.etecture.opensource.dynamicresources.extension.VersionNumberRangeExpression;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 * implements a {@link HttpServlet} that maps all {@link HttpServletRequest} to
 * the {@link Resource} for the given request-uri and the {@link ResourceMethod}
 * for the given request-method.
 *
 * @author rhk
 * @version
 * @since
 */
@WebServlet(urlPatterns = "/*")
public class DynamicResourcesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    /**
     * an access point for all resources.
     */
    @Inject
    Resources resources;
    /**
     * resolves a writer for a given type, mediatype and version.
     */
    @Inject
    ResponseWriterResolver responseWriters;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            resp.setCharacterEncoding("UTF-8");
            executeResource(req, resp);
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (ResourceException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        service(req, resp);
    }

    /**
     * executes the resource.
     *
     * @param req
     * @param resp
     * @throws ResourceException
     * @throws IOException
     */
    private void executeResource(HttpServletRequest req,
            HttpServletResponse resp) throws ResourceException, IOException {
        // select the accesspoint for the given uri and method.
        ResourceMethodAccessor<?> methodAccessor =
                resources.select(req.getRequestURI(), req.getMethod());

        // get the contentType
        final MediaType contentType = getContentType(req);

        // get the request that is responsible for this contentType
        ResourceMethodRequest requestMeta = methodAccessor.getMetadata()
                .getRequest(contentType);

        // get the acceptedType
        final MediaType acceptedType = getAcceptedType(req);

        // get the response that is responsible for this acceptedType
        ResourceMethodResponse responseMeta = requestMeta.getResponse(
                acceptedType);

        // add the query parameters
        for (Entry<String, String[]> e : req.getParameterMap().entrySet()) {
            methodAccessor = methodAccessor.queryParam(e.getKey(), (Object[]) e
                    .getValue());
        }

        // read the request body
        methodAccessor = methodAccessor
                .body(requestMeta.getRequestReader().processRequest(req
                .getReader(),
                contentType.toString()));

        // invoke the resource method
        Response<?> response = methodAccessor.invoke();
        Object entity;
        try {
            entity = response.getEntity();
        } catch (ResponseException ex) {
            entity = ex.getCause();
        }

        // add the response-headers
        for (Entry<String, List<Object>> e : response.getHeaders()) {
            for (Object v : e.getValue()) {
                resp.addHeader(e.getKey(), v.toString());
            }
        }

        // write the response.
        if (entity != null) {
            final ResponseWriter responseWriter =
                    responseWriters.resolve(entity.getClass(), acceptedType,
                    new VersionNumberRangeExpression(acceptedType.version()));
            resp.setContentLength(responseWriter.getContentLength(entity,
                    acceptedType));
            responseWriter
                    .processElement(entity, resp.getWriter(), acceptedType);
        }
    }

    private static MediaType getAcceptedType(HttpServletRequest req) {
        String acceptType = req.getHeader("Accept");
        if (StringUtils.isBlank(acceptType)) {
            acceptType = "application/xml";
        }
        return new MediaTypeExpression(acceptType);
    }

    private static MediaType getContentType(HttpServletRequest req) {
        String contentType = req.getHeader("Content-Type");
        if (StringUtils.isBlank(contentType)) {
            contentType = "application/xml";
        }
        return new MediaTypeExpression(contentType);
    }
}
