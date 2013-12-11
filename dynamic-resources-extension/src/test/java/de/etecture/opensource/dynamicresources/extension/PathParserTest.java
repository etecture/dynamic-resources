/*
 *  This file is part of the ETECTURE Open Source Community Projects.
 *
 *  Copyright (c) 2013 by:
 *
 *  ETECTURE GmbH
 *  Darmstädter Landstraße 112
 *  60598 Frankfurt
 *  Germany
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  3. Neither the name of the author nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.extension;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class PathParserTest {

    @Test
    public void testMatch() {
        String template =
                "/customers/{custNo:\\d+}/employees/{empNo}/addresses";
        String path =
                "/customers/1234567890/employees/1-9Y2CLO/addresses";

        Map<String, String> groups = new HashMap<>();
        Assert.assertTrue(PathParser.match(template, path, groups));
        Assert.assertFalse(groups.isEmpty());
        Assert.assertEquals("1234567890", groups.get("custNo"));
        Assert.assertEquals("1-9Y2CLO", groups.get("empNo"));
    }

    @Test
    public void testCreateUri() {
        String template =
                "/customers/{custNo:\\d+}/employees/{empNo}/addresses";
        String expectedPath =
                "/customers/1234567890/employees/1-9Y2CLO/addresses";

        Map<String, String> groups = new HashMap<>();
        groups.put("custNo", "1234567890");
        groups.put("empNo", "1-9Y2CLO");
        Assert.assertEquals(expectedPath, PathParser.createURI(template,
                groups));
    }
}
