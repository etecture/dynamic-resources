package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.JSONWriter;
import javax.enterprise.inject.Default;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author rhk
 */
@Default
public class DefaultJSONWriter implements JSONWriter {

    @Override
    public void process(Object element, JsonGenerator generator, UriInfo uriInfo) {
        generator.writeStartArray().write(element == null ? "null" : element
                .toString()).writeEnd();
    }
}
