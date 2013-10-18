package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Consumes;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class ConsumesLiteral extends AnnotationLiteral<Consumes> implements
        Consumes {

    private static final long serialVersionUID = 1L;
    private final String version;
    private final String[] mimeTypes;
    private final Class requestType;

    public ConsumesLiteral(Class requestType) {
        this.requestType = requestType;
        this.mimeTypes = new String[]{"text/plain"};
        this.version = String.format("%d.%d.%d", Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public ConsumesLiteral(Object object) {
        this.requestType = object.getClass();
        this.mimeTypes = new String[]{"text/plain"};
        this.version = String.format("%d.%d.%d", Integer.MAX_VALUE,
                Integer.MAX_VALUE, System.identityHashCode(object));
    }

    public ConsumesLiteral(Consumes consumes) {
        this(consumes.requestType(), consumes.mimeType(), consumes.version());
    }

    public ConsumesLiteral(Class requestType, String[] mimeTypes,
            String versionString) {
        this.requestType = requestType;
        this.mimeTypes = mimeTypes;
        this.version = versionString;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public String[] mimeType() {
        return mimeTypes;
    }

    @Override
    public Class requestType() {
        return requestType;
    }
}
