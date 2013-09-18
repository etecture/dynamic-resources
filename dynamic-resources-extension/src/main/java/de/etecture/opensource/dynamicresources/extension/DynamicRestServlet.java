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

import com.sun.jersey.server.impl.uri.PathTemplate;
import de.etecture.opensource.dynamicresources.api.ForEntity;
import de.etecture.opensource.dynamicresources.api.JSONWriter;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.XMLWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author rhk
 */
@WebServlet(urlPatterns = "/*")
public class DynamicRestServlet extends HttpServlet {

    private static final JsonGeneratorFactory JSON_FACTORY = Json
            .createGeneratorFactory(Collections.<String, Object>singletonMap(
            JsonGenerator.PRETTY_PRINTING, "true"));
    private static final XMLOutputFactory XML_FACTORY = XMLOutputFactory
            .newFactory();
    private static final long serialVersionUID = 1L;
    @Inject
    DynamicResourcesExtension resext;
    @Inject
    DynamicResourceExecutor executor;
    @Inject
    BeanManager beanManager;
    @Inject
    @Default
    JSONWriter defaultJSONWriter;
    @Inject
    @Default
    XMLWriter defaultXMLWriter;
    @Inject
    @Any
    Instance<ResourceInterceptor> resourceInterceptors;

    Response before(String method, Resource resource, Class<?> resourceClass,
            Map<String, Object> parameter) {
        Response response = null;
        for (ResourceInterceptor ri : resourceInterceptors) {
            response = ri.before(method, resource, resourceClass, parameter);
            if (response != null) {
                break;
            }
        }
        return response;
    }

    Response after(String method, Resource resource, Class<?> resourceClass,
            Map<String, Object> parameter, Response response) {
        for (ResourceInterceptor ri : resourceInterceptors) {
            response = ri.after(method, resource, resourceClass, parameter,
                    response);
        }
        return response;
    }

    private void executeResource(final HttpServletRequest req,
            HttpServletResponse resp) throws
            IOException {
        for (Class<?> clazz : resext.resourcesInterfaces) {
            Resource resource = clazz.getAnnotation(Resource.class);
            PathTemplate pt = new PathTemplate(resource.value());
            Map<String, String> groups = new HashMap<>();
            if (pt.match(req.getPathInfo(), groups)) {
                Map<String, Object> parameter = new HashMap<>();
                parameter.putAll(groups);
                for (String parameterName : Collections.list(req
                        .getParameterNames())) {
                    parameter
                            .put(parameterName, req.getParameter(parameterName));
                }
                Response response = before(req.getMethod(), resource, clazz,
                        parameter);
                if (response == null) {
                    switch (req.getMethod()) {
                        case "HEAD":
                            resp.setContentType("text/plain");
                            resp.setContentLength(-1);
                            resp.getWriter().println(executor.getInformation(
                                    resource, clazz));
                            return;
                        case "OPTIONS":
                            resp.setContentType("text/plain");
                            resp.getWriter().println(executor.getOptions(
                                    resource, clazz));
                            return;
                        case "DELETE":
                            response = executor.DELETE(resource, clazz,
                                    parameter);
                            break;
                        case "GET":
                            response = executor.GET(resource, clazz,
                                    parameter);
                            break;
                        case "PUT":
                            response = executor.PUT(resource, clazz,
                                    parameter, readContent(req));
                            break;
                        case "POST":
                            response = executor.POST(resource, clazz,
                                    parameter, readContent(req));
                            break;
                    }
                    response =
                            after(req.getMethod(), resource, clazz, parameter,
                            response);
                    if (response != null) {
                        writeResponse(resp, response, req);
                        return;
                    }
                }
            }
        }
        resp.sendError(404, String.format(
                "There is no resource matching the path %s",
                req.getPathInfo()));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        executeResource(req, resp);
        resp.getWriter().flush();
    }

    private Object readContent(HttpServletRequest req) {
        return null;
    }

    private JSONWriter findJSONWriterForClass(Class<?> clazz) {
        for (Bean<?> bean : beanManager.getBeans(JSONWriter.class,
                new AnnotationLiteral<Any>() {
        })) {
            for (Annotation qualifier : bean.getQualifiers()) {
                if (qualifier.annotationType() == ForEntity.class) {
                    if (((ForEntity) qualifier).value().isAssignableFrom(clazz)) {
                        final Bean<JSONWriter> typedBean =
                                (Bean<JSONWriter>) bean;
                        return typedBean.create(
                                beanManager.createCreationalContext(typedBean));
                    }
                }
            }
        }
        return defaultJSONWriter;
    }

    private XMLWriter findXMLWriterForClass(Class<?> clazz) {
        for (Bean<?> bean : beanManager.getBeans(XMLWriter.class,
                new AnnotationLiteral<Any>() {
        })) {
            for (Annotation qualifier : bean.getQualifiers()) {
                if (qualifier.annotationType() == ForEntity.class) {
                    if (((ForEntity) qualifier).value().isAssignableFrom(clazz)) {
                        final Bean<XMLWriter> typedBean =
                                (Bean<XMLWriter>) bean;
                        return typedBean.create(
                                beanManager.createCreationalContext(typedBean));
                    }
                }
            }
        }
        return defaultXMLWriter;
    }

    private void writeEntityAsJSON(HttpServletResponse resp, Response response)
            throws IOException {
        resp.setContentType("application/json");
        JsonGenerator jg = JSON_FACTORY
                .createGenerator(resp.getWriter());
        findJSONWriterForClass(response.getEntity().getClass())
                .process(response.getEntity(), jg, null);
        jg.flush();
        jg.close();
    }

    private void writeEntityAsXML(HttpServletResponse resp, Response response)
            throws IOException {
        resp.setContentType("application/xml");
        try {
            XMLStreamWriter xw = XML_FACTORY.createXMLStreamWriter(
                    resp.getWriter());
            xw.writeStartDocument("UTF-8", "1.0");
            findXMLWriterForClass(response.getEntity().getClass())
                    .process(response.getEntity(), xw, null);
            xw.writeEndDocument();
            xw.flush();
            xw.close();
        } catch (XMLStreamException ex) {
            throw new IOException("cannot produce xml: ", ex);
        }
    }

    private void writeResponse(HttpServletResponse resp, Response response,
            final HttpServletRequest req) throws IOException {
        resp.setStatus(response.getStatus());
        if (response.getEntity() != null) {
            if ("application/json".startsWith(req
                    .getHeader("Accept").trim()
                    .toLowerCase())) {
                writeEntityAsJSON(resp, response);
            } else if ("application/xml".startsWith(req
                    .getHeader(
                    "Accept")
                    .trim()
                    .toLowerCase())) {
                writeEntityAsXML(resp, response);
            } else if ("text/xml".startsWith(req.getHeader(
                    "Accept")
                    .trim()
                    .toLowerCase())) {
                writeEntityAsXML(resp, response);
            } else {
                resp.setContentType("text/plain");
                resp.getWriter().println(response
                        .getEntity());
            }
        }
    }
}
