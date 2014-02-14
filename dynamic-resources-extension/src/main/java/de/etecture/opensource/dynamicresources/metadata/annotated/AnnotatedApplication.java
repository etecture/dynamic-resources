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

import de.etecture.opensource.dynamicresources.metadata.AbstractApplication;
import de.etecture.opensource.dynamicresources.metadata.Application;
import java.lang.reflect.AnnotatedElement;
import java.net.URISyntaxException;

/**
 * This is the default implementation of the {@link Application} metadata
 * interface that is based on annotation information.
 *
 * @author rhk
 * @version
 * @since
 */
public class AnnotatedApplication extends AbstractApplication implements
        Annotated<de.etecture.opensource.dynamicresources.annotations.Application> {

    private final de.etecture.opensource.dynamicresources.annotations.Application annotation;
    private final AnnotatedElement annotatedElement;

    public AnnotatedApplication(AnnotatedElement annotatedElement,
            de.etecture.opensource.dynamicresources.annotations.Application annotation)
            throws URISyntaxException {
        super(annotation.name(), annotation.base(), annotation
                .description());
        this.annotatedElement = annotatedElement;
        this.annotation = annotation;
    }

    @Override
    public de.etecture.opensource.dynamicresources.annotations.Application getAnnotation() {
        return annotation;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }
}
