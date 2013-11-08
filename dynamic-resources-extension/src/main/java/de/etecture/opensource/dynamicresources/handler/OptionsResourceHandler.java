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

import com.sun.jersey.server.impl.uri.PathTemplate;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.Version;
import de.etecture.opensource.dynamicresources.extension.Current;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.Verb;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
@Verb("OPTIONS")
public class OptionsResourceHandler implements ResourceMethodHandler {

    @Inject
    @Any
    Instance<ResourceMethodHandler> resourceMethodHandlers;
    @Inject
    ResponseWriterResolver responseWriters;
    @Inject
    @Current
    HttpServletRequest request;
    @Inject
    @Current
    HttpServletResponse response;

    @Override
    public boolean isAvailable(
            Class<?> resourceClazz) {
        return true; // the OPTIONS method is available for any resources
    }

    @Override
    public <T> void handleRequest(
            Class<T> resourceClazz, Map<String, String> pathValues) throws
            IOException {
        response.setStatus(200);
        response.setContentType("text/plain");
        final PrintWriter writer = response.getWriter();

        writer.printf("Available Methods for Resource: %s\n", resourceClazz
                .getSimpleName());
        writer.println(StringUtils.repeat("-", resourceClazz.getSimpleName()
                .length() + 32));
        writer.println();

        for (ResourceMethodHandler handler : resourceMethodHandlers) {
            if (handler.isAvailable(resourceClazz)) {
                String verb = "";
                if (handler.getClass().isAnnotationPresent(Verb.class)) {
                    verb = handler.getClass().getAnnotation(Verb.class).value();
                }
                writer.printf("\t%s%n\t\t- %s%n", verb, handler
                        .getDescription(resourceClazz));
            }
        }
        writer.println();
        writer.printf("Available MediaTypes of Resource: %s\n", resourceClazz
                .getSimpleName());
        writer.println(StringUtils.repeat("-", resourceClazz.getSimpleName()
                .length() + 34));
        writer.println();

        String exampleMediaType = null;
        for (Map.Entry<MediaType, List<Version>> e : responseWriters
                .getAvailableFormats(resourceClazz).entrySet()) {
            writer.printf("\t%s%n", e.getKey().toString());
            writer.print("\t\tVersions: ");
            boolean first = true;
            for (Version version : e.getValue()) {
                if (!first) {
                    writer.print(", ");
                }
                writer.print(version.toString());
                first = false;
                if (exampleMediaType == null) {
                    exampleMediaType = String.format("%s/%s.v%s; charset=%s", e
                            .getKey()
                            .category(), e.getKey().subType(), version
                            .toString(), e.getKey().encoding());
                }
            }
            if (exampleMediaType == null) {
                exampleMediaType = e.getKey().toString();
            }
            writer.println();
        }

        writer.println();
        writer.printf("Example Request for Resource: %s\n", resourceClazz
                .getSimpleName());
        writer.println(StringUtils.repeat("-", resourceClazz.getSimpleName()
                .length() + 30));
        writer.println();

        Resource resource = resourceClazz.getAnnotation(Resource.class);
        String path;
        if (pathValues != null) {
            PathTemplate pt = new PathTemplate(resource.uri());
            path = pt.createURI(pathValues);
        } else {
            path = resource.uri();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        writer.printf("> GET %s%s HTTP/1.1%n", request.getContextPath(), path);
        writer.printf("> Host: %s:%s%n", request.getServerName(), request
                .getServerPort());
        if (exampleMediaType != null) {
            writer.printf("> Accept: %s%n", exampleMediaType);
        }
        writer.printf("> ...%n");
        writer.println();
        writer.printf("< HTTP/1.1 200 OK%n");
        if (exampleMediaType != null) {
            writer.printf("< Content-Type : %s%n", exampleMediaType);
        }
        writer.printf("< ...%n");
        writer.println();
        writer.flush();
    }

    @Override
    public String getDescription(
            Class<?> resourceClazz) {
        return String.format(
                "Returns the HTTP methods that the server supports for the resource: %s",
                resourceClazz.getSimpleName());
    }

    @Override
    public <T> T execute(
            Class<T> resourceClazz,
            Map<String, Object> params, Object request) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
