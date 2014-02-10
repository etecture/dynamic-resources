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

/**
 * an application is the base of all resources.
 *
 * @author rhk
 * @version
 * @since
 */
public interface Application {

    /**
     * the base uri, under which all resources are located.
     *
     * @return
     */
    String getBaseURI();

    /**
     * returns the resources defined in this application. the key is the
     * uri-template of this resource and the value is the Resource metadata.
     *
     * @return
     */
    Map<String, Resource> getResources();

    /**
     * returns the resource where it's uritemplate matches the given uri.
     *
     * If no resource found for this uri, a ResourceNotFoundException is thrown.
     *
     * @param uri
     * @return
     * @throws ResourceNotFoundException
     */
    Resource findResource(String uri) throws ResourceNotFoundException;

    /**
     * returns the resource that is defined for the given resourceType.
     *
     * If no resource found for this class, a ResourceNotFoundException is
     * thrown.
     *
     * @param resourceType
     * @return
     * @throws ResourceNotFoundException
     */
    Resource getResource(Class<?> resourceType) throws ResourceNotFoundException;

    /**
     * returns the description of this application.
     *
     * @return
     */
    String getDescription();
}
