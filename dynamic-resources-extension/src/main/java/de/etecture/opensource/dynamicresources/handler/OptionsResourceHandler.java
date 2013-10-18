package de.etecture.opensource.dynamicresources.handler;

import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import de.etecture.opensource.dynamicresources.spi.Verb;
import java.io.IOException;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author rhk
 */
@Verb("OPTIONS")
public class OptionsResourceHandler implements ResourceMethodHandler {

    @Inject
    @Any
    Instance<ResourceMethodHandler> resourceMethodHandlers;

    @Override
    public boolean isAvailable(
            Class<?> resourceClazz) {
        return true;
    }

    @Override
    public <T> void handleRequest(
            Class<T> resourceClazz,
            Map<String, String> pathValues, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setStatus(200);
        response.setContentType("text/plain");

        for (ResourceMethodHandler handler : resourceMethodHandlers) {
            if (handler.isAvailable(resourceClazz)) {
                response.getWriter().printf("%s%n\t- %s%n", handler.getClass()
                        .getAnnotation(Verb.class).value(), handler
                        .getDescription(resourceClazz));
            }
        }
        response.getWriter().flush();
    }

    @Override
    public String getDescription(
            Class<?> resourceClazz) {
        return String.format(
                "Returns the HTTP methods that the server supports for the resource: %s",
                resourceClazz.getSimpleName());
    }


    @Override
    public <T> T execute(
            Class<T> resourceClazz,
            Map<String, Object> params, Object request) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
