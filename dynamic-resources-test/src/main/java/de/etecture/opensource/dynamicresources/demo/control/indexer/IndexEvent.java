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
package de.etecture.opensource.dynamicresources.demo.control.indexer;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class IndexEvent {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({FIELD,
             PARAMETER})
    public static @interface WithAction {

        Action value();
    }

    public static enum Action {

        ADD,
        REPLACE,
        REMOVE
    }

    @SuppressWarnings("AnnotationAsSuperInterface")
    static class WithActionLiteral extends AnnotationLiteral<WithAction>
            implements WithAction {

        private static final long serialVersionUID = 1L;

        private final Action action;

        WithActionLiteral(Action action) {
            this.action = action;
        }

        @Override
        public Action value() {
            return action;
        }

    }

    private final Action action;
    private final String indexName;
    private final long nodeId;
    private final Map<String, Object> properties = new HashMap<>();

    public IndexEvent(Action action, long nodeId, String indexName) {
        this.action = action;
        this.nodeId = nodeId;
        this.indexName = indexName;
    }

    public String getIndexName() {
        return indexName;
    }

    public long getNodeId() {
        return nodeId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public IndexEvent property(String name, Object value) {
        this.properties.put(name, value);
        return this;
    }

    public IndexEvent properties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    public Action getAction() {
        return action;
    }

}
