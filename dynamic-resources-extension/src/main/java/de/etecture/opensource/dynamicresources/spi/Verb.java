package de.etecture.opensource.dynamicresources.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 *
 * @author rhk
 */
@Qualifier
@Inherited
@Target({ElementType.METHOD,
    ElementType.FIELD,
    ElementType.PARAMETER,
    ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Verb {

    String value();
}
