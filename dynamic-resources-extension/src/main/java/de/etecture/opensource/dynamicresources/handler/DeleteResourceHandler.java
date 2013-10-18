package de.etecture.opensource.dynamicresources.handler;

import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import de.etecture.opensource.dynamicresources.api.DELETE;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.spi.AbstractReflectiveResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.Verb;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author rhk
 */
@Verb("DELETE")
public class DeleteResourceHandler extends AbstractReflectiveResourceMethodHandler<DELETE> {

    public DeleteResourceHandler() {
        super(DELETE.class, QueryMetaData.Kind.DELETE);
    }

    @Override
    public <T> void handleRequest(
            Class<T> resourceClazz,
            Map<String, String> pathValues, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        executeQuery(resourceClazz, buildMetaData(resourceClazz, pathValues,
                request));
        writeResponse(request, response, new Response(null, getDefaultStatus(
                resourceClazz)));
    }
}
