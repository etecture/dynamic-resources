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

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.annotations.declaration.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.api.UriBuilder;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import javax.inject.Inject;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author rhk
 */
public enum XMLTest implements ResponseWriter {

    @Produces(contentType = String.class,
              mimeType = {"application/xml",
        "text/xml"})
    BLABLA {
        @Override
        protected void process(Object element, XMLStreamWriter writer)
                throws XMLStreamException {
            writer.writeStartElement("test");
            writer.writeCData(element.toString());
            writer.writeEndElement();
        }
    },
    TEST_NEWEST {
        @Override
        protected void process(Object element, XMLStreamWriter writer) throws
                XMLStreamException {
            TestResource resource = (TestResource) element;
            writer.writeStartElement("testresource");
            writer.writeAttribute("id", resource.getId());
            writer.writeStartElement("firstName");
            writer.writeCharacters(resource.getFirstName());
            writer.writeEndElement();
            writer.writeStartElement("lastName");
            writer.writeCharacters(resource.getLastName());
            writer.writeEndElement();
            writer.writeStartElement("uri");
            writer.writeCharacters(uriBuilder.build(TestResource.class,
                    Collections.singletonMap("id", resource
                    .getId())));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    },
    @Produces(contentType = TestResource.class,
              mimeType = {"application/xml",
        "text/xml"},
              version = "1.0")
    TEST1_0 {
        @Override
        protected void process(Object element, XMLStreamWriter writer) throws
                XMLStreamException {
            TestResource resource = (TestResource) element;
            writer.writeStartElement("testresource");
            writer.writeAttribute("version", "1.0");
            writer.writeAttribute("id", resource.getId());
            writer.writeStartElement("firstName");
            writer.writeCharacters(resource.getFirstName());
            writer.writeEndElement();
            writer.writeStartElement("lastName");
            writer.writeCharacters(resource.getLastName());
            writer.writeEndElement();
            writer.writeEndElement();
        }
    },
    @Produces(contentType = TestResource.class,
              mimeType = {"application/xml",
        "text/xml"},
              version = "2.0")
    TEST2_0 {
        @Override
        protected void process(Object element, XMLStreamWriter writer) throws
                XMLStreamException {
            TestResource resource = (TestResource) element;
            writer.writeStartElement("testresource");
            writer.writeAttribute("version", "2.0");
            writer.writeAttribute("id", resource.getId());
            writer.writeStartElement("firstName");
            writer.writeCharacters(resource.getFirstName());
            writer.writeEndElement();
            writer.writeStartElement("lastName");
            writer.writeCharacters(resource.getLastName());
            writer.writeEndElement();
            writer.writeEndElement();
        }
    },
    @Produces(contentType = TestResource.class,
              mimeType = {"application/xml",
        "text/xml"},
              version = "2.0.1")
    TEST2_0_1 {
        @Override
        protected void process(Object element, XMLStreamWriter writer) throws
                XMLStreamException {
            TestResource resource = (TestResource) element;
            writer.writeStartElement("testresource");
            writer.writeAttribute("version", "2.0.1");
            writer.writeAttribute("id", resource.getId());
            writer.writeStartElement("firstName");
            writer.writeCharacters(resource.getFirstName());
            writer.writeEndElement();
            writer.writeStartElement("lastName");
            writer.writeCharacters(resource.getLastName());
            writer.writeEndElement();
            writer.writeEndElement();
        }
    },
    @Produces(contentType = TestResources.class,
              mimeType = {"application/xml",
        "text/xml"})
    TESTS {
        @Override
        protected void process(Object element, XMLStreamWriter writer) throws
                XMLStreamException {
            writer.writeStartElement("test");
            writer.writeAttribute("count", "" + ((TestResources) element)
                    .getCount());
            writer.writeEndElement();
        }
    };
    private static final XMLOutputFactory XML_FACTORY = XMLOutputFactory
            .newFactory();
    @Inject
    UriBuilder uriBuilder;

    protected abstract void process(Object element, XMLStreamWriter writer)
            throws XMLStreamException;

    @Override
    public void processElement(Object element, Writer writer, MediaType mimetype)
            throws
            IOException {
        try {
            final XMLStreamWriter xmlwriter =
                    XML_FACTORY.createXMLStreamWriter(writer);
            process(element, xmlwriter);
            xmlwriter.flush();
            xmlwriter.close();
        } catch (XMLStreamException ex) {
            throw new IOException("cannot write the exception to xml response.",
                    ex);
        }
    }

    @Override
    public int getContentLength(Object entity, MediaType acceptedMediaType) {
        return -1;
    }
}
