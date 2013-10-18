package de.etecture.opensource.dynamicresources.spi;

import de.etecture.opensource.dynamicrepositories.spi.QueryMetaData;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @param <A>
 * @author rhk
 */
public abstract class AbstractReflectiveResourceMethodHandler<A extends Annotation>
        extends AbstractResourceMethodHandler {

    private final Class<A> annotationClass;

    protected AbstractReflectiveResourceMethodHandler(Class<A> annotationClass,
            QueryMetaData.Kind kind) {
        super(annotationClass.getSimpleName(), kind);
        this.annotationClass = annotationClass;
    }

    @Override
    public boolean isAvailable(
            Class<?> resourceClazz) {
        System.out.println("check, if " + annotationClass.getSimpleName()
                + " is available for " + resourceClazz.getSimpleName());
        return resourceClazz.isAnnotationPresent(annotationClass);
    }

    private A getAnnotation(Class<?> resourceClass) {
        return resourceClass.getAnnotation(annotationClass);
    }

    private <T> T getAnnotationValue(String name, Class<?> resourceClass) {
        try {
            return (T) this.annotationClass.getMethod(name).invoke(
                    getAnnotation(
                    resourceClass));
        } catch (NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException ex) {
            throw new RuntimeException(
                    String.format(
                    "bad annotation class for reflective resource method handler! Does not specify a method: %s()!",
                    name),
                    ex);
        }
    }

    @Override
    public String getDescription(
            Class<?> resourceClass) {
        return getAnnotationValue("description", resourceClass);
    }

    @Override
    protected String getTechnology(Class<?> resourceClass) {
        return getAnnotationValue("technology", resourceClass);
    }

    @Override
    protected int getDefaultStatus(Class<?> resourceClass) {
        return getAnnotationValue("status", resourceClass);
    }

    @Override
    protected String getQuery(
            Class<?> resourceClass) {
        return getAnnotationValue("query", resourceClass);
    }

    @Override
    protected String getQueryName(
            Class<?> resourceClass) {
        String queryName = getAnnotationValue("queryName", resourceClass);
        if (StringUtils.isBlank(queryName)) {
            return getClass().getAnnotation(Verb.class).value();
        }
        return queryName;
    }

    @Override
    protected Class<?> getRequestType(
            Class<?> resourceClazz) {
        Class<?> clazz = getAnnotationValue("requestType", resourceClazz);
        if (clazz == Class.class) {
            return resourceClazz;
        } else {
            return clazz;
        }
    }
}
