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

import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.UriBuilder;
import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
@WebServlet(urlPatterns = "/*")
public class DynamicRestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    @Inject
    DynamicResourcesExtension resext;
    @Inject
    @Any
    Instance<ResourceMethodHandler> resourceMethodHandlers;
    @Inject
    @Default
    UriBuilder uriBuilder;
    @Inject
    Event<HttpServletResponse> responseEvents;
    @Inject
    Event<HttpServletRequest> requestEvents;
    @Inject
    ResponseWriterResolver responseWriterResolver;
    @Inject
    RequestReaderResolver requestReaderResolver;

    private static String stripLastSlashIfExist(String whatever) {
        while (whatever.endsWith("/")) {
            whatever = whatever.substring(0, whatever.length() - 1);
        }
        return whatever;
    }

    private void executeResource(final HttpServletRequest req,
            HttpServletResponse resp) throws
            IOException {
        for (Class<?> clazz : resext.resourcesInterfaces) {
            Resource resource = clazz.getAnnotation(Resource.class);
            Map<String, String> groups = new HashMap<>();
            String uri = resource.uri();
            if (!uri.startsWith("/")) {
                uri = "/" + uri;
            }
            if (PathParser.match(uri, stripLastSlashIfExist(req
                    .getPathInfo()), groups)) {
                // find the ResourceMethodHandler for the method
                Instance<ResourceMethodHandler> selectedResourceMethodHandlers =
                        resourceMethodHandlers.select(new VerbLiteral(req
                        .getMethod()));
                try {
                    Request request = new DefaultRequest(req, groups, clazz,
                            requestReaderResolver);
                    for (ResourceMethodHandler handler
                            : selectedResourceMethodHandlers) {
                        if (handler.isAvailable(clazz)) {
                            Response response = handler.handleRequest(request);
                            if (response != null) {
                                writeResponse(request, resp, response);
                                return;
                            }
                        }
                    }
                    throw new IllegalArgumentException(
                            "no resource-method-handler defined for this method");
                } catch (IllegalArgumentException ex) {
                    resp.sendError(StatusCodes.METHOD_NOT_ALLOWED,
                            "no such method: " + req.getMethod()
                            + " for resource: " + clazz.getSimpleName()
                            + " reason: " + ex.getMessage());
                    return;
                }
            }
        }
        if ("OPTIONS".equalsIgnoreCase(req.getMethod()) && ("/".equals(req
                .getPathInfo()) || StringUtils.isEmpty(req.getPathInfo()))) {
            resp.setStatus(StatusCodes.NOT_FOUND);
            resp.setContentType("text/plain");
            final PrintWriter writer = resp.getWriter();

            writer.println("Available Resources");
            writer.println(StringUtils.repeat("-", 19));
            writer.println();

            for (Class<?> clazz : resext.resourcesInterfaces) {
                writer.printf("\t%s --> %s%n", clazz.getSimpleName(), uriBuilder
                        .build(clazz, null));
            }
            writer.println();

        } else {
            resp.sendError(StatusCodes.NOT_FOUND, String.format(
                    "There is no resource matching the path %s",
                    req.getPathInfo()));
        }
    }

    protected void writeResponse(Request request,
            HttpServletResponse resp, Response<?> response) throws IOException {
        for (Map.Entry<String, List<Object>> e : response.getHeaders()) {
            for (Object o : e.getValue()) {
                if (o == null) {
                    // do nothing here
                } else if (Number.class.isAssignableFrom(o.getClass())) {
                    resp.addIntHeader(e.getKey(), ((Number) o).intValue());
                } else if (Date.class.isAssignableFrom(o.getClass())) {
                    resp.addDateHeader(e.getKey(), ((Date) o).getTime());
                } else {
                    resp.addHeader(e.getKey(), o.toString());
                }
            }
        }
        resp.setContentType(request.getAcceptedMediaType().toString());
        resp.setStatus(response.getStatus());
        if (response.getEntity() != null) {
            ResponseWriter writer =
                    responseWriterResolver.resolve(response
                    .getEntity().getClass(), request.getAcceptedMediaType(),
                    request.getAcceptedVersionRange());
            if (writer != null) {
                final int contentLength = writer.getContentLength(response
                        .getEntity(), request.getAcceptedMediaType());
                if (contentLength >= 0) {
                    resp.setContentLength(contentLength);
                }
                final PrintWriter respWriter = resp.getWriter();
                writer.processElement(response.getEntity(), respWriter,
                        request.getAcceptedMediaType());
                respWriter.flush();
            } else {
                resp.sendError(406, String.format(
                        "The resource is not available with mediatype: %s and version: %s",
                        request.getAcceptedMediaType().toString(), request
                        .getAcceptedVersionRange().toString()));
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        requestEvents.fire(req);
        responseEvents.fire(resp);
        resp.setCharacterEncoding("UTF-8");
        executeResource(req, resp);
        resp.getWriter().flush();
        resp.getWriter().close();
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("do the head");
        service(req, resp);
    }
}
