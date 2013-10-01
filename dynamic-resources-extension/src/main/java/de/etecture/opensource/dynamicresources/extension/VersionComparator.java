package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Version;
import java.util.Comparator;

/**
 * compares two version expressions.
 *
 * @author rhk
 */
public class VersionComparator implements
        Comparator<Version> {

    private final boolean ascending;

    public VersionComparator() {
        this(true);
    }

    public VersionComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(Version o1, Version o2) {
        return compare(o1.major(), o1.minor(), o1.release(), o2.major(), o2
                .minor(), o2.release(), ascending);
    }

    public static int compare(String v1, String v2) {
        return compare(v1, v2, true);
    }

    public static int compare(String v1, String v2, boolean ascending) {
        return new VersionComparator(ascending).compare(
                new VersionExpression(
                v1),
                new VersionExpression(v2));
    }

    /**
     * compares two version parts.
     *
     * @param major1
     * @param minor1
     * @param release1
     * @param major2
     * @param minor2
     * @param release2
     * @return
     */
    public static int compare(double major1, double minor1, double release1,
            double major2, double minor2, double release2) {
        return compare(major1, minor1, release1, major2, minor2, release2, true);
    }

    public static int compare(double major1, double minor1, double release1,
            double major2, double minor2, double release2, boolean ascending) {
        if (major1 == major2) {
            if (minor1 == minor2) {
                return Double.compare(release1, release2) * (ascending ? 1 : -1);
            } else {
                return Double.compare(minor1, minor2) * (ascending ? 1 : -1);
            }
        } else {
            return Double.compare(major1, major2) * (ascending ? 1 : -1);
        }

    }
}
