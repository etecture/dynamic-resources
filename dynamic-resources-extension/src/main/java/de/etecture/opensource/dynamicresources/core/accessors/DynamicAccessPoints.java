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

import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponse;
import de.etecture.opensource.dynamicresources.utils.NewLiteral;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class DynamicAccessPoints {

    private final static class CacheKey<M, A extends DynamicAccessPoint<M>> {

        private final Class<A> accessPointType;
        private final M metadata;

        CacheKey(
                Class<A> accessPointType, M metadata) {
            this.accessPointType = accessPointType;
            this.metadata = metadata;
        }

        public Class<A> getAccessPointType() {
            return accessPointType;
        }

        public M getMetadata() {
            return metadata;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.accessPointType);
            hash = 53 * hash + Objects.hashCode(this.metadata);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CacheKey<M, A> other = (CacheKey<M, A>) obj;
            if (!Objects.equals(this.accessPointType, other.accessPointType)) {
                return false;
            }
            if (!Objects.equals(this.metadata, other.metadata)) {
                return false;
            }
            return true;
        }
    }

    private final static class AccessPointCache {

        private final Map<CacheKey<?, ?>, WeakReference<? extends DynamicAccessPoint>> cache =
                new HashMap<>();

        public <M, A extends DynamicAccessPoint<M>> void store(A accessPoint,
                M metadata) {
            CacheKey<M, A> key = new CacheKey(accessPoint.getClass(), metadata);
            cache.put(key, new WeakReference(accessPoint));
        }

        public <M, A extends DynamicAccessPoint<M>> A retrieve(Class<A> type,
                M metadata, Callable<A> creator) throws Exception {
            CacheKey<M, A> key = new CacheKey(type, metadata);
            A accessPoint = null;
            if (cache.containsKey(key)) {
                accessPoint = (A) cache.get(key).get();
            }
            if (accessPoint == null) {
                accessPoint = creator.call();
                store(accessPoint, metadata);
            }
            return accessPoint;
        }
    }
    private final AccessPointCache cache = new AccessPointCache();
    @Inject
    Instance<DynamicAccessPoint> accessPoints;

    public DynamicApplicationAccessor create(Application application) {
        return create(DynamicApplicationAccessor.class, application);
    }

    public DynamicResourceAccessor create(Resource resource) {
        return create(DynamicResourceAccessor.class, resource);
    }

    public <T> DynamicTypedResourceAccessor<T> create(Resource resource,
            Class<T> responseType) {
        return create(DynamicTypedResourceAccessor.class, responseType);
    }

    public <R, B> DynamicMethodAccessor<R, B> create(
            ResourceMethodResponse<R> response,
            Map<String, String> pathparameter) {
        return create(DynamicMethodAccessor.class, pathparameter);
    }

    private <M, A extends DynamicAccessPoint<M>> A create(
            Class<A> type, final M metadata, final Object... informations) {
        try {
            // lookup the cache.
            return cache.retrieve(type, metadata, new Callable<A>() {
                @Override
                public A call() throws Exception {
                    A accessPoint = (A) accessPoints.select(
                            new DynamicAccessPointType(metadata.getClass()),
                            new NewLiteral()).get();
                    accessPoint.init(metadata, informations);
                    return accessPoint;
                }
            });
        } catch (Exception ex) {
            throw new InjectionException(ex);
        }
    }

    private static class DynamicAccessPointType<A> extends TypeLiteral<DynamicAccessPoint<A>> {

        private static final long serialVersionUID = 1L;

        DynamicAccessPointType(Class<A> subtype) {
        }
    }
}
