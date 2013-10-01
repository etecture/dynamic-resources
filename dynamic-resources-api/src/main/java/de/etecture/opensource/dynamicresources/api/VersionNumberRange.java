package de.etecture.opensource.dynamicresources.api;

/**
 *
 * @author rhk
 */
public interface VersionNumberRange {

    boolean includes(String expression);

    boolean includes(Version version);

    Version getLower();

    Version getUpper();

    boolean isConcrete();

    boolean isLowerInclusive();

    boolean isUpperInclusive();

    boolean isInfinite();
}
