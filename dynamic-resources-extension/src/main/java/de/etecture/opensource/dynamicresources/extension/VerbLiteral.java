package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.spi.Verb;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class VerbLiteral extends AnnotationLiteral<Verb> implements Verb {

    private static final long serialVersionUID = 1L;
    private final String value;

    public VerbLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return this.value;
    }
}
