package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Version;
import de.etecture.opensource.dynamicresources.api.VersionNumberRange;
import de.etecture.opensource.dynamicresources.spi.VersionNumberResolver;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.TreeMap;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @param <T>
 * @param <A>
 * @author rhk
 */
public abstract class AbstractMimeAndVersionResolver<T, A extends Annotation> {

    @Inject
    BeanManager beanManager;
    @Inject
    VersionNumberResolver versionNumberResolver;
    private final Class<T> beanClass;
    private final Class<A> annotationClass;

    protected AbstractMimeAndVersionResolver(
            Class<T> beanClass, Class<A> annotationClass) {
        this.beanClass = beanClass;
        this.annotationClass = annotationClass;
    }

    protected abstract Class<?> getTypeDefinition(A annotation);

    protected abstract String[] getMimeType(A annotation);

    protected abstract String getVersion(A annotation);

    public T resolve(Class<?> resourceClass,
            MediaType mimeType,
            VersionNumberRange versionMatcher) {
        final Set<Bean<?>> beans = beanManager.getBeans(beanClass,
                new AnnotationLiteral<Any>() {
            private static final long serialVersionUID = 1L;
        });
        System.out.printf(
                "selected mimetype: %s, selected version: %s, beans: %s, entity: %s%n",
                mimeType,
                versionMatcher == null ? "not specified" : versionMatcher
                .toString(),
                beans.size(), resourceClass.getName());
        // (1) Build the map of versioned beans.
        TreeMap<Version, Bean<T>> versionedBeans =
                new TreeMap<>(new VersionComparator(false));
        outer:
        for (Bean<?> bean : beans) {
            for (Annotation qualifier : bean.getQualifiers()) {
                if (qualifier.annotationType() == annotationClass) {
                    A annotation = annotationClass.cast(qualifier);
                    if (!getTypeDefinition(annotation).isAssignableFrom(
                            resourceClass)) {
                        continue outer;
                    } else {
                        if (mimeType != null && mimeType.isCompatibleTo(
                                getMimeType(annotation))) {
                            if (StringUtils.isNotBlank(getVersion(annotation))) {
                                // found a mimetype and a version, so add the bean with this version
                                versionedBeans.put(new VersionExpression(
                                        getVersion(annotation)),
                                        (Bean<T>) bean);
                            } else {
                                versionedBeans.put(
                                        new VersionExpression(bean),
                                        (Bean<T>) bean);
                            }
                        }
                        continue outer;
                    }
                }
            }
            // add the bean only, if the mimeType is not specified or text/plain.
            versionedBeans.put(
                    new VersionExpression(bean),
                    (Bean<T>) bean);
        }
        System.out.println("Versions are: ");
        for (Version v : versionedBeans.keySet()) {
            System.out.println(v.toString());
        }
        // (2) now resolve the correct bean for the correct version
        Bean<T> resolved = versionNumberResolver.resolve(
                versionedBeans, versionMatcher);
        if (resolved != null) {
            return resolved.create(beanManager
                    .createCreationalContext(resolved));
        }
        return null;
    }
}
