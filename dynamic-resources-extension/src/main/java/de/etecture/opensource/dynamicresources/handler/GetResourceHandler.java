package de.etecture.opensource.dynamicresources.handler;

import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import de.etecture.opensource.dynamicresources.api.GET;
import de.etecture.opensource.dynamicresources.spi.AbstractReflectiveResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.Verb;

/**
 *
 * @author rhk
 */
@Verb("GET")
public class GetResourceHandler extends AbstractReflectiveResourceMethodHandler<GET>
        implements ResourceMethodHandler {

    public GetResourceHandler() {
        super(GET.class, QueryMetaData.Kind.RETRIEVE);
    }
}
