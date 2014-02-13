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
package de.etecture.opensource.dynamicresources.annotations;

import de.etecture.opensource.dynamicrepositories.api.annotations.Query;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.etecture.opensource.dynamicresources.api.ResourceMethodInterceptor;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * represents the query, that is invoked, if the given http-request is called
 * for this resource.
 *
 * @author rhk
 * @since 1.0.0
 * @version ${project.version}
 */
@Qualifier
@Retention(RUNTIME)
@Target({METHOD,
    FIELD,
    PARAMETER,
    TYPE})
public @interface Method {

    /**
     * defines the name of the request method, when this query has to be used.
     * defaults to 'GET'
     *
     * @return
     */
    String name() default HttpMethods.GET;

    /**
     * the description of this method.
     *
     * @return
     */
    @Nonbinding
    String description() default "";

    /**
     * the queries that builds the response of this method.
     *
     * @return
     */
    @Nonbinding
    Query[] query() default {};

    /**
     * the status code to be provided by the response in case of no exception.
     *
     * @return
     */
    @Nonbinding
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
    @Nonbinding
    Class<?> seeOther() default Class.class;

    /**
     * the request types, this method accepts.
     *
     * @return
     */
    @Nonbinding
    Consumes[] consumes() default {};

    /**
     * the content types, this method produces.
     *
     * @return
     */
    @Nonbinding
    Produces[] produces() default {};

    /**
     * the roles that are allowed to request this resource.
     *
     * @return
     */
    @Nonbinding
    String[] rolesAllowed() default {};

    /**
     * these are the interceptors, the framework has to be noted if this method
     * is invoked.
     *
     * @return
     */
    @Nonbinding
    Class<? extends ResourceMethodInterceptor>[] interceptors() default {};

    /**
     * the optional filter parameter.
     *
     * @return
     */
    @Nonbinding
    Filter[] filters() default {};

    /**
     * the optional header parameter.
     *
     * @return
     */
    @Nonbinding
    Header[] headers() default {};
}
