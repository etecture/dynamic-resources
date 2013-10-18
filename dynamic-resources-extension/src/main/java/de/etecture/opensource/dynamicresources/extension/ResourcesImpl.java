package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Resources;
import de.etecture.opensource.dynamicresources.spi.ResourceMethodHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 *
 * @author rhk
 */
public class ResourcesImpl<T> implements Resources<T> {

    private final BeanManager bm;
    private final Class<T> resourceClass;
    protected final Map<String, Object> params;

    protected ResourcesImpl(
            BeanManager bm,
            Class<T> resourceClass,
            Map<String, Object> params) {
        this.bm = bm;
        this.params = params;
        this.resourceClass = resourceClass;
    }

    public ResourcesImpl(
            BeanManager bm, Class<T> resourceClass) {
        this(bm, resourceClass, new HashMap<String, Object>());

    }

    @Override
    public Resources<T> select(
            Map<String, Object> params) {
        return new ResourcesWithParametersImpl<>(bm, resourceClass, params);
    }

    @Override
    public Resources<T> select(String paramName, Object paramValue) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(paramName, paramValue);
        return select(parameters);
    }

    @Override
    public T GET() throws Exception {
        return lookupExecutor(bm, "GET").execute(resourceClass, params, null);
    }

    @Override
    public T PUT(Object content) throws Exception {
        return lookupExecutor(bm, "PUT").execute(resourceClass, params, content);
    }

    @Override
    public T POST(Object content) throws Exception {
        return lookupExecutor(bm, "POST")
                .execute(resourceClass, params, content);
    }

    @Override
    public T POST() throws Exception {
        return POST(null);
    }

    @Override
    public boolean DELETE() throws Exception {
        lookupExecutor(bm, "POST").execute(resourceClass, params, null);
        return true;
    }

    private static ResourceMethodHandler lookupExecutor(BeanManager bm,
            String method) {
        final Set<Bean<?>> beans =
                bm.getBeans(ResourceMethodHandler.class,
                new VerbLiteral(method));
        System.out.println(method + " " + beans.size());
        Bean<ResourceMethodHandler> b = (Bean<ResourceMethodHandler>) bm
                .resolve(
                beans);
        System.out.println(b);
        return b.create(bm
                .createCreationalContext(b));
    }
}
