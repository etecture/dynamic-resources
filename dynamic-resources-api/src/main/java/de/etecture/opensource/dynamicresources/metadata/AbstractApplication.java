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
public abstract class AbstractApplication implements Application {
    protected final String name;
    protected final String base;
    protected final String description;
    protected final Set<Resource> resources = new HashSet<>();

    public AbstractApplication(String name, String base, String description) {
        this.name = name;
        this.base = base;
        this.description = description;
    }

    public void addResource(Resource r) {
        if (r.getApplication() != this) {
            throw new IllegalArgumentException("Cannot add resource that is not part of this application!");
        }
        resources.add(r);
    }

    @Override
    public Resource findResource(String uri) throws
            ResourceNotFoundException {
        for (Resource resource : resources) {
            if (resource.getPath().matches(uri)) {
                return resource;
            }
        }
        throw new ResourceNotFoundException(uri.toString());
    }

    @Override
    public String getBase() {
        return base;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<String, Resource> getResources() {
        return new AbstractValueMap<String, Resource>(resources) {
            @Override
            protected String getKeyForValue(Resource value) {
                return value.getName();
            }
        };
    }

}
