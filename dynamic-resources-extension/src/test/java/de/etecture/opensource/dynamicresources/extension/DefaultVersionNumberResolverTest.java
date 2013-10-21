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
import de.etecture.opensource.dynamicresources.spi.VersionNumberResolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author rhk
 */
public class DefaultVersionNumberResolverTest {

    VersionNumberResolver resolver = new DefaultVersionNumberResolver();

    @Test
    public void testVersionNumberResolving() {
        Map<Version, String> testCandidates = new HashMap<>();

        testCandidates.put(new VersionExpression("2.1"), "2.1");
        testCandidates.put(new VersionExpression("1.1"), "1.1");
        testCandidates.put(new VersionExpression("4"), "4");
        testCandidates.put(new VersionExpression("1"), "1");
        testCandidates.put(new VersionExpression("3.0.1"), "3.0.1");
        testCandidates.put(new VersionExpression("1.0.1"), "1.0.1");

        assertThat(resolver.resolve(testCandidates, "1"), equalTo("1"));
        assertThat(resolver.resolve(testCandidates, "1.0"), equalTo("1"));
        assertThat(resolver.resolve(testCandidates, "1.0.0"), equalTo("1"));

        assertThat(resolver.resolve(testCandidates, "2.1"), equalTo("2.1"));
        assertThat(resolver.resolve(testCandidates, "2.1.0"), equalTo("2.1"));

        assertThat(resolver.resolve(testCandidates, "3.0.1"), equalTo("3.0.1"));

        assertThat(resolver.resolve(testCandidates, "(,)"), equalTo("4"));

        assertThat(resolver.resolve(testCandidates, "(,3)"), equalTo("2.1"));

        assertThat(resolver.resolve(Collections
                .<Version, String>emptyMap(),
                "(,)"), equalTo(null));
        assertThat(resolver.resolve(testCandidates, "]2.1,3.0.1["),
                equalTo(null));

        assertThat(resolver.resolve(testCandidates, "]2.1,4["), equalTo(
                "3.0.1"));

        assertThat(resolver.resolve(testCandidates, "]2.1,3.0.1]"), equalTo(
                "3.0.1"));
    }
}
