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
package de.etecture.opensource.dynamicresources.defaults;

import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.FilterValueGenerator;
import de.etecture.opensource.dynamicresources.api.InvalidFilterValueException;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodFilter;
import de.herschke.converters.api.ConvertException;
import de.herschke.converters.api.Converters;
import java.util.Collection;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class DefaultFilterValueGenerator implements FilterValueGenerator {

    @Inject
    Converters converters;

    @Override
    public <T> T generate(
            ResourceMethodFilter<T> filter, ExecutionContext<?, ?> context)
            throws
            InvalidFilterValueException {
        Object value = context.getParameterValue(filter.getName());
        try {
            if (value instanceof Collection) {
                if (((Collection) value).isEmpty()) {
                    value = null;
                } else {
                    value = ((Collection) value).iterator().next();
                }
            }
            if (value == null) {
                value = filter.getDefaultValue();
            }
            T t = converters.select(filter.getType()).convert(value);
            if (filter.isValidValue(t)) {
                return t;
            } else {
                throw new InvalidFilterValueException(filter, value);
            }
        } catch (ConvertException ex) {
            throw new InvalidFilterValueException(filter, ex, value);
        }
    }
}
