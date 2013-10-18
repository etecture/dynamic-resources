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
}
