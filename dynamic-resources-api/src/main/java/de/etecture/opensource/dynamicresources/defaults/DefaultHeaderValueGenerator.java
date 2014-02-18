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
import de.etecture.opensource.dynamicresources.api.HeaderValueGenerator;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponseHeader;
import de.herschke.converters.api.ConvertException;
import de.herschke.converters.api.Converters;
import java.text.DateFormat;
import java.text.ParseException;
import javax.inject.Inject;

/**
 * generates a header value by using the default value given in the header
 * definition.
 *
 * @author rhk
 * @version
 * @since
 */
public class DefaultHeaderValueGenerator implements HeaderValueGenerator {

    public static final DateFormat DATEFORMAT = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.SHORT);
    @Inject
    Converters converters;

    @Override
    public Object generateHeaderValue(ResourceMethodResponseHeader header,
            ExecutionContext context) {
        try {
            switch (header.getType()) {
                case DATE:
                    try {
                        return DATEFORMAT.parse(converters.toString(header
                                .getDefaultValue())).getTime();
                    } catch (ParseException ex) {
                        return converters.toString(header.getDefaultValue());
                    }
                case INTEGER:
                    return converters.select(Integer.class).convert(header
                            .getDefaultValue());
                default:
                    return converters.toString(header.getDefaultValue());
            }
        } catch (ConvertException ex) {
            return converters.toString(header.getDefaultValue());
        }
    }
}
