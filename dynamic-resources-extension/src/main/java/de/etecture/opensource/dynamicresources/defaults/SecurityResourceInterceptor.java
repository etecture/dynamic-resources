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
package de.etecture.opensource.dynamicresources.defaults;

import de.etecture.opensource.dynamicresources.api.DefaultResponse;
import de.etecture.opensource.dynamicresources.annotations.Global;
import de.etecture.opensource.dynamicresources.api.OldRequest;
import de.etecture.opensource.dynamicresources.api.OldResourceInterceptor;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.SecurityContext;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import javax.inject.Inject;

/**
 * checks the security for the resources in the before method.
 *
 * @author rhk
 * @version
 * @since
 */
@Global
public class SecurityResourceInterceptor implements OldResourceInterceptor {

    @Inject
    SecurityContext security;

    @Override
    public Response before(OldRequest request) {
        String[] allowedRoles = request.getResourceMethod().rolesAllowed();
        if (allowedRoles != null && allowedRoles.length > 0) {
            boolean trust = false;
            for (String role : allowedRoles) {
                trust = trust || security.isUserInRole(role);
            }
            if (!trust) {
                return new DefaultResponse("User " + security
                        .getUserPrincipal()
                        + " is not allowed to perform this request.\n",
                        StatusCodes.FORBIDDEN);
            }
        }
        return null;

    }

    @Override
    public Response afterSuccess(OldRequest request, Response response) {
        return response;
    }

    @Override
    public <T> Response<T> afterFailure(
            OldRequest<T> request,
            Response<T> originalResponse, Throwable exception) {
        return originalResponse;
    }
}
