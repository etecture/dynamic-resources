package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.XMLWriter;
import javax.enterprise.inject.Default;
import javax.ws.rs.core.UriInfo;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author rhk
 */
@Default
public class DefaultXMLWriter implements XMLWriter {

    @Override
    public void process(Object element, XMLStreamWriter writer, UriInfo uriInfo)
            throws XMLStreamException {
        writer.writeStartElement("response");
        writer.writeCharacters(element == null ? "null" : element
                .toString());
        writer.writeEndElement();
    }
}
