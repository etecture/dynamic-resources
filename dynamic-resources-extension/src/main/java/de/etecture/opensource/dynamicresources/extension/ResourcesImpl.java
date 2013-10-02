package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Resource;
import de.etecture.opensource.dynamicresources.api.Resources;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

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
    public T GET() {
        return resourceClass.cast(lookupExecutor(bm).GET(resourceClass
                .getAnnotation(
                Resource.class), resourceClass, params).getEntity());
    }

    @Override
    public T PUT(T content) {
        return resourceClass.cast(lookupExecutor(bm).PUT(resourceClass
                .getAnnotation(
                Resource.class), resourceClass, params, content).getEntity());
    }

    @Override
    public T POST(T content) {
        return resourceClass.cast(lookupExecutor(bm).POST(resourceClass
                .getAnnotation(
                Resource.class), resourceClass, params, content).getEntity());
    }

    @Override
    public T POST() {
        return POST(null);
    }

    @Override
    public boolean DELETE() {
        return lookupExecutor(bm).DELETE(resourceClass.getAnnotation(
                Resource.class), resourceClass, params).getStatus() == 200;
    }

    private static DynamicResourceExecutor lookupExecutor(BeanManager bm) {
        Bean<DynamicResourceExecutor> b = (Bean<DynamicResourceExecutor>) bm
                .resolve(
                bm.getBeans(DynamicResourceExecutor.class,
                new AnnotationLiteral<Default>() {
            private static final long serialVersionUID = 1L;
        }));
        return b.create(bm
                .createCreationalContext(b));
    }
}
