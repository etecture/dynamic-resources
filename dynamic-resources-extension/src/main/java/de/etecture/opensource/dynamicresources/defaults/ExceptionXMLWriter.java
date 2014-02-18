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
package de.etecture.opensource.dynamicresources.defaults;

import de.etecture.opensource.dynamicresources.annotations.Produces;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author rhk
 */
@Produces(contentType = Exception.class,
          mimeType = {"application/xml",
    "text/xml"})
public class ExceptionXMLWriter implements ResponseWriter<Throwable> {

    private void internalProcess(XMLStreamWriter writer, Throwable ex)
            throws
            XMLStreamException {
        writer.writeStartElement("exception");
        writer.writeAttribute("type", ex.getClass().getSimpleName());

        writer.writeStartElement("message");
        if (ex.getMessage() != null) {
            writer.writeCharacters(ex.getMessage());
        }
        writer.writeEndElement();

        writer.writeStartElement("trace");
        for (StackTraceElement ste : ex.getStackTrace()) {
            writer.writeStartElement("traceElement");
            writer.writeAttribute("class", ste.getClassName());
            if (ste.getFileName() != null) {
                writer.writeAttribute("file", ste.getFileName());
            }
            if (ste.getMethodName() != null) {
                writer.writeAttribute("method", ste.getMethodName());
            }
            writer.writeAttribute("line", "" + ste.getLineNumber());
            writer.writeEndElement();
        }
        writer.writeEndElement();

        if (ex.getCause() != null) {
            writer.writeStartElement("cause");
            internalProcess(writer, ex.getCause());
            writer.writeEndElement();
        }
    }

    @Override
    public void processElement(Throwable element, Writer writer,
            MediaType mimetype) throws
            IOException {
        try {
            final XMLStreamWriter xmlwriter =
                    XML_FACTORY.createXMLStreamWriter(writer);
            internalProcess(xmlwriter, element);
            xmlwriter.flush();
            xmlwriter.close();
        } catch (XMLStreamException ex) {
            throw new IOException(
                    "cannot write the exception to xml response.",
                    ex);
        }
    }

    @Override
    public int getContentLength(Throwable entity,
            MediaType acceptedMediaType) {
        return -1;
    }
    static final int PRIORITY = Integer.MAX_VALUE - 1;
    private static final XMLOutputFactory XML_FACTORY = XMLOutputFactory
            .newFactory();
}
