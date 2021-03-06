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

import de.etecture.opensource.dynamicresources.api.accesspoints.ApplicationAccessor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * represents a ReST Resource.
 * <p>
 * Interfaces tagged with this annotation are handled as ReST
 * ApplicationAccessor.
 * <p>
 * Example:
 * <pre>
 * &#64;Resource("/data/myresource/{id}")
 * public interface MyResource {
 *   String getId();
 *   String getInformation();
 * }
 * </pre>
 * <p>
 * Fields of type {@link ApplicationAccessor} tagged with this annotations are
 * handled as * an injection point to a resource with the given path.
 * <p>
 * Example:
 * <pre>
 * &#64;Stateless
 * public class MyBean {
 *
 *   &#64;Inject
 *   &#64;Resource("/data/myresource/{id}")
 *   ApplicationAccessor&lt;MyResource&gt; myresources;
 *
 *   // ...
 * }
 * </pre>
 *
 * @author rhk
 * @since 1.0.0
 * @version ${project.version}
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Target({ElementType.TYPE,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PARAMETER})
public @interface Resource {

    /**
     * defines the uri-pattern where this resource is located at. the default
     * value is '/' + lowercased name of resource-class
     *
     * @return
     */
    @Nonbinding
    String path() default "";

    /**
     * the name of the resource.
     *
     * @return
     */
    String name() default "";

    /**
     * the description of the resource.
     *
     * @return
     */
    @Nonbinding
    String description() default "";
}
