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

import de.etecture.opensource.dynamicresources.annotations.Produces;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import de.etecture.opensource.dynamicresources.metadata.Application;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@Produces(contentType = Application.class,
          mimeType = "text/plain")
public class ApplicationDescriptionWriter implements ResponseWriter<Application> {

    @Inject
    HttpServletRequest req;

    @Override
    public int getContentLength(Application entity, MediaType acceptedMediaType) {
        return -1;
    }

    @Override
    public void processElement(Application rd, Writer w,
            MediaType mimetype) throws IOException {
        final PrintWriter writer = new PrintWriter(w);
        writer.printf("Application: %s%n", rd.getName());
        writer.println(StringUtils.repeat("=", rd.getName()
                .length() + 10));
        writer.println();

        if (StringUtils.isBlank(rd.getDescription())) {
            writer.printf("\t%s%n", rd.getDescription());
        }

        writer.println("Available Resources");
        writer.println("-------------------");
        writer.println();

        for (Resource r : rd.getResources().values()) {
            writer
                    .printf("\t%s: %s://%s:%d%s%s%s%n", r.getName(), req
                    .getScheme(), req.getServerName(), req.getServerPort(), req
                    .getContextPath(),
                    rd.getBase(),
                    r.getPath()
                    .buildCompleteUri());
            writer.printf("\t\t%s%n%n", r.getDescription());
        }
    }
}
