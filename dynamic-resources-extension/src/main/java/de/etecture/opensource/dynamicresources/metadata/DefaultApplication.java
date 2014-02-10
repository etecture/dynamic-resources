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

import de.etecture.opensource.dynamicresources.extension.RequestReaderResolver;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class DefaultApplication implements Application {

    private final String baseURI;
    private final Map<String, Resource> resources = new HashMap<>();

    public DefaultApplication(String baseURI) {
        this.baseURI = baseURI;
    }

    @Override
    public String getBaseURI() {
        return baseURI;
    }

    @Override
    public Map<String, Resource> getResources() {
        return Collections.unmodifiableMap(resources);
    }

    public void addResource(Resource resource) {
        this.resources.put(resource.getUriTemplate(), resource);
    }

    public boolean isResource(Class<?> resourceClass) {
        return resourceClass.isAnnotationPresent(
                de.etecture.opensource.dynamicresources.annotations.declaration.Resource.class);
    }

    public Resource addAsResource(Class<?> resourceClass,
            ResponseWriterResolver writers,
            RequestReaderResolver readers) {
        Resource r =
                new AnnotatedResource(this, resourceClass, writers, readers);
        addResource(r);
        return r;
    }

}
