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

import de.etecture.opensource.dynamicresources.api.accesspoints.ApplicationAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.Applications;
import de.etecture.opensource.dynamicresources.api.accesspoints.MethodAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.ResourceAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.TypedResourceAccessor;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.ApplicationNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourcePathNotMatchException;
import de.etecture.opensource.dynamicresources.utils.ApplicationLiteral;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * this is the standard implementation for {@link Applications}.
 *
 * It provides the implementation for the
 * <code>select()</code>-methods in the {@link Application} interface as well as
 * producer methods to be able to inject qualified
 * {@link ApplicationAccessor}, {@link TypedResourceAccessor}s and
 * {@link MethodAccessor}s.
 *
 * @author rhk
 * @version
 * @since
 * @see ApplicationAccessor
 */
public class DynamicApplications implements Applications {

    /**
     * the allApplications.
     */
    @Inject
    Instance<Application> allApplications;
    /**
     * this is used to lookup or create accesspoints.
     */
    @Inject
    @Any
    Instance<Object> accessPoints;

    @Override
    public Set<String> getApplicationNames() {
        Set<String> applicationNames = new HashSet<>();
        for (Application application : allApplications) {
            applicationNames.add(application.getName());
        }
        return Collections.unmodifiableSet(applicationNames);
    }

    @Override
    public ApplicationAccessor selectByName(String applicationName) throws
            ApplicationNotFoundException {
        final Instance<ApplicationAccessor> selected =
                accessPoints.select(ApplicationAccessor.class,
                new ApplicationLiteral(applicationName));
        if (selected.isAmbiguous()) {
            throw new ApplicationNotFoundException(
                    "more than one applications will  match the name: "
                    + applicationName);
        } else if (selected.isUnsatisfied()) {
            throw new ApplicationNotFoundException(
                    "no application match the name: " + applicationName);
        }
        return selected.get();
    }

    @Override
    public ApplicationAccessor selectByPath(String basePath) throws
            ApplicationNotFoundException {
        // lookup the allApplications for the baseUri
        for (Application application : allApplications) {
            if (application.getBase().equals(basePath)) {
                return selectByName(application.getName());
            }
        }
        throw new ApplicationNotFoundException(
                "There is no application registered, that is responsible for the given path: "
                + basePath);
    }

    @Override
    public ResourceAccessor findForCompleteUri(String uri) throws
            ResourceNotFoundException, ApplicationNotFoundException {
        // lookup the allApplications baseUri...
        for (Application application : allApplications) {
            if (uri.startsWith(application.getBase())) {
                Resource resource = application.findResource(uri);
                try {
                    return selectByName(application.getName())
                            .selectByName(resource.getName())
                            .pathParams(resource.getPath()
                            .getPathParameterValues(uri));
                } catch (ResourcePathNotMatchException ex) {
                    throw new ResourceNotFoundException(ex);
                }
            }
        }
        throw new ApplicationNotFoundException(
                "there is no application that holds any resource matching the path: "
                + uri);
    }
}
