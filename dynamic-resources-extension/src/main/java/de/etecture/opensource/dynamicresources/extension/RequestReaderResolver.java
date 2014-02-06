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
package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Consumes;
import de.etecture.opensource.dynamicresources.api.RequestReader;

/**
 *
 * @author rhk
 */
public class RequestReaderResolver extends AbstractMimeAndVersionResolver<RequestReader, Consumes> {

    public RequestReaderResolver() {
        super(RequestReader.class, Consumes.class);
    }

    @Override
    protected Class<?> getTypeDefinition(Consumes annotation) {
        return annotation.requestType();
    }

    @Override
    protected String[] getMimeType(Consumes annotation) {
        return annotation.mimeType();
    }

    @Override
    protected String getVersion(Consumes annotation) {
        return annotation.version();
    }

    @Override
    protected int getPriority(Consumes annotation) {
        return annotation.priority();
    }

    /**
     * returns true, if there is a request reader, that matches the given
     * consumes annotations.
     *
     * @param annotations
     * @return
     */
    public boolean exists(Consumes... annotations) {
        if (annotations.length == 0) {
            return true;
        }
        for (Consumes annotation : annotations) {
            for (String mimeType : annotation.mimeType()) {
                if (exists(annotation.requestType(), new MediaTypeExpression(
                        mimeType), new VersionNumberRangeExpression(annotation
                        .version()))) {
                    return true;
                }
            }
        }
        return false;
    }
}
