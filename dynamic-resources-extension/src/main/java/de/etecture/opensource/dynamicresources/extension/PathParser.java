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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class PathParser {

    private static final Pattern GROUP_PATTERN = Pattern.compile(
            "\\{(\\w+?)(?:\\:(.+?))?\\}");

    public static boolean match(String uriTemplate, String path,
            Map<String, String> groups) {
        Matcher matcher = GROUP_PATTERN.matcher(uriTemplate);
        List<String> groupNames = new ArrayList<>();
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String pattern = matcher.group(2);
            if (StringUtils.isBlank(pattern)) {
                pattern = "[^/]+";
            }
            pattern = pattern.replaceAll("\\\\", "\\\\\\\\");
            matcher.appendReplacement(buffer, String.format("(?<%s>%s)", matcher
                    .group(1), pattern));
            groupNames.add(matcher.group(1));
        }
        matcher.appendTail(buffer);
        final Pattern uriPattern = Pattern.compile(buffer.toString());
        matcher = uriPattern.matcher(
                "/customers/1234567890/employees/1-9Y2CLO/addresses");
        if (matcher.matches()) {
            for (String groupName : groupNames) {
                groups.put(groupName, matcher.group(groupName));
            }
            return true;
        }
        return false;
    }

    public static String createURI(String uriTemplate,
            Map<String, String> groups) {
        Matcher matcher = GROUP_PATTERN.matcher(uriTemplate);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, groups.get(matcher.group(1)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
