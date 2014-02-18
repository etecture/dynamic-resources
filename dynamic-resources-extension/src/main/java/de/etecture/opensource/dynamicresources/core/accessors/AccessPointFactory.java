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
package de.etecture.opensource.dynamicresources.core.accessors;

import de.etecture.opensource.dynamicresources.annotations.Application;
import de.etecture.opensource.dynamicresources.annotations.URI;
import de.etecture.opensource.dynamicresources.api.accesspoints.ApplicationAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.Applications;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.TypedResourceAccessor;
import de.etecture.opensource.dynamicresources.metadata.ApplicationNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import de.etecture.opensource.dynamicresources.utils.InjectionPointHelper;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class AccessPointFactory {

    /**
     * the applications.
     */
    @Inject
    Applications applications;

    /**
     * produces a TypedResourceAccessor for an injectionPoint annotated with a
     * {@link URI}.
     *
     * searches the resourceaccessor for the uri specified with the annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;URI("/resources/customers/1234567890/")
     *   TypedResourceAccessor&lt;CustomerResource&gt; customerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param ip
     * @return
     */
    @Produces
    @URI("")
    public TypedResourceAccessor produceTypedResourceAccessorForUri(
            InjectionPoint ip) {
        // lookup the path from the @Resource annotation of this ip
        String path = InjectionPointHelper.findQualifier(
                URI.class,
                ip).value();
        // get the type of the response (which is the 0th element of the
        // TypedResourceAccessor type)
        Class<?> responseType = InjectionPointHelper
                .getGenericTypeOfInjectionPoint(ip, 0);
        if (responseType == null) {
            throw new InjectionException(
                    "cannot determine the response-type for the TypedResourceAccessor, so cannot find an appropriate resource!");
        } else {
            try {
                return applications.findForCompleteUri(path)
                        .select(responseType);
            } catch (ResponseTypeNotSupportedException |
                    ApplicationNotFoundException | ResourceNotFoundException ex) {
                throw new InjectionException(ex);
            }
        }
    }

    /**
     * produces a ResourceAccessor for an injectionPoint annotated with a
     * {@link URI}.
     *
     * searches the resourceaccessor for the uri specified with the annotation.
     *
     * <p>
     * Example:
     * <pre><code>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;URI("/resources/customers/1234567890/")
     *   ResourceAccessor customerResourceAccessor;
     *
     *   ...
     * }
     * <code></pre>
     *
     * @param ip
     * @return
     */
    @Produces
    @URI("")
    public ResourceAccessor produceResourceAccessorForUri(
            InjectionPoint ip) {
        // lookup the path from the @Resource annotation of this ip
        String path = InjectionPointHelper.findQualifier(
                URI.class,
                ip).value();
        try {
            return applications.findForCompleteUri(path);
        } catch (ApplicationNotFoundException | ResourceNotFoundException ex) {
            throw new InjectionException(ex);
        }
    }

    @Produces
    public ApplicationAccessor produceApplicationAccessorForUri(
            InjectionPoint ip) {
        try {
            if (ip.getAnnotated().isAnnotationPresent(URI.class)) {
                return applications.selectByPath(InjectionPointHelper
                        .findQualifier(
                        URI.class,
                        ip).value());
            } else if (ip.getAnnotated().isAnnotationPresent(Application.class)) {
                return applications.selectByName(ip.getAnnotated()
                        .getAnnotation(
                        de.etecture.opensource.dynamicresources.annotations.Application.class)
                        .name());
            } else if (applications.getApplicationNames().size() == 1) {
                return applications.selectByName(applications
                        .getApplicationNames().iterator().next());
            } else {
                throw new InjectionException(
                        "Ambigious Applications found for injection point: "
                        + ip);
            }
        } catch (ApplicationNotFoundException ex) {
            throw new InjectionException(ex);
        }
    }
}
