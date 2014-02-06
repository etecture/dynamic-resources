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

import de.etecture.opensource.dynamicresources.api.Method;
import de.etecture.opensource.dynamicresources.api.metadata.Resource;
import de.etecture.opensource.dynamicresources.api.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.extension.RequestReaderResolver;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class AnnotatedResource implements Resource {

    private final String name, description, uri;
    private final Map<String, ResourceMethod> methods = new HashMap<>();

    public AnnotatedResource(Class<?> resourceClass,
            ResponseWriterResolver writers,
            RequestReaderResolver readers) {
        if (!resourceClass.isAnnotationPresent(
                de.etecture.opensource.dynamicresources.api.Resource.class)) {
            throw new IllegalArgumentException(resourceClass.toString()
                    + " is not a resource class. It is not annotated with @Resource");
        }
        de.etecture.opensource.dynamicresources.api.Resource annotation =
                resourceClass.getAnnotation(
                de.etecture.opensource.dynamicresources.api.Resource.class);
        this.uri = annotation.uri();
        if (StringUtils.isBlank(annotation.name())) {
            this.name = resourceClass.getSimpleName();
        } else {
            this.name = annotation.name();
        }
        this.description = annotation.description();
        for (Method method : annotation.methods()) {
            methods.put(method.name(),
                    new AnnotatedResourceMethod(resourceClass, method, writers,
                    readers));
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Map<String, ResourceMethod> getMethods() {
        return Collections.unmodifiableMap(methods);
    }

    @Override
    public String getUriTemplate() {
        return uri;
    }
}
