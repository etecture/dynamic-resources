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
package de.etecture.opensource.dynamicresources.api.accesspoints;

import de.etecture.opensource.dynamicresources.metadata.ApplicationNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import java.util.Set;

/**
 * this is an access-point to all application, this extension is aware of.
 *
 * @author rhk
 * @version ${project.version}
 * @since 2.0.0
 */
public interface Applications {

    /**
     * selects the ApplicationAccessor of the application for the specified application
     * name.
     * <p>
     * N.B. another way to get the resources by name, is to inject the
     * ApplicationAccessor-Interface with &#64;Named annotation. Example:
     * <p>
     * <pre>
     * public class MyBean {
     *
     *   &#64;Inject
     *   &#64;Named("MyApplication")
     *   ApplicationAccessor myApplicationResources;
     *
     *   // ...
     * }
     * </pre>
     *
     * @param applicationName
     * @return
     * @throws ApplicationNotFoundException
     */
    ApplicationAccessor selectByName(String applicationName) throws
            ApplicationNotFoundException;

    /**
     * selects the ApplicationAccessor of the application for the specified basePath.
     *
     * @param basePath
     * @return
     * @throws ApplicationNotFoundException
     */
    ApplicationAccessor selectByPath(String basePath) throws ApplicationNotFoundException;

    /**
     * returns a set that holds all the registered application names.
     *
     * @return
     */
    Set<String> getApplicationNames();

    /**
     * finds the Resource Accessor for a given uri. The uri is the complete uri,
     * starting with the base path that is specified in the application of a
     * resource.
     * <p>
     * Hint: The given uri must not contain the context-root of the webapp where
     * this framework is deployed to!
     *
     * @param uri
     * @return
     * @throws ApplicationNotFoundException
     * @throws ResourceNotFoundException
     */
    ResourceAccessor findForCompleteUri(String uri) throws ApplicationNotFoundException,
            ResourceNotFoundException;
}
