package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.api.EntityNotFoundException;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author rhk
 */
public enum XMLTest implements ResponseWriter {

    @Produces(contentType = String.class, mimeType = {"application/xml",
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
    @Produces(contentType = EntityNotFoundException.class, mimeType = {
        "application/xml",
        "text/xml"})
    NOT_FOUND {
        @Override
        protected void process(Object element, XMLStreamWriter writer) throws
                XMLStreamException {
            final EntityNotFoundException enfe =
                    (EntityNotFoundException) element;
            final Serializable entityId =
                    enfe.getEntityId();
            writer.writeStartElement("NotFoundException");
            writer.writeAttribute("id", entityId.toString());
            writer.writeAttribute("entity", enfe.getEntityClass()
                    .getSimpleName());
            writer.writeEndElement();
        }
    },
    @Produces(contentType = TestResource.class, mimeType = {"application/xml",
        "text/xml"})
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
            writer.writeEndElement();
        }
    },
    @Produces(contentType = TestResource.class, mimeType = {"application/xml",
        "text/xml"}, version = "1.0")
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
    @Produces(contentType = TestResource.class, mimeType = {"application/xml",
        "text/xml"}, version = "2.0")
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
    @Produces(contentType = TestResource.class, mimeType = {"application/xml",
        "text/xml"}, version = "2.0.1")
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
    @Produces(contentType = TestResources.class, mimeType = {"application/xml",
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
}
