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

import de.etecture.opensource.dynamicresources.annotations.declaration.Consumes;
import de.etecture.opensource.dynamicresources.annotations.declaration.Method;
import de.etecture.opensource.dynamicresources.extension.RequestReaderResolver;
import de.etecture.opensource.dynamicresources.extension.ResponseWriterResolver;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class AnnotatedResourceMethod implements ResourceMethod {

    private final String name, description;
    private final Set<ResourceMethodRequest> requests = new HashSet<>();

    public AnnotatedResourceMethod(Class<?> resourceClass, Method annotation,
            ResponseWriterResolver writers,
            RequestReaderResolver readers) {
        this.name = annotation.name();
        this.description = annotation.description();
        if (annotation.consumes().length > 0) {
            for (Consumes consumes : annotation.consumes()) {
                this.requests.add(new AnnotatedResourceMethodRequest(
                        resourceClass,
                        annotation, consumes, writers, readers));
            }
        } else {
            this.requests.add(new AnnotatedResourceMethodRequest(resourceClass,
                    annotation, writers, readers));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<ResourceMethodRequest> getRequests() {
        return Collections.unmodifiableSet(requests);
    }
}
