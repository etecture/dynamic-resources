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
