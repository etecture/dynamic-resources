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
package de.etecture.opensource.dynamicresources.utils;

import de.etecture.opensource.dynamicresources.annotations.Consumes;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class ConsumesLiteral extends AnnotationLiteral<Consumes> implements
        Consumes {

    private static final long serialVersionUID = 1L;
    private final String[] mimeTypes;
    private final Class requestType;
    private final int priority;

    public ConsumesLiteral(Class requestType) {
        this.requestType = requestType;
        this.mimeTypes = new String[]{"text/plain"};
        this.priority = 0;
    }

    public ConsumesLiteral(Object object) {
        this.requestType = object.getClass();
        this.mimeTypes = new String[]{"text/plain"};
        this.priority = 0;
    }

    public ConsumesLiteral(Consumes consumes) {
        this(consumes.requestType(), consumes.mimeType(),
                consumes.priority());
    }

    public ConsumesLiteral(Class requestType, String[] mimeTypes,int priority) {
        this.requestType = requestType;
        this.mimeTypes = mimeTypes;
        this.priority = priority;
    }

    @Override
    public String[] mimeType() {
        return mimeTypes;
    }

    @Override
    public Class requestType() {
        return requestType;
    }

    @Override
    public int priority() {
        return priority;
    }
}