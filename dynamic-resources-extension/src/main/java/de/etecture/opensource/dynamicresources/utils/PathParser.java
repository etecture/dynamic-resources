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
package de.etecture.opensource.dynamicresources.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
        Matcher matcher = matcher(uriTemplate, path);
        if (matcher.matches()) {
            for (String groupName : groupNames(uriTemplate)) {
                groups.put(groupName, matcher.group(groupName));
            }
            return true;
        }
        return false;
    }

    public static String createURI(String uriTemplate,
            Map<String, String> groups) {
        Matcher matcher = GROUP_PATTERN.matcher(uriTemplate);
        StringBuffer buffer = new StringBuffer("/");
        while (matcher.find()) {
            matcher.appendReplacement(buffer, encode(groups
                    .get(matcher.group(1))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return string;
        }
    }

    public static List<String> groupNames(String uriTemplate) {
        List<String> groups = new ArrayList<>();
        Matcher matcher = GROUP_PATTERN.matcher(uriTemplate);
        while (matcher.find()) {
            groups.add(matcher.group(1));
        }
        return groups;
    }

    public static Matcher matcher(String uriTemplate,
            String path) {
        Pattern uriPattern = compile(uriTemplate);
        return uriPattern.matcher(path);
    }

    public static Pattern compile(String uriTemplate) {
        Matcher matcher = GROUP_PATTERN.matcher(uriTemplate);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            String pattern = matcher.group(2);
            if (StringUtils.isBlank(pattern)) {
                pattern = "[^/]+";
            }
            pattern = pattern.replaceAll("\\\\", "\\\\\\\\");
            matcher.appendReplacement(buffer, String.format("(?<%s>%s)",
                    name, pattern));
        }
        matcher.appendTail(buffer);
        return Pattern.compile(buffer.toString());
    }
}
