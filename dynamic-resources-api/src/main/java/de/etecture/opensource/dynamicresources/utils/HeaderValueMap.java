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

import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponseHeader;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class HeaderValueMap extends AbstractMap<String, List<Object>> {

    private static class HeaderValue extends ArrayList<Object> {
        private static final long serialVersionUID = 1L;

        private final ResourceMethodResponseHeader.Type type;

        HeaderValue(ResourceMethodResponseHeader.Type type) {
            this.type = type;
        }

        public ResourceMethodResponseHeader.Type getType() {
            return type;
        }
    }
    private final Map<String, HeaderValue> headerValues = new HashMap<>();

    public ResourceMethodResponseHeader.Type getValueType(String key) {
        if (headerValues.containsKey(key)) {
            return headerValues.get(key).getType();
        } else {
            return null;
        }
    }

    public void add(String name, ResourceMethodResponseHeader.Type type,
            Object value) {
        HeaderValue values = headerValues.get(name);
        if (values == null) {
            values = new HeaderValue(type);
            headerValues.put(name, values);
        }
        values.add(value);
    }

    public void addAll(String name, ResourceMethodResponseHeader.Type type,
            List<Object> value) {
        HeaderValue values = headerValues.get(name);
        if (values == null) {
            values = new HeaderValue(type);
            headerValues.put(name, values);
        }
        values.addAll(value);
    }

    @Override
    public List<Object> put(String key,
            List<Object> value) {
        throw new UnsupportedOperationException("use add or addAll!");
    }

    @Override
    public Set<Entry<String, List<Object>>> entrySet() {
        return new AbstractSet<Map.Entry<String, List<Object>>>() {
            @Override
            public Iterator<Entry<String, List<Object>>> iterator() {
                final Iterator<Entry<String, HeaderValue>> it = headerValues
                        .entrySet().iterator();
                return new Iterator<Entry<String, List<Object>>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<String, List<Object>> next() {
                        final Entry<String, HeaderValue> entry = it.next();
                        return new Entry<String, List<Object>>() {
                            @Override
                            public String getKey() {
                                return entry.getKey();
                            }

                            @Override
                            public List<Object> getValue() {
                                return entry.getValue();
                            }

                            @Override
                            public List<Object> setValue(
                                    List<Object> value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public int size() {
                return headerValues.size();
            }
        };
    }
}
