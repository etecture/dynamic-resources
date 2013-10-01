package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Entity;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class EntityLiteral extends AnnotationLiteral<Entity> implements
        Entity {

    private static final long serialVersionUID = 1L;
    private final Class<? extends Object> entityClass;

    public EntityLiteral(Entity entity) {
        this.entityClass = entity.value();
    }

    public EntityLiteral(Class<? extends Object> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Class<? extends Object> value() {
        return this.entityClass;
    }
}
