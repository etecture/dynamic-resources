package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Resources;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @param <T>
 * @author rhk
 */
public class ResourcesBean<T> implements Bean<Resources<T>> {

    private final Type resourcesType;
    private final Class<Resources<T>> resourcesClass;
    private final Resources<T> resourcesImpl;

    public ResourcesBean(Type resourcesType, Resources<T> resourcesImpl) {
        this.resourcesType = resourcesType;
        this.resourcesImpl = resourcesImpl;
        this.resourcesClass = (Class<Resources<T>>) resourcesImpl.getClass();
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<>();
        types.add(resourcesType);
        types.add(resourcesClass.getGenericSuperclass());
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(new AnnotationLiteral<Default>() {
            private static final long serialVersionUID = 1L;
        });
        qualifiers.add(new AnnotationLiteral<Any>() {
            private static final long serialVersionUID = 1L;
        });
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Class<?> getBeanClass() {
        return resourcesClass;
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Resources<T> create(
            CreationalContext<Resources<T>> creationalContext) {
        return resourcesImpl;
    }

    @Override
    public void destroy(
            Resources<T> instance,
            CreationalContext<Resources<T>> creationalContext) {
        creationalContext.release();
    }

    @Override
    public String toString() {
        return String.format("ResourcesBean for type: %s", resourcesType);
    }
}
