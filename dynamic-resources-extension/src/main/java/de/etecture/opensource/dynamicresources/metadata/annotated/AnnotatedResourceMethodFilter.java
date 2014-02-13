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

package de.etecture.opensource.dynamicresources.metadata.annotated;

import de.etecture.opensource.dynamicresources.annotations.Filter;
import de.etecture.opensource.dynamicresources.api.FilterValueGenerator;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethod;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodFilter;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class AnnotatedResourceMethodFilter<T> implements
        ResourceMethodFilter<T> {

    private final ResourceMethod method;
    private final Filter filter;

    AnnotatedResourceMethodFilter(ResourceMethod method,
            Filter filter) {
        this.method = method;
        this.filter = filter;
    }

    @Override
    public String getName() {
        return this.filter.name();
    }

    @Override
    public String getDescription() {
        return this.filter.description();
    }

    @Override
    public Class<T> getType() {
        return (Class<T>) this.filter.type();
    }

    @Override
    public ResourceMethod getResourceMethod() {
        return method;
    }

    @Override
    public String getDefaultValue() {
        return filter.defaultValue();
    }

    @Override
    public boolean isValidValue(T value) {
        if (value != null && getType().isInstance(value)) {
            if (getType() == String.class) {
                return ((String) value).matches(filter.validationRegex());
            }
        }
        return false;
    }

    @Override
    public Class<? extends FilterValueGenerator> getValueGenerator() {
        return filter.converter();
    }

    public static AnnotatedResourceMethodFilter create(ResourceMethod method,
            Filter annotation) {
        return new AnnotatedResourceMethodFilter(method, annotation);
    }
}
