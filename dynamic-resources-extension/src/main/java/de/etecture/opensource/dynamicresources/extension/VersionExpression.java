package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Version;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.math.NumberUtils;

/**
 *
 * @author rhk
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class VersionExpression implements Comparable<VersionExpression>, Version {

    public static final Pattern versionPattern = Pattern.compile(
            "(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<release>\\d+))?)?");
    private static final long serialVersionUID = 1L;
    private final int major, minor, release;

    public VersionExpression() {
        this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public VersionExpression(Object object) {
        this(Integer.MAX_VALUE, Integer.MAX_VALUE, System.identityHashCode(
                object));
    }

    public VersionExpression(int major, int minor) {
        this(major, minor, 0);
    }

    public VersionExpression(int major, int minor, int release) {
        this.major = major;
        this.minor = minor;
        this.release = release;
    }

    public VersionExpression(String versionString) {
        Matcher matcher = versionPattern.matcher(versionString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "version must follow the versionPattern!");
        }
        this.major = NumberUtils.toInt(matcher.group("major"), 0);
        this.minor = NumberUtils.toInt(matcher.group("minor"), 0);
        this.release = NumberUtils.toInt(matcher.group("release"), 0);
    }

    @Override
    public int major() {
        return major;
    }

    @Override
    public int minor() {
        return minor;
    }

    @Override
    public int release() {
        return release;
    }

    @Override
    public int compareTo(VersionExpression o) {
        return new VersionComparator().compare(this, o);
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, release);
    }
}
