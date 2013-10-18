package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;

/**
 *
 * @author rhk
 */
public class ResponseWriterResolver extends AbstractMimeAndVersionResolver<ResponseWriter, Produces> {

    public ResponseWriterResolver() {
        super(ResponseWriter.class, Produces.class);
    }

    @Override
    protected Class<?> getTypeDefinition(Produces annotation) {
        return annotation.contentType();
    }

    @Override
    protected String[] getMimeType(Produces annotation) {
        return annotation.mimeType();
    }

    @Override
    protected String getVersion(Produces annotation) {
        return annotation.version();
    }
}
