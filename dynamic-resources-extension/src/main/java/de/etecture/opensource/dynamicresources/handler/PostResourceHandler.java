package de.etecture.opensource.dynamicresources.handler;

import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import de.etecture.opensource.dynamicresources.api.POST;
import de.etecture.opensource.dynamicresources.spi.AbstractReflectiveResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.Verb;

/**
 *
 * @author rhk
 */
@Verb("POST")
public class PostResourceHandler extends AbstractReflectiveResourceMethodHandler<POST>
        implements ResourceMethodHandler {

    public PostResourceHandler() {
        super(POST.class, QueryMetaData.Kind.UPDATE);
    }
}
