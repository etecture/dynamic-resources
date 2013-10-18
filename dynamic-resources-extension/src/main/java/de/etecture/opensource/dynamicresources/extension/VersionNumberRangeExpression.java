/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Version;
import de.etecture.opensource.dynamicresources.api.VersionNumberRange;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * represents a version number expression, as explained in
 * {@link DefaultVersionNumberResolver}
 *
 * @author rhk
 */
public class VersionNumberRangeExpression implements VersionNumberRange {

    public final static Pattern versionRangePattern = Pattern.compile(
            ""
            + "(?:(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<release>\\d+))?)?)"
            + "|"
            + "(?<lowerIncEx>\\[|\\]|\\()"
            + "(?:(?<lowerMajor>\\d+)(?:\\.(?<lowerMinor>\\d+)(?:\\.(?<lowerRelease>\\d+))?)?)?"
            + "(?:,"
            + "(?:(?<upperMajor>\\d+)(?:\\.(?<upperMinor>\\d+)(?:\\.(?<upperRelease>\\d+))?)?)?"
            + "(?<upperIncEx>\\[|\\]|\\)))?");
    final int upperMajor, upperMinor, upperRelease;
    final int lowerMajor, lowerMinor, lowerRelease;
    final boolean upperInclusive, lowerInclusive;

    public VersionNumberRangeExpression(Version version) {
        if (version == null) {
            this.lowerInclusive = false;
            this.lowerMajor = 0;
            this.lowerMinor = 0;
            this.lowerRelease = 0;
            this.upperInclusive = false;
            this.upperMajor = Integer.MAX_VALUE;
            this.upperMinor = Integer.MAX_VALUE;
            this.upperRelease = Integer.MAX_VALUE;
        } else {
            this.lowerInclusive = true;
            this.lowerMajor = version.major();
            this.lowerMinor = version.minor();
            this.lowerRelease = version.release();
            this.upperInclusive = true;
            this.upperMajor = lowerMajor;
            this.upperMinor = lowerMinor;
            this.upperRelease = lowerRelease;
        }
    }

    public VersionNumberRangeExpression(Version lower, boolean lowerInclusive,
            Version upper, boolean upperInclusive) {
        this(lower.major(), lower.minor(), lower.release(), lowerInclusive,
                upper.major(), upper.minor(), upper.release(), upperInclusive);
    }

    /**
     * constructs version range expression
     *
     * @param lowerMajor
     * @param lowerMinor
     * @param lowerRelease
     * @param lowerInclusive
     * @param upperMajor
     * @param upperMinor
     * @param upperRelease
     * @param upperInclusive
     */
    public VersionNumberRangeExpression(int lowerMajor, int lowerMinor,
            int lowerRelease, boolean lowerInclusive, int upperMajor,
            int upperMinor, int upperRelease, boolean upperInclusive) {
        this.upperMajor = upperMajor;
        this.upperMinor = upperMinor;
        this.upperRelease = upperRelease;
        this.upperInclusive = upperInclusive;
        this.lowerMajor = lowerMajor;
        this.lowerMinor = lowerMinor;
        this.lowerRelease = lowerRelease;
        this.lowerInclusive = lowerInclusive;
    }

    public VersionNumberRangeExpression(int lowerMajor, int lowerMinor,
            int lowerRelease, boolean lowerInclusive) {
        this.upperMajor = Integer.MAX_VALUE;
        this.upperMinor = Integer.MAX_VALUE;
        this.upperRelease = Integer.MAX_VALUE;
        this.upperInclusive = false;
        this.lowerMajor = lowerMajor;
        this.lowerMinor = lowerMinor;
        this.lowerRelease = lowerRelease;
        this.lowerInclusive = lowerInclusive;
    }

    public VersionNumberRangeExpression(String lowerBound,
            boolean lowerInclusive, String upperBound, boolean upperInclusive) {
        Matcher matcher = VersionExpression.versionPattern.matcher(lowerBound);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "lowerBound must follow the versionPattern!");
        }
        this.lowerMajor = NumberUtils.toInt(matcher
                .group("major"), Integer.MAX_VALUE);
        this.lowerMinor = NumberUtils.toInt(matcher
                .group("minor"), Integer.MAX_VALUE);
        this.lowerRelease = NumberUtils.toInt(matcher.group(
                "release"), Integer.MAX_VALUE);
        this.lowerInclusive = lowerInclusive;

        matcher = VersionExpression.versionPattern.matcher(upperBound);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "upperBound must follow the versionPattern!");
        }
        this.upperMajor = NumberUtils.toInt(matcher
                .group("major"), Integer.MAX_VALUE);
        this.upperMinor = NumberUtils.toInt(matcher
                .group("minor"), Integer.MAX_VALUE);
        this.upperRelease = NumberUtils.toInt(matcher.group(
                "release"), Integer.MAX_VALUE);
        this.upperInclusive = upperInclusive;

    }

    public VersionNumberRangeExpression(String versionRangeExpression) {
        Matcher matcher = versionRangePattern.matcher(versionRangeExpression);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "versionRangeExpression must follow the versionRangePattern!");
        }
        String lowerIncEx = matcher.group("lowerIncEx");
        if (StringUtils.isBlank(lowerIncEx)) {
            // it's a concrete version
            this.lowerInclusive = true;
            this.lowerMajor = NumberUtils.toInt(matcher
                    .group("major"), 0);
            this.lowerMinor = NumberUtils.toInt(matcher
                    .group("minor"), 0);
            this.lowerRelease = NumberUtils.toInt(matcher.group(
                    "release"), 0);
            this.upperInclusive = true;
            this.upperMajor = this.lowerMajor;
            this.upperMinor = this.lowerMinor;
            this.upperRelease = this.lowerRelease;
        } else {
            // it's a range.
            this.lowerInclusive = !("(".equals(lowerIncEx) || "]".equals(
                    lowerIncEx));
            this.lowerMajor = NumberUtils.toInt(matcher
                    .group("lowerMajor"), 0);
            this.lowerMinor = NumberUtils.toInt(matcher
                    .group("lowerMinor"), 0);
            this.lowerRelease = NumberUtils.toInt(matcher.group(
                    "lowerRelease"), 0);

            String upperIncEx = matcher.group("upperIncEx");
            this.upperInclusive = !(")".equals(upperIncEx) || "[".equals(
                    upperIncEx));
            this.upperMajor = NumberUtils.toInt(matcher
                    .group("upperMajor"), Integer.MAX_VALUE);
            this.upperMinor = NumberUtils.toInt(matcher
                    .group("upperMinor"),
                    upperInclusive ? Integer.MAX_VALUE : 0);
            this.upperRelease = NumberUtils.toInt(matcher.group(
                    "upperRelease"),
                    upperInclusive ? Integer.MAX_VALUE : 0);
        }

    }

    @Override
    public boolean includes(Version version) {
        return matches(version.major(), version.minor(), version
                .release());
    }

    @Override
    public boolean includes(String expression) {
        return includes(new VersionExpression(expression));
    }

    public boolean matches(int major, int minor, int release) {
        boolean matchesLowerBound;
        if (lowerInclusive) {
            matchesLowerBound = (VersionComparator.compare(lowerMajor,
                    lowerMinor, lowerRelease, major, minor,
                    release) <= 0);
        } else {
            matchesLowerBound = (VersionComparator.compare(lowerMajor,
                    lowerMinor, lowerRelease, major, minor,
                    release) < 0);

        }
        boolean matchesUpperBound;
        if (upperInclusive) {
            matchesUpperBound = (VersionComparator
                    .compare(major, minor, release, upperMajor, upperMinor,
                    upperRelease) <= 0);
        } else {
            matchesUpperBound = (VersionComparator
                    .compare(major, minor, release, upperMajor, upperMinor,
                    upperRelease) < 0);
        }
        return matchesLowerBound && matchesUpperBound;
    }

    @Override
    public boolean isLowerInclusive() {
        return lowerInclusive;
    }

    @Override
    public boolean isUpperInclusive() {
        return upperInclusive;
    }

    @Override
    public Version getLower() {
        return new VersionExpression(lowerMajor, lowerMinor, lowerRelease);
    }

    @Override
    public Version getUpper() {
        return new VersionExpression(upperMajor, upperMinor, upperRelease);
    }

    @Override
    public boolean isConcrete() {
        return upperMajor == lowerMajor && upperMinor == lowerMinor
                && upperRelease
                == lowerRelease;
    }

    @Override
    public boolean isInfinite() {
        return lowerMajor == 0 && lowerMinor == 0 && lowerRelease == 0
                && upperMajor == Integer.MAX_VALUE && upperMinor
                == Integer.MAX_VALUE && upperRelease == Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isConcrete()) {
            sb.append(upperMajor);
            sb.append(".");
            sb.append(upperMinor);
            sb.append(".");
            sb.append(upperRelease);
        } else {
            if (!lowerInclusive) {
                if (lowerMajor == 0 && lowerMinor == 0
                        && lowerRelease == 0) {
                    sb.append("(");
                } else {
                    sb.append("]");
                    sb.append(lowerMajor);
                    sb.append(".");
                    sb.append(lowerMinor);
                    sb.append(".");
                    sb.append(lowerRelease);
                }
            } else {
                sb.append("[");
                sb.append(lowerMajor);
                sb.append(".");
                sb.append(lowerMinor);
                sb.append(".");
                sb.append(lowerRelease);
            }
            sb.append(",");
            if (!upperInclusive) {
                if (upperMajor == Integer.MAX_VALUE && upperMinor
                        == Integer.MAX_VALUE && upperRelease
                        == Integer.MAX_VALUE) {
                    sb.append(")");
                } else {
                    sb.append(upperMajor);
                    sb.append(".");
                    sb.append(upperMinor);
                    sb.append(".");
                    sb.append(upperRelease);
                    sb.append("[");
                }
            } else {
                sb.append(upperMajor);
                sb.append(".");
                sb.append(upperMinor);
                sb.append(".");
                sb.append(upperRelease);
                sb.append("]");
            }
        }
        return sb.toString();
    }
}
