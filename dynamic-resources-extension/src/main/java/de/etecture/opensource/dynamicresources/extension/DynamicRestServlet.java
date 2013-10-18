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
import de.etecture.opensource.dynamicresources.api.Entity;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.api.Version;
import de.etecture.opensource.dynamicresources.api.VersionNumberRange;
import de.etecture.opensource.dynamicresources.spi.VersionNumberResolver;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
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
    DynamicResourceExecutor executor;
    @Inject
    BeanManager beanManager;
    @Inject
    @Any
    Instance<ResourceInterceptor> resourceInterceptors;
    @Inject
    VersionNumberResolver versionNumberResolver;

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
                            try {
                                response = executor.PUT(resource, clazz,
                                        parameter, readContent(req));
                            } catch (Exception ex) {
                                response = new Response(ex, 415);
                            }
                            break;
                        case "POST":
                            try {
                                response = executor.POST(resource, clazz,
                                        parameter, readContent(req));
                            } catch (Exception ex) {
                                response = new Response(ex, 415);
                            }
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

    private <T> ResponseWriter<T> findResponseWriterForClassAndMimeTypeAndVersion(
            Class<T> clazz,
            MediaType mimeType,
            VersionNumberRange versionMatcher) {


        final Set<Bean<?>> beans = beanManager.getBeans(ResponseWriter.class,
                new AnnotationLiteral<Any>() {
                    private static final long serialVersionUID = 1L;
        });
        System.out.printf(
                "selected mimetype: %s, selected version: %s, beans: %s, entity: %s%n",
                mimeType,
                versionMatcher == null ? "not specified" : versionMatcher
                .toString(),
                beans.size(), clazz.getName());
        // (1) Build the map of versioned beans.
        TreeMap<Version, Bean<ResponseWriter<T>>> versionedBeans =
                new TreeMap<>(new VersionComparator(false));
        outer:
        for (Bean<?> bean : beans) {
            boolean foundEntity = false;
            // lookup the bean with the correct Entity
            // HINT: we cannot use select(Entity) here, due to we do not want to
            inner:
            for (Annotation qualifier : bean.getQualifiers()) {
                if (qualifier.annotationType() == Entity.class) {
                    if (!((Entity) qualifier).value().isAssignableFrom(clazz)) {
                        continue outer;
                    } else {
                        foundEntity = true;
                        break inner;
                    }
                }
            }
            if (foundEntity) {
                // only, if bean is an entity of the given type.
                for (Annotation qualifier : bean.getQualifiers()) {
                    if (qualifier.annotationType() == Produces.class) {
                        Produces producesAnnotation = (Produces) qualifier;
                        if (mimeType != null && mimeType.isCompatibleTo(
                                producesAnnotation.mimeType())) {
                            if (StringUtils.isNotBlank(producesAnnotation
                                    .version())) {
                                // found a mimetype and a version, so add the bean with this version
                                versionedBeans.put(new VersionExpression(
                                        producesAnnotation.version()),
                                        (Bean<ResponseWriter<T>>) bean);
                            } else {
                                versionedBeans.put(
                                        new VersionExpression(bean),
                                        (Bean<ResponseWriter<T>>) bean);
                            }
                        }
                        continue outer;
                    }
                }
                // add the bean only, if the mimeType is not specified or text/plain.
                versionedBeans.put(
                        new VersionExpression(bean),
                        (Bean<ResponseWriter<T>>) bean);
            }
        }
        System.out.println("Versions are: ");
        for (Version v : versionedBeans.keySet()) {
            System.out.println(v.toString());
        }
        // (2) now resolve the correct bean for the correct version
        Bean<ResponseWriter<T>> resolved = versionNumberResolver.resolve(
                versionedBeans, versionMatcher);
        if (resolved != null) {
            return resolved.create(beanManager
                    .createCreationalContext(resolved));
        }
        return null;
    }

    private void writeResponse(HttpServletResponse resp, Response<?> response,
            final HttpServletRequest req) throws IOException {
        String versionString = req.getHeader("Accept-Version");
        String contentType = req.getHeader("Accept");
        if (StringUtils.isBlank(contentType)) {
            contentType = "application/xml";
        }
        MediaTypeExpression mediaType = new MediaTypeExpression(contentType);
        VersionNumberRangeExpression version;
        if (StringUtils.isBlank(versionString)) {
            version = new VersionNumberRangeExpression(mediaType.version());
        } else {
            version = new VersionNumberRangeExpression(versionString);
        }

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
        resp.setStatus(response.getStatus());
        resp.setContentType(mediaType.toString());
        if (response.getEntity() != null) {
            ResponseWriter writer =
                    findResponseWriterForClassAndMimeTypeAndVersion(response
                    .getEntity().getClass(), mediaType, version);
            if (writer != null) {
                writer.processElement(response.getEntity(), resp.getWriter(),
                        mediaType);
            } else {
                resp.sendError(406,
                        "The resource is not available with mediatype: "
                        + mediaType.toString() + " and version: " + version
                        .toString());
            }
        }
    }

    private Object readContent(HttpServletRequest req) throws Exception {
        final String contentType = req.getHeader("Content-Type");
        final String normalizedContentType = contentType.trim().toLowerCase();
        throw new Exception("unsupported Media-Type: " + contentType);
    }
}
