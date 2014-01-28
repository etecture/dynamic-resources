package de.etecture.opensource.dynamicresources.spi;

import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.etecture.opensource.dynamicresources.api.Request;
import de.etecture.opensource.dynamicresources.api.ResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.UriBuilder;
import java.util.Map;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

/**
 * redirects the POST method response.
 *
 * @param <R> the class of the resource where the POST method must be redirected
 * @param <N> the class of the resource to be redirected to
 * @author rhk
 * @version ${project.version}
 * @since 1.0.5
 */
public abstract class AbstractPostRedirectInterceptor<R, N> implements
        ResourceInterceptor {

    @Inject
    @Default
    UriBuilder uriBuilder;
    private final Class<R> originClass;
    private final Class<N> targetClass;

    protected AbstractPostRedirectInterceptor(Class<R> originClass,
            Class<N> targetClass) {
        this.originClass = originClass;
        this.targetClass = targetClass;
    }

    @Override
    public Response before(Request request) {
        return null; // go on.
    }

    @Override
    public Response after(Request request, Response response) {
        final Object entity;
        try {
            entity = response.getEntity();
        } catch (Throwable ex) {
            return response;
        }
        // check if responsible
        if (response.getStatus() == StatusCodes.CREATED && request
                .getResourceClass() == originClass && entity != null
                && originClass
                .isAssignableFrom(entity.getClass())
                && HttpMethods.POST
                .equalsIgnoreCase(request.getMethodName())) {
            final R responseEntity = originClass.cast(entity);
            // build a new response with the new resource location
            Map<String, String> pathValues = buildPathValues(responseEntity);
            if (pathValues == null) {
                // if the response entity is not correct, it's an
                // internal server error.
                return new DefaultResponse("Unable to redirect!",
                        StatusCodes.INTERNAL_SERVER_ERROR);
            } else {
                DefaultResponse newResponse = new DefaultResponse(null,
                        StatusCodes.SEE_OTHER);
                newResponse.addHeader("Location", uriBuilder.build(targetClass,
                        pathValues));
                return newResponse;
            }
        } else {
            // otherwise, return the original response
            return response;
        }
    }

    protected abstract Map<String, String> buildPathValues(R entity);
}
