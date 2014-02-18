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
import de.etecture.opensource.dynamicresources.defaults.AbstractCompoundFilterConverter.FilterPart;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodFilter;
import de.herschke.converters.api.ConvertException;
import de.herschke.converters.api.Converters;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

/**
 *
 * @param <F>
 * @author rhk
 * @version
 * @since
 */
public abstract class AbstractCompoundFilterConverter<F extends FilterPart>
        implements FilterValueGenerator {

    public interface FilterPart {

        String name();

        int ordinal();

        Object value(ExecutionContext context);
    }
    private final Set<F> parts = new HashSet<>();
    private final String template;
    @Inject
    Converters converters;

    protected AbstractCompoundFilterConverter(String template, F... parts) {
        this(template, Arrays.asList(parts));
    }

    protected AbstractCompoundFilterConverter(
            String template,
            Collection<F> parts) {
        this.template = template;
        this.parts.addAll(parts);

    }

    @Override
    public <T> T generate(
            ResourceMethodFilter<T> filter, ExecutionContext<?, ?> context)
            throws
            InvalidFilterValueException {
        try {
            if (!context.hasParameter(filter.getName())) {
            Object[] partValues = new Object[parts.size()];
            for (F part : parts) {
                partValues[part.ordinal()] = part.value(context);
            }
            return converters.select(filter.getType()).convert(String.format(
                    template, partValues));
        } else {
                return converters.select(filter.getType()).convert(context
                        .getParameterValue(filter.getName()));
            }
        } catch (ConvertException ex) {
            throw new InvalidFilterValueException(filter, ex);
        }
    }
}
