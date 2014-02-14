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
package de.etecture.opensource.dynamicresources.metadata.annotated;

import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Resource;
import de.etecture.opensource.dynamicresources.metadata.AbstractResource;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.DefaultResourcePath;
import de.etecture.opensource.dynamicresources.metadata.ResourcePath;
import java.lang.reflect.AnnotatedElement;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class AnnotatedResource extends AbstractResource implements
        Annotated<Resource> {

    private final AnnotatedElement annotatedElement;
    private final Resource annotation;
    private final ResourcePath path;

    AnnotatedResource(Application application, Resource annotation,
            AnnotatedElement annotatedElement) {
        super(application, annotation.name(),
                annotation.description());
        this.path = new DefaultResourcePath(this,
                annotation.path());
        this.annotation = annotation;
        this.annotatedElement = annotatedElement;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    @Override
    public ResourcePath getPath() {
        return path;
    }

    @Override
    public Resource getAnnotation() {
        return annotation;
    }

    public static AnnotatedResource create(Application application,
            Resource annotation, AnnotatedElement annotatedElement) throws
            IllegalArgumentException {
        return new AnnotatedResource(application, annotation, annotatedElement);
    }

    public static AnnotatedResource createAndAddMethods(Application application,
            Class<?> annotatedResourceClass,
            Iterable<String> producedMimes, Iterable<String> consumedMimes) {
        de.etecture.opensource.dynamicresources.annotations.Resource annotation =
                annotatedResourceClass.getAnnotation(
                de.etecture.opensource.dynamicresources.annotations.Resource.class);
        if (annotation == null) {
            throw new IllegalArgumentException("The " + annotatedResourceClass
                    + " must be annotated with @Resource to build a resource metadata from it.");
        }

        AnnotatedResource resource = create(application, annotation,
                annotatedResourceClass);
        for (Method method : annotation.methods()) {
            resource.addMethod(AnnotatedResourceMethod.create(resource,
                    annotatedResourceClass, method, producedMimes, consumedMimes));
        }
        return resource;
    }
}
