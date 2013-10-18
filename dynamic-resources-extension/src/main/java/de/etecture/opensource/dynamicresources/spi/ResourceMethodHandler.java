package de.etecture.opensource.dynamicresources.spi;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * handles the execution of queries for a given method.
 *
 * @author rhk
 */
public interface ResourceMethodHandler {

    /**
     * checks, if the given method is available for the specified resource.
     *
     * @param resourceClazz
     * @return
     */
    boolean isAvailable(Class<?> resourceClazz);

    /**
     * returns the description of the method execution in the resource.
     *
     * @param resourceClazz
     * @return
     */
    String getDescription(Class<?> resourceClazz);

    /**
     * handles a request for the given method.
     *
     * @param <T> resource type
     * @param resourceClazz the class of the (content-)resource
     * @param pathValues the path values extracted from the request path
     * @param request the servlet reques
     * @param response the servlet response
     * @throws IOException
     */
    <T> void handleRequest(
            Class<T> resourceClazz,
            Map<String, String> pathValues,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException;

    /**
     * executes the query in the resource class for the given method.
     *
     * @param <T>
     * @param resourceClazz
     * @param params
     * @param request
     * @return
     * @throws Exception
     */
    <T> T execute(Class<T> resourceClazz, Map<String, Object> params,
            Object request) throws Exception;
}
