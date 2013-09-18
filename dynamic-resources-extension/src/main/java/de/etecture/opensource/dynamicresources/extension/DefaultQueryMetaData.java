package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicrepositories.api.Generator;
import de.etecture.opensource.dynamicrepositories.api.Param;
import de.etecture.opensource.dynamicrepositories.api.ResultConverter;
import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.beanutils.ConvertUtils;

/**
 *
 * @author rhk
 */
public class DefaultQueryMetaData<T> implements QueryMetaData<T> {

    private final Map<String, Object> parameters = new HashMap<>();
    private final String queryName;
    private final String query;
    private final Class<T> queryType;
    private final Kind queryKind;

    public DefaultQueryMetaData(Class<T> queryType, Kind queryKind, String query,
            String queryName, Map<String, Object> parameters) {
        this.queryType = queryType;
        this.queryKind = queryKind;
        this.query = query;
        this.queryName = queryName;
        this.parameters.putAll(parameters);
    }

    @Override
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    @Override
    public Map<String, Object> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public Object getParameterValue(String parameterName) {
        return parameters.get(parameterName);
    }

    @Override
    public int getOffset() {
        return -1;
    }

    @Override
    public int getCount() {
        return -1;
    }

    @Override
    public String getQueryName() {
        return queryName;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Class<T> getQueryType() {
        return queryType;
    }

    @Override
    public Kind getQueryKind() {
        return queryKind;
    }

    @Override
    public Exception createException(
            Class<? extends Annotation> qualifier, String message,
            Exception cause) {
        return cause;
    }

    @Override
    public ResultConverter<T> getConverter() {
        return null;
    }

    @Override
    public Type getQueryGenericType() {
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
    public Class<?> getRepositoryClass() {
        return null;
    }

    public void addParameter(Param param) {
        if (param.generator().getName().equals(Generator.class.getName())) {
            final String value = param.value();
            if ("$$$generated$$$".equals(value)) {
                throw new IllegalArgumentException(String.format(
                        "Either generator or value must be specified for parameter defintion '%s'!",
                        param.name()));
            }
            parameters.put(param.name(), ConvertUtils.convert(value, param
                    .type()));
        } else {
            try {
                final Generator generator = param.generator().newInstance();
                parameters.put(param.name(), ConvertUtils.convert(generator
                        .generateValue(param), param.type()));
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException(
                        "The generator cannot be instantiated. ", ex);
            }
        }
    }
}
