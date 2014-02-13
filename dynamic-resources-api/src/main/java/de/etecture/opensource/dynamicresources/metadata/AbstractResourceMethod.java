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
package de.etecture.opensource.dynamicresources.metadata;

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.ResourceMethodInterceptor;
import de.etecture.opensource.dynamicresources.utils.AbstractValueMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public abstract class AbstractResourceMethod implements ResourceMethod {

    private final Resource resource;
    private final String name;
    private final String description;
    private final Set<String> allowedRoleNames = new HashSet<>();
    private final Set<ResourceMethodFilter<?>> filters = new HashSet<>();
    private final List<Class<? extends ResourceMethodInterceptor>> interceptors =
            new ArrayList<>();
    private final Set<ResourceMethodRequest<?>> requests = new HashSet<>();
    private final Set<ResourceMethodResponse<?>> responses = new HashSet<>();

    protected AbstractResourceMethod(Resource resource, String name,
            String description) {
        this.resource = resource;
        this.name = name;
        this.description = description;
    }

    protected void addAllowedRoleName(String roleName) {
        this.allowedRoleNames.add(roleName);
    }

    protected <T> void addFilter(
            ResourceMethodFilter<T> filter) {
        if (filter.getResourceMethod() != this) {
            throw new IllegalArgumentException(
                    "can only add filter that is part of this method.");
        }
        this.filters.add(filter);
    }

    protected void addInterceptor(
            Class<? extends ResourceMethodInterceptor> interceptor) {
        this.interceptors.add(interceptor);
    }

    protected <B> void addRequest(
            ResourceMethodRequest<B> request) {
        if (request.getMethod() != this) {
            throw new IllegalArgumentException(
                    "can only add request that is part of this method.");
        }
        this.requests.add(request);
    }

    protected <R> void addResponse(
            ResourceMethodResponse<R> response) {
        if (response.getMethod() != this) {
            throw new IllegalArgumentException(
                    "can only add response that is part of this method.");
        }
        this.responses.add(response);
    }

    @Override
    public Set<String> getAllowedRoleNames() {
        return Collections.unmodifiableSet(allowedRoleNames);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<ResourceMethodFilter<?>> getFilters() {
        return Collections.unmodifiableSet(filters);
    }

    @Override
    public List<Class<? extends ResourceMethodInterceptor>> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<ResourceMethodRequest<?>> getRequests(MediaType mediaType) throws
            MediaTypeNotAllowedException {
        Set<ResourceMethodRequest<?>> possibleRequests = new HashSet<>();
        for (ResourceMethodRequest<?> request : requests) {
            if (mediaType.isCompatibleTo(request.getAcceptedRequestMediaTypes().
                    toArray(new MediaType[request.getAcceptedRequestMediaTypes()
                    .size()]))) {
                possibleRequests.add(request);
                continue;
            }
        }
        if (possibleRequests.isEmpty()) {
            throw new MediaTypeNotAllowedException(this, mediaType);
        }
        return possibleRequests;
    }

    @Override
    public ResourceMethodRequest<?> getRequest(MediaType mediaType) throws
            MediaTypeNotAllowedException, MediaTypeAmbigiousException {
        Set<ResourceMethodRequest<?>> possibleRequests = getRequests(mediaType);
        if (possibleRequests.size() > 1) {
            throw new MediaTypeAmbigiousException(this, mediaType);
        } else {
            return possibleRequests.iterator().next();
        }
    }

    @Override
    public ResourceMethodResponse<?> getResponse(MediaType mediaType) throws
            MediaTypeNotSupportedException, MediaTypeAmbigiousException {
        Set<ResourceMethodResponse<?>> possibleResponses = getResponses(
                mediaType);
        if (possibleResponses.size() > 1) {
            throw new MediaTypeAmbigiousException(this, mediaType);
        } else {
            return possibleResponses.iterator().next();
        }
    }

    @Override
    public Set<ResourceMethodResponse<?>> getResponses(MediaType mediaType)
            throws
            MediaTypeNotSupportedException {
        Set<ResourceMethodResponse<?>> possibleResponses = new HashSet<>();
        for (ResourceMethodResponse<?> response : responses) {
            if (mediaType.isCompatibleTo(response
                    .getSupportedResponseMediaTypes().
                    toArray(new MediaType[response
                    .getSupportedResponseMediaTypes().
                    size()]))) {
                possibleResponses.add(response);
                continue;
            }
        }
        if (possibleResponses.isEmpty()) {
            throw new MediaTypeNotSupportedException(this, mediaType);
        }
        return possibleResponses;
    }

    @Override
    public Map<Class<?>, ResourceMethodRequest<?>> getRequests() {
        return Collections.unmodifiableMap(
                new AbstractValueMap<Class<?>, ResourceMethodRequest<?>>(
                requests) {
            @Override
            protected Class<?> getKeyForValue(
                    ResourceMethodRequest<?> value) {
                return value.getRequestType();
            }

            @Override
            public ResourceMethodRequest<?> get(Object key) {
                if (key != null) {
                    if (Class.class.isInstance(key)) {
                        for (ResourceMethodRequest<?> request : requests) {
                            if (request.getRequestType().isAssignableFrom(
                                    (Class) key)) {
                                return request;
                            }
                        }
                    } else {
                        for (ResourceMethodRequest<?> request : requests) {
                            if (request.getRequestType().isInstance(key)) {
                                return request;
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            public boolean containsKey(Object key) {
                if (key != null) {
                    if (Class.class.isInstance(key)) {
                        for (ResourceMethodRequest<?> request : requests) {
                            if (request.getRequestType().isAssignableFrom(
                                    (Class) key)) {
                                return true;
                            }
                        }
                    } else {
                        for (ResourceMethodRequest<?> request : requests) {
                            if (request.getRequestType().isInstance(key)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    @Override
    public Map<Class<?>, ResourceMethodResponse<?>> getResponses() {
        return Collections.unmodifiableMap(
                new AbstractValueMap<Class<?>, ResourceMethodResponse<?>>(
                responses) {
            @Override
            protected Class<?> getKeyForValue(
                    ResourceMethodResponse<?> value) {
                return value.getResponseType();
            }

            @Override
            public ResourceMethodResponse<?> get(Object key) {
                if (key != null) {
                    if (Class.class.isInstance(key)) {
                        for (ResourceMethodResponse<?> response : responses) {
                            if (response.getResponseType().isAssignableFrom(
                                    (Class) key)) {
                                return response;
                            }
                        }
                    } else {
                        for (ResourceMethodResponse<?> response : responses) {
                            if (response.getResponseType().isInstance(key)) {
                                return response;
                            }
                        }
                    }
                }
                return null;
            }

            @Override
                    public boolean containsKey(Object key) {
                        if (key != null) {
                            if (Class.class.isInstance(key)) {
                                for (ResourceMethodResponse<?> response
                                        : responses) {
                                    if (response.getResponseType()
                                            .isAssignableFrom(
                                            (Class) key)) {
                                        return true;
                                    }
                                }
                            } else {
                                for (ResourceMethodResponse<?> response
                                        : responses) {
                                    if (response.getResponseType().isInstance(
                                            key)) {
                                        return true;
                                    }
                                }
                            }
                        }
                return false;
            }
        });
    }

    @Override
    public Resource getResource() {
        return resource;
    }
}
