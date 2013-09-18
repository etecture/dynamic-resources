package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 *
 * @author rhk
 */
public class TestResourceInterceptor implements ResourceInterceptor {

    @Override
    public Response before(String method, Resource resource,
            Class<?> resourceClass,
            Map<String, Object> parameter) {
        System.out.printf("BEFORE: %s %s for: %s%n", method,
                resource.value(), resourceClass.getSimpleName());
        for (Map.Entry<String, Object> entry : parameter.entrySet()) {
            System.out.printf("\t%s = %s%n", entry.getKey(), entry.getValue());
        }
        return null;
    }

    @Override
    public Response after(String method, Resource resource,
            Class<?> resourceClass,
            Map<String, Object> parameter, Response response) {
        System.out.printf("AFTER: %s %s for: %s%n", method,
                resource.value(), resourceClass.getSimpleName());
        for (Map.Entry<String, Object> entry : parameter.entrySet()) {
            System.out.printf("\t%s = %s%n", entry.getKey(), entry.getValue());
        }
        System.out.printf("\tStatus : %d%n", response.getStatus());
        System.out.printf("\tEntity : %s%n", response.getEntity());
        return response;
    }
}
