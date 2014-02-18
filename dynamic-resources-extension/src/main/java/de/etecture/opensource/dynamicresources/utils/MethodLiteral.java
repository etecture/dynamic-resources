/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.utils;

import de.etecture.opensource.dynamicrepositories.api.annotations.Query;
import de.etecture.opensource.dynamicresources.annotations.Consumes;
import de.etecture.opensource.dynamicresources.annotations.Filter;
import de.etecture.opensource.dynamicresources.annotations.Header;
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Produces;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author rhk
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class MethodLiteral extends AnnotationLiteral<Method> implements Method {

    private static final long serialVersionUID = 1L;
    private final String name;

    public MethodLiteral(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public Query[] query() {
        return new Query[0];
    }

    @Override
    public int status() {
        return StatusCodes.OK;
    }

    @Override
    public Consumes[] consumes() {
        return new Consumes[0];
    }

    @Override
    public Produces[] produces() {
        return new Produces[0];
    }

    @Override
    public String[] rolesAllowed() {
        return new String[0];
    }

    @Override
    public Filter[] filters() {
        return new Filter[0];
    }

    @Override
    public Header[] headers() {
        return new Header[0];
    }
}
