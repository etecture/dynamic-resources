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
package de.etecture.opensource.dynamicresources.demo.boundary.movies.processors;

import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.HeaderValueGenerator;
import de.etecture.opensource.dynamicresources.api.HttpHeaders;
import de.etecture.opensource.dynamicresources.metadata.Resource;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodResponseHeader;
import de.herschke.converters.api.ConvertException;
import de.herschke.converters.api.Converters;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * generates the {@link HttpHeaders#LINK} header with <code>rel="prev"</code>
 * <p>
 * @author rhk
 * @version
 * @since
 */
public abstract class AbstractNavigationLinkHeaderGenerator implements
        HeaderValueGenerator {

    private static final Logger LOG = Logger.getLogger(
            AbstractNavigationLinkHeaderGenerator.class.getName());

    @Inject
    Converters converters;

    @Inject
    HttpServletRequest req;
    private final String rel;

    protected AbstractNavigationLinkHeaderGenerator(String rel) {
        this.rel = rel;
    }

    @Override
    public Object generateHeaderValue(ResourceMethodResponseHeader header,
                                      ExecutionContext context) {
        return generateLink(context, rel);
    }

    protected abstract int getNewSkip(int skip, int limit);

    protected Object generateLink(
            ExecutionContext context, String rel) {
        Resource resource = context.getResourceMethod().getResource();
        String path;
        if (req != null) {
            path = String.format("%s://%s:%d%s%s%s",
                                 req.getScheme(),
                                 req.getServerName(),
                                 req.getServerPort(),
                                 req.getContextPath(),
                                 resource.getApplication().getBase(),
                                 resource.getPath().buildCompleteUri());
        } else {
            path = resource.getApplication().getBase() + resource.getPath()
                    .buildCompleteUri();

        }
        try {
            int limit = converters
                    .select(Integer.class)
                    .convert(
                            context
                            .getParameterValue("limit", 0));
            int skip = converters
                    .select(Integer.class)
                    .convert(
                            context
                            .getParameterValue("skip", 0));
            String query = (String) context.getParameterValue("query", "*");
            int newSkip = getNewSkip(skip, limit);
            if (newSkip >= 0) {
                String uri = String.format(
                        "<%s?query=%s&skip=%d&limit=%d>; rel=\"%s\"",
                        path,
                        query,
                        newSkip,
                        limit,
                        rel);
                LOG.log(Level.FINER,
                        "add a Link header: {0}",
                        uri);
                return uri;
            }
        } catch (ConvertException ex) {
            LOG
                    .log(Level.WARNING,
                         "cannot convert skip or limit parameter.", ex);

        }
        return null;
    }

}
