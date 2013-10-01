package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Produces;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class ProducesLiteral extends AnnotationLiteral<Produces> implements
        Produces {

    private static final long serialVersionUID = 1L;
    private final String version;
    private final String[] mimeTypes;

    public ProducesLiteral() {
        this.mimeTypes = new String[]{"text/plain"};
        this.version = String.format("%d.%d.%d", Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public ProducesLiteral(Object object) {
        this.mimeTypes = new String[]{"text/plain"};
        this.version = String.format("%d.%d.%d", Integer.MAX_VALUE,
                Integer.MAX_VALUE, System.identityHashCode(object));
    }

    public ProducesLiteral(Produces produces) {
        this(produces.mimeType(), produces.version());
    }

    public ProducesLiteral(String[] mimeTypes, String versionString) {
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
}
