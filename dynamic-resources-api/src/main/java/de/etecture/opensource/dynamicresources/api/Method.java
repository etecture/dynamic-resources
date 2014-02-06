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
package de.etecture.opensource.dynamicresources.api;

import de.etecture.opensource.dynamicrepositories.api.Query;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * represents the query, that is invoked, if the given http-request is called
 * for this resource.
 *
 * @author rhk
 * @since 1.0.0
 * @version ${project.version}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Method {

    /**
     * defines the name of the request method, when this query has to be used.
     *
     * @return
     */
    String name();

    /**
     * the description of this method.
     *
     * @return
     */
    String description() default "";

    /**
     * the query
     *
     * @return
     */
    Query query() default @Query;

    /**
     * the status code to be provided by the response in case of no exception.
     *
     * @return
     */
    int status() default StatusCodes.OK;

    /**
     * a resource to redirect to.
     *
     * N.B. If this class is specified and a resource is given, then the status
     * is set to {@link StatusCodes#SEE_OTHER}.
     *
     * The path-parameters for the see-other-uri will be caught from the
     * parameters.
     *
     * @return
     */
    Class<?> seeOther() default Class.class;

    /**
     * the request types, this method accepts.
     *
     * @return
     */
    Consumes[] consumes() default {};

    /**
     * the content types, this method produces.
     *
     * @return
     */
    Produces[] produces() default {};

    /**
     * the roles that are allowed to request this resource.
     *
     * @return
     */
    String[] rolesAllowed() default {};

    /**
     * these are the interceptors, the framework has to be noted if this method
     * is invoked.
     *
     * @return
     */
    Class<? extends ResourceInterceptor>[] interceptors() default {};

    /**
     * the optional filter parameter.
     *
     * @return
     */
    Filter[] filters() default {};

    /**
     * the optional header parameter.
     *
     * @return
     */
    Header[] headers() default {};
}
