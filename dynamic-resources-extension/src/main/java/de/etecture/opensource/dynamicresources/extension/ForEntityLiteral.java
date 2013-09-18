package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.ForEntity;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 */
public class ForEntityLiteral extends AnnotationLiteral<ForEntity> implements
        ForEntity {

    private static final long serialVersionUID = 1L;
    private final Class<? extends Object> entityClass;

    public ForEntityLiteral(Class<? extends Object> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Class<? extends Object> value() {
        return this.entityClass;
    }
}
