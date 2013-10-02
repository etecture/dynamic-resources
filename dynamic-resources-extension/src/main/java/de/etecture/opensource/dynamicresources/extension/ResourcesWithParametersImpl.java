package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Resources;
import java.util.Map;
import javax.enterprise.inject.spi.BeanManager;

/**
 *
 * @param <T>
 * @author rhk
 */
public class ResourcesWithParametersImpl<T> extends ResourcesImpl<T> {


    public ResourcesWithParametersImpl(BeanManager bm,
            Class<T> resourceClass, Map<String, Object> parameter) {
        super(bm, resourceClass, parameter);
    }

    @Override
    public Resources<T> select(
            Map<String, Object> params) {
        params.putAll(params);
        return this;
    }

    @Override
    public Resources<T> select(String paramName, Object paramValue) {
        this.params.put(paramName, paramValue);
        return this;
    }

}
