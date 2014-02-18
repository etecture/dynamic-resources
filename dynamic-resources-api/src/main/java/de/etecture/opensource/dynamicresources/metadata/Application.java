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

import java.util.Map;
import java.util.Set;
import javax.servlet.ServletSecurityElement;

/**
 * an application is the base of all resources.
 *
 * @author rhk
 * @version
 * @since
 */
public interface Application {

    /**
     * the name of the application.
     *
     * @return
     */
    String getName();

    /**
     * the base path, under which all resources are located.
     * <p>
     * Hint: the base path of this application is started AFTER the
     * context-root. So a '/' means that this application is located directly
     * under the context-root of this webapp.
     *
     * @return
     */
    String getBase();

    /**
     * returns the resources defined in this application. the key is the
     * uri-template of this resource and the value is the Resource metadata.
     *
     * @return
     */
    Map<String, Resource> getResources();

    /**
     * returns the resource where it's uritemplate matches the given path. The
     * path is the complete path starting after the context-root of the webapp.
     * So the context-root must not be part of the given path.
     *
     * If no resource found for this path, a ResourceNotFoundException is
     * thrown.
     *
     * @param path
     * @return
     * @throws ResourceNotFoundException
     */
    Resource findResource(String path) throws ResourceNotFoundException;

    /**
     * returns the description of this application.
     *
     * @return
     */
    String getDescription();

    /**
     * returns a set with all the declared role names in this application.
     *
     * @return
     */
    Set<String> getDeclaredRoleNames();

    /**
     * returns the servletsecurityelement to define security for this
     * application.
     *
     * @return
     */
    ServletSecurityElement getApplicationSecurity();
}
