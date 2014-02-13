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

import de.etecture.opensource.dynamicresources.utils.AbstractValueMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public abstract class AbstractResource implements Resource {

    protected final Application application;
    protected final String name;
    protected final String description;
    protected final Set<ResourceMethod> methods = new HashSet<>();

    protected AbstractResource(Application application, String name,
            String description) {
        this.application = application;
        this.name = name;
        this.description = description;
    }

    public void addMethod(ResourceMethod method) {
        if (method.getResource() != this) {
            throw new IllegalArgumentException(
                    "ResourceMethod must be part of this resource.");
        }
        methods.add(method);
    }

    @Override
    public Application getApplication() {
        return application;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public ResourceMethod getMethod(String methodName) throws
            ResourceMethodNotFoundException {
        if (!getMethods().containsKey(methodName)) {
            throw new ResourceMethodNotFoundException(this, methodName);
        }
        return getMethods().get(methodName);
    }

    @Override
    public Map<String, ResourceMethod> getMethods() {
        return new AbstractValueMap<String, ResourceMethod>(methods) {
            @Override
            protected String getKeyForValue(ResourceMethod value) {
                return value.getName();
            }
        };
    }

    @Override
    public String getName() {
        return this.name;
    }

}
