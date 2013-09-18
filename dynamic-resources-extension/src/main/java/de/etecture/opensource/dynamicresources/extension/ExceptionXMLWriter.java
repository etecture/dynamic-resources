package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.ForEntity;
import de.etecture.opensource.dynamicresources.api.XMLWriter;
import javax.ws.rs.core.UriInfo;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author rhk
 */
@ForEntity(Exception.class)
public class ExceptionXMLWriter implements XMLWriter {

    @Override
    public void process(Object element, XMLStreamWriter writer, UriInfo uriInfo)
            throws XMLStreamException {
        Throwable ex = (Throwable) element;
        internalProcess(writer, ex);
    }

    private void internalProcess(XMLStreamWriter writer, Throwable ex) throws
            XMLStreamException {
        writer.writeStartElement("exception");
        writer.writeAttribute("type", ex.getClass().getSimpleName());

        writer.writeStartElement("message");
        writer.writeCharacters(ex.getMessage());
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
}
