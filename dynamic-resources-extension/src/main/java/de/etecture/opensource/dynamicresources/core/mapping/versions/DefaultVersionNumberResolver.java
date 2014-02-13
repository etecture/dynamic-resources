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
package de.etecture.opensource.dynamicresources.core.mapping.versions;

import de.etecture.opensource.dynamicresources.core.mapping.versions.VersionNumberRangeExpression;
import de.etecture.opensource.dynamicresources.core.mapping.versions.VersionComparator;
import de.etecture.opensource.dynamicresources.api.Version;
import de.etecture.opensource.dynamicresources.api.VersionNumberRange;
import java.util.Map;
import java.util.TreeMap;
import javax.enterprise.inject.Default;

/**
 * resolves a concrete object by comparing the given versioned objects with the
 * version expression.
 * <p>
 * A version expression is either a concrete version number (e.g. "1.0.1") or a
 * version number range (e.g. "[1.0.1,2.0[").
 * <p>
 * Note: If more than one object includes the version expression, the newest one
 * will be returned. For example: <br>
 * <pre>
 * Expression is: [1.0.1,2.0[
 * Matched versions are: 1.0.1, 1.0.2, 1.1.0
 * Not-Matched versions are: 2.0, 2.0.1, 2.1
 * Returned is version: 1.1.0, due to it is the newest matched version.
 * </pre>
 * <p>
 * A version number range must follow the expression:
 * <pre>
 * &lt;range&gt; = &lt;start&gt;? &lt;version_range&gt; &lt;end&gt;?
 * &lt;start&gt; = &lt;inclusive_start&gt; | &lt;exclusive_start&gt;
 * &lt;end&gt; = &lt;inclusive_end&gt; | &lt;exclusive_end&gt;
 * &lt;version_range&gt; = &lt;concrete_version&gt; (&lt;COMMA&gt; &lt;concrete_version&gt;)?
 * &lt;concrete_version&gt; = &lt;major&gt; (&lt;DOT&gt; &lt;minor&gt; (&lt;DOT&gt; &lt;release&gt;)? )?
 * &lt;major&gt; = &lt;DIGIT&gt;+
 * &lt;minor&gt; = &lt;DIGIT&gt;+
 * &lt;release&gt; = &lt;DIGIT&gt;+
 * &lt;inclusive_start&gt; = '['
 * &lt;inclusive_end&gt; = ']'
 * &lt;exclusive_start&gt; = '(' | ']'
 * &lt;exclusive_end&gt; = ')' | '['
 * </pre>
 * <p>
 * Examples:
 * <table>
 * <tr>
 * <th>Expression</th><th>Matches</th>
 * </tr>
 * <tr>
 * <td>[1.0,2.0]</td></td>versions greater or equal to 1.0 and lower or equal to
 * 2.0</td>
 * </tr>
 * <tr>
 * <td>[1.0,2.0[</td></td>all versions greater or equal to 1.0 and lower than
 * 2.0</td>
 * </tr>
 * <tr>
 * <td>]1.0,2.0]</td></td>all versions greater than 1.0 and lower or equal to
 * 2.0</td>
 * </tr>
 * <tr>
 * <td>]1.0,2.0[</td></td>all versions greater than 1.0 and lower than 2.0</td>
 * </tr>
 * <tr>
 * <td>[1.0,)</td></td>all versions greater or equal to 1.0</td>
 * </tr>
 * <tr>
 * <td>]1.0,)</td></td>all versions greater than 1.0</td>
 * </tr>
 * <tr>
 * <td>(,2.0]</td></td>all versions lower or equal to 2.0</td>
 * </tr>
 * <tr>
 * <td>(,2.0[</td></td>all versions lower than 2.0</td>
 * </tr>
 * </table>
 *
 * @author rhk
 * @see VersionNumberResolver
 */
@Default
public class DefaultVersionNumberResolver implements VersionNumberResolver {

    @Override
    public <T> T resolve(Map<Version, T> objects,
            String versionExpression) {
        // (1) return null if the map is empty
        if (objects.isEmpty()) {
            return null;
        }

        // (2) reduce the map to get only matched versions.
        VersionNumberRangeExpression exp = new VersionNumberRangeExpression(
                versionExpression);
        return resolve(objects, exp);
    }

    @Override
    public <T> T resolve(
            Map<Version, T> objects, VersionNumberRange exp) {
        TreeMap<Version, T> reducedMap = new TreeMap<>(
                new VersionComparator(
                false));
        for (Map.Entry<Version, T> e : objects.entrySet()) {
            if (exp.includes(e.getKey())) {
                reducedMap.put(e.getKey(), e.getValue());
            }
        }

        // (3) return the first of the map or null if the map is empty
        if (reducedMap.isEmpty()) {
            return null;
        } else {
            return reducedMap.firstEntry().getValue();
        }
    }
}
