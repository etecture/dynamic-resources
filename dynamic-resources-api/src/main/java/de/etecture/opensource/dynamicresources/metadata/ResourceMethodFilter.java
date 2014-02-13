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

import de.etecture.opensource.dynamicresources.api.FilterValueGenerator;

/**
 * represents the metadata for a filter.
 *
 * @param <T>
 * @author rhk
 * @version
 * @since
 */
public interface ResourceMethodFilter<T> {

    /**
     * returns the ResourceMethod to which this filter belongs to.
     *
     * @return
     */
    ResourceMethod getResourceMethod();

    /**
     * returns the type of the filter
     *
     * @return
     */
    Class<T> getType();

    /**
     * returns the name of the filter
     *
     * @return
     */
    String getName();

    /**
     * returns the description of the filter.
     *
     * @return
     */
    String getDescription();

    /**
     * returns the default value, this filter should use if the filter is not
     * specified in the request.
     *
     * @return
     */
    String getDefaultValue();

    /**
     * returns true, if the value is a valid value for this filter.
     *
     * @param value
     * @return
     */
    boolean isValidValue(T value);

    /**
     * returns the class of the {@link FilterValueGenerator} that is responsible
     * to generate appropriate values for this filter.
     *
     * @return
     */
    Class<? extends FilterValueGenerator> getValueGenerator();
}
