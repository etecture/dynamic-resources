package de.etecture.opensource.dynamicresources.handler;

import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import de.etecture.opensource.dynamicresources.api.PUT;
import de.etecture.opensource.dynamicresources.spi.AbstractReflectiveResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.Verb;

/**
 *
 * @author rhk
 */
@Verb("PUT")
public class PutResourceHandler extends AbstractReflectiveResourceMethodHandler<PUT>
        implements ResourceMethodHandler {

    public PutResourceHandler() {
        super(PUT.class, QueryMetaData.Kind.UPDATE);
    }
}
