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
package de.etecture.opensource.dynamicresources.core.accessors;

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.accesspoints.ApplicationAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.TypedResourceAccessor;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeAmbigiousException;
import de.etecture.opensource.dynamicresources.metadata.MediaTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourcePathNotMatchException;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.utils.ResourceLiteral;
import java.util.Map;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class DynamicApplicationAccessor implements ApplicationAccessor {

    private final Application application;
    @Inject
    Instance<Object> accessPoints;

    DynamicApplicationAccessor() {
        throw new IllegalStateException("why the heck wants to proxy this bean?");
    }

    public DynamicApplicationAccessor(Application application) {
        this.application = application;
    }

    @Override
    public Application getMetadata() {
        return application;
    }

    @Override
    public <X> TypedResourceAccessor<X> selectByName(String name,
            Class<X> responseType) throws ResourceNotFoundException,
            ResponseTypeNotSupportedException {
        return selectByName(name).select(responseType);
    }

    @Override
    public <X> TypedResourceAccessor<X> selectByPath(String path,
            Class<X> responseType) throws ResourceNotFoundException,
            ResponseTypeNotSupportedException {
        return selectByPath(path).select(responseType);
    }

    @Override
    public ResourceAccessor selectByName(String name) throws
            ResourceNotFoundException {
        // do the resource exists?
        if (!application.getResources().containsKey(name)) {
            throw new ResourceNotFoundException("resource with name: " + name
                    + " not found within the application: " + application
                    .getName());
        }
        return accessPoints.select(ResourceAccessor.class, new ResourceLiteral(
                name)).get();
    }

    @Override
    public ResourceAccessor selectByPath(String path) throws
            ResourceNotFoundException {
        Resource resource = application.findResource(path);
        try {
            return selectByName(resource.getName())
                    .pathParams(
                    resource.getPath().
                    getPathParameterValues(path));
        } catch (ResourcePathNotMatchException ex) {
            throw new ResourceNotFoundException(
                    "cannot extract path parameters from path: " + path
                    + " for resource: " + resource.getName(), ex);
        }
    }

    @Override
    public <X> MethodAccessor<X> selectByPath(String path, String method,
            Class<X> responseType) throws ResourceMethodNotFoundException,
            ResourceNotFoundException, ResponseTypeNotSupportedException {
        return selectByPath(path, responseType).method(method);
    }

    @Override
    public <X> MethodAccessor<X> selectByPathAndMime(String path, String method,
            MediaType acceptedMediaType) throws
            ResourceNotFoundException,
            MediaTypeNotSupportedException, MediaTypeAmbigiousException,
            ResourceMethodNotFoundException {
        return (MethodAccessor<X>) selectByPath(path).method(method,
                acceptedMediaType);
    }

    @Override
    public <X> MethodAccessor<X> selectByName(String name, String method,
            Class<X> responseType, String... pathParamValues) throws
            ResourceMethodNotFoundException, ResourceNotFoundException,
            ResponseTypeNotSupportedException {
        TypedResourceAccessor<X> accessor = selectByName(name, responseType);
        int i = 0;
        for (String pathParameterName : accessor.getMetadata().getPath().
                getPathParameterNames()) {
            accessor =
                    accessor.pathParam(pathParameterName, pathParamValues[i++]);
        }
        return accessor.method(method);
    }

    @Override
    public <X> MethodAccessor<X> selectByName(String name, String method,
            Class<X> responseType,
            Map<String, String> pathParams) throws
            ResourceMethodNotFoundException, ResourceNotFoundException,
            ResponseTypeNotSupportedException {
        return selectByName(name, responseType).pathParams(pathParams).
                method(method);
    }
}
