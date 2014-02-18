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
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.accesspoints.ApplicationAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.Applications;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodAccessor;
import de.etecture.opensource.dynamicresources.core.mapping.RequestReaders;
import de.etecture.opensource.dynamicresources.core.mapping.ResponseWriters;
import de.etecture.opensource.dynamicresources.core.mapping.mime.MediaTypeExpression;
import de.etecture.opensource.dynamicresources.metadata.ApplicationNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotAllowedException;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.metadata.RequestTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodRequest;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
public class DynamicResourcesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    /**
     * the name of the init-parameter 'application-name'.
     */
    public static final String APPLICATION_NAME = "application-name";
    /**
     * resolves a writer for a given type, mediatype and version.
     */
    @Inject
    ResponseWriters responseWriters;
    /**
     * resolves a reader for a given type, mediatype and version.
     */
    @Inject
    RequestReaders requestReaders;
    /**
     * these are all the applications found by scanning.
     */
    @Inject
    Applications applications;
    /**
     * these are the resources of the application. they are resolved in the
     * {@link HttpServlet#init()} method for the application specified by an
     * init-parameter 'application-name' in the {@link ServletConfig} of this
     * servlet.
     */
    private ApplicationAccessor resources;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // get the Application-Name from the ServletConfig.
        String applicationName = config.getInitParameter(APPLICATION_NAME);
        if (StringUtils.isBlank(applicationName)) {
            throw new ServletException("when using the " + getClass()
                    .getSimpleName() + ", an init-parameter with name '"
                    + APPLICATION_NAME + "' must be specified!");
        } else {
            try {
                // select the desired application by name.
                this.resources = applications.selectByName(applicationName);
            } catch (ApplicationNotFoundException ex) {
                throw new ServletException(
                        "tries to register a servlet for an application that does not exists!",
                        ex);
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        log(String.format("handle: %S %s in application: %s", req.getMethod(),
                req.getRequestURI(), resources.getMetadata().getName()));
        resp.setCharacterEncoding("UTF-8");
        try {
            executeResource(req, resp);
        } catch (ResourceNotFoundException | ApplicationNotFoundException ex) {
            log(String.format("resource or application not found for: %s",
                    req.getRequestURI()), ex);
            resp.sendError(StatusCodes.NOT_FOUND, ex.getMessage());
        } catch (ResourceMethodNotFoundException ex) {
            log(String.format("resource method: %S not found for: %s",
                    req.getMethod(), req.getRequestURI()), ex);
            resp.sendError(StatusCodes.METHOD_NOT_ALLOWED, ex.getMessage());
        } catch (RequestTypeNotSupportedException | MediaTypeNotAllowedException ex) {
            log(String.format("resource method: %S not available for: %s",
                    req.getMethod(), req.getRequestURI()), ex);
            resp.sendError(StatusCodes.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
        } catch (ResponseTypeNotSupportedException |
                MediaTypeNotSupportedException ex) {
            log(String.format("resource method: %S not acceptable for: %s",
                    req.getMethod(), req.getRequestURI()), ex);
            resp.sendError(StatusCodes.NOT_ACCEPTABLE, ex.getMessage());
        } catch (ResourceException ex) {
            log(String.format("resource method: %S for: %s was in error",
                    req.getMethod(), req.getRequestURI()), ex);
            resp.sendError(StatusCodes.UNPROCESSABLE_ENTITY, ex.getMessage());
        }
        resp.getWriter().flush();
        resp.getWriter().close();
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
        // get the contentType
        final MediaType contentType = getContentType(req);

        // get the acceptedType
        final MediaType acceptedType = getAcceptedType(req);

        // get the path of the request
        final String path = StringUtils.removeStart(req.getRequestURI(), req
                .getContextPath());

        // get the method of the request
        final String methodName = req.getMethod();

        log(String.format(
                "search a resource with path: %s with method: %S that produces: %s and consumes: %s",
                path, methodName, acceptedType, contentType));

        // select the accesspoint for the given uri and method.
        MethodAccessor<?> responses =
                resources.selectByPathAndMime(path, methodName, acceptedType);

        // get the request that is responsible for this contentType
        ResourceMethodRequest requestMeta = responses.getMetadata().getMethod()
                .getRequest(contentType);

        log(String.format("found resource: %s to handle: %S %s", responses
                .getMetadata().getMethod().getResource().getName(), responses
                .getMetadata().getMethod().getName(), path));

        // add the query parameters
        for (Entry<String, String[]> e : req.getParameterMap().entrySet()) {
            responses = responses.queryParam(e.getKey(), (Object[]) e
                    .getValue());
        }

        log(String.format("read request with type: %s and mimes: %s",
                requestMeta.getRequestType().getSimpleName(), contentType));
        // read the request body
        Object body = requestReaders
                .read(requestMeta.getRequestType(), contentType, req.getReader());
        responses = responses.body(body);

        // invoke the resource method
        log(String.format("invoke the resource: %s with method: %S",
                responses.getMetadata().getMethod().getResource().getName(),
                responses.getMetadata().getMethod().getName()));
        Response<?> response = responses.invoke();
        Object entity;
        try {
            entity = response.getEntity();
            log(String.format("got entity: %s for: %S %s", entity, responses
                    .getMetadata().getMethod().getName(), responses
                    .getMetadata().getMethod().getResource().getName()));
        } catch (ResponseException ex) {
            entity = ex.getCause();
            log(String.format("got exception: %s for: %S %s", entity, responses
                    .getMetadata().getMethod().getName(), responses
                    .getMetadata().getMethod().getResource().getName()));
        }

        // add the headers from the resonse object
        for (Entry<String, List<Object>> e : response.getHeaders()) {
            for (Object v : e.getValue()) {
                // @TODO: choose the correct method to add header (int, date...)
                resp.addHeader(e.getKey(), v.toString());
            }
        }

        // write the response.
        if (entity != null) {
            log(String.format("write response with type: %s and mimes: %s",
                    entity.getClass().getSimpleName(), acceptedType));
            resp.setContentLength(responseWriters.getContentLength(entity,
                    acceptedType));
            responseWriters.write(entity, acceptedType, resp.getWriter());
        }
    }

    private static MediaType getAcceptedType(HttpServletRequest req) {
        String acceptType = req.getHeader("Accept");
        if (StringUtils.isBlank(acceptType)) {
            acceptType = "*/*";
        }
        return new MediaTypeExpression(acceptType);
    }

    private static MediaType getContentType(HttpServletRequest req) {
        String contentType = req.getHeader("Content-Type");
        if (StringUtils.isBlank(contentType)) {
            contentType = "*/*";
        }
        return new MediaTypeExpression(contentType);
    }
}
