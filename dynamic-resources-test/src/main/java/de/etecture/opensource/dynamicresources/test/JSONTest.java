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
package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.api.EntityNotFoundException;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collections;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

/**
 *
 * @author rhk
 */
public enum JSONTest implements ResponseWriter {

    @Produces(contentType = String.class, mimeType = "application/json")
    BLABLA {
        @Override
        protected void process(Object element, JsonGenerator generator) {
            generator.writeStartObject().write("test", element.toString())
                    .writeEnd();
        }
    },
    @Produces(contentType = EntityNotFoundException.class, mimeType =
            "application/json")
    NOT_FOUND {
        @Override
        protected void process(Object element, JsonGenerator generator) {
            final EntityNotFoundException enfe =
                    (EntityNotFoundException) element;
            final Serializable entityId =
                    enfe.getEntityId();
            generator.writeStartObject()
                    .write("id", entityId.toString())
                    .write("entity", enfe.getEntityClass().getSimpleName())
                    .writeEnd();
        }
    },
    @Produces(contentType = TestResources.class, mimeType = "application/json")
    TESTS {
        @Override
        protected void process(Object element, JsonGenerator generator) {
            generator.writeStartObject().write("count",
                    ((TestResources) element).getCount()).writeEnd();
        }
    },
    TEST {
        @Override
        protected void process(Object element, JsonGenerator generator) {
            TestResource resource = (TestResource) element;
            generator.writeStartObject();
            generator.write("id", resource.getId());
            generator.write("firstName", resource.getFirstName());
            generator.write("lastName", resource.getLastName());
            generator.writeEnd();
        }
    };
    private static final JsonGeneratorFactory JSON_FACTORY = Json
            .createGeneratorFactory(Collections.<String, Object>singletonMap(
            JsonGenerator.PRETTY_PRINTING, "true"));

    protected abstract void process(Object element, JsonGenerator generator);

    @Override
    public void processElement(Object element, Writer writer, MediaType mimetype)
            throws IOException {
        JsonGenerator jg = JSON_FACTORY.createGenerator(writer);
        process(element, jg);
        jg.flush();
        jg.close();
    }

    @Override
    public int getContentLength(Object entity, MediaType acceptedMediaType) {
        return -1;
    }
}
