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
package de.etecture.opensource.dynamicresources.metadata;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * represents a default implementation of a {@link ResourcePath}
 *
 * @author rhk
 * @version
 * @since
 */
public class DefaultResourcePath implements ResourcePath {

    private static final Pattern GROUP_PATTERN = Pattern.compile(
            "\\{(\\w+?)(?:\\:(.+?))?\\}");
    private final Resource resource;
    private final String path;
    private final List<String> pathParamNames = new ArrayList<>();
    private final Pattern compiledPath;

    public DefaultResourcePath(Resource resource, String path) {
        this.resource = resource;
        this.path = path;
        Matcher matcher = GROUP_PATTERN.matcher(path);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            pathParamNames.add(name);
            String pattern = matcher.group(2);
            if (StringUtils.isBlank(pattern)) {
                pattern = "[^/]+";
            }
            pattern = pattern.replaceAll("\\\\", "\\\\\\\\");
            matcher.appendReplacement(buffer, String.format("(?<%s>%s)",
                    name, pattern));
        }
        matcher.appendTail(buffer);
        this.compiledPath = Pattern.compile(buffer.toString());
    }

    @Override
    public Resource getResource() {
        return this.resource;
    }

    @Override
    public List<String> getPathParameterNames() {
        return Collections.unmodifiableList(pathParamNames);
    }

    @Override
    public String buildCompleteUri(String... pathParamValues) {
        Matcher matcher = GROUP_PATTERN.matcher(path);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; matcher.find() && i < pathParamValues.length; i++) {
            matcher.appendReplacement(buffer, pathParamValues[i]);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    @Override
    public Map<String, String> getPathParameterValues(String uri) throws
            ResourcePathNotMatchException {
        Map<String, String> values = new HashMap<>();
        Matcher matcher = compiledPath.matcher(uri);
        if (matcher.matches()) {
            for (String groupName : getPathParameterNames()) {
                values.put(groupName, matcher.group(groupName));
            }
            return values;
        } else {
            throw new ResourcePathNotMatchException("the uri: " + uri
                    + " did not match the template: " + path);
        }
    }

    @Override
    public boolean matches(String uri) {
        uri = StringUtils.removeStart(uri, resource.getApplication().getBase());
        return compiledPath.matcher(uri).matches();
    }

    @Override
    public String toString() {
        return path;
    }

    private static String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return string;
        }
    }
}
