package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.api.EntityNotFoundException;
import de.etecture.opensource.dynamicresources.api.ForEntity;
import de.etecture.opensource.dynamicresources.api.XMLWriter;
import java.io.Serializable;
import javax.ws.rs.core.UriInfo;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author rhk
 */
public enum XMLTest implements XMLWriter {

    @ForEntity(String.class)
    BLABLA {
        @Override
        public void process(Object element, XMLStreamWriter writer,
                UriInfo uriInfo) throws XMLStreamException {
            writer.writeStartElement("test");
            writer.writeCData(element.toString());
            writer.writeEndElement();
        }
    },
    @ForEntity(EntityNotFoundException.class)
    NOT_FOUND {
        @Override
        public void process(Object element, XMLStreamWriter writer,
                UriInfo uriInfo) throws XMLStreamException {
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
    @ForEntity(TestResource.class)
    TEST {
        @Override
        public void process(Object element, XMLStreamWriter writer,
                UriInfo uriInfo) throws XMLStreamException {
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
    @ForEntity(TestResources.class)
    TESTS {
        @Override
        public void process(Object element, XMLStreamWriter writer,
                UriInfo uriInfo) throws XMLStreamException {
            writer.writeStartElement("test");
            writer.writeAttribute("count", "" + ((TestResources) element)
                    .getCount());
            writer.writeEndElement();
        }
    }
}
