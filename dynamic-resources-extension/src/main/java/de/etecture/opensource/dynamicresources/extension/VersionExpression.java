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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
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
        if (StringUtils.isEmpty(versionString)) {
            versionString = "0.0.0";
        }
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
