package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.XMLWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 */
public class XMLWriterBean implements Bean<XMLWriter> {

    private final XMLWriter instance;
    private final Class<? extends Object> entityClass;

    public XMLWriterBean(
            Class<? extends Object> entityClass,
            XMLWriter instance) {
        this.entityClass = entityClass;
        this.instance = instance;
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<>();
        types.add(XMLWriter.class);
        types.add(Object.class);
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(new AnnotationLiteral<Any>() {
        });
        qualifiers.add(new ForEntityLiteral(entityClass));
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
        return XMLWriter.class;
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
    public XMLWriter create(CreationalContext ctx) {
        return instance;
    }

    @Override
    public void destroy(XMLWriter instance,
            CreationalContext ctx) {
        ctx.release();
    }
}
