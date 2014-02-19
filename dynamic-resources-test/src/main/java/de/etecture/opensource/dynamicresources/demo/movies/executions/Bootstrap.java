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
package de.etecture.opensource.dynamicresources.demo.movies.executions;

import de.etecture.opensource.dynamicresources.annotations.Body;
import de.etecture.opensource.dynamicresources.annotations.Executes;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class Bootstrap {

    @Inject
    @Default
    Neo4jUplink uplink;

    @Executes(application = "MovieCatalog",
              resource = "MovieCatalogRoot",
              method = HttpMethods.POST,
              responseType = Boolean.class,
              requestType = String.class)
    public Boolean executesBootstrap(ExecutionContext<?, ?> context,
                                     @Body String bootstrapQuery)
            throws Neo4jServerException {
        // cleanup the database at first
        if (executesCleanup()) {
            // now perform the bootstrap
            return (Boolean) uplink
                    .executeCypherQuery(bootstrapQuery)
                    .getValue(0, 0);

        }
        return false;
    }

    @Executes(application = "MovieCatalog",
              resource = "MovieCatalogRoot",
              method = HttpMethods.DELETE,
              responseType = Boolean.class)
    public Boolean executesCleanup() throws Neo4jServerException {
        // delete the relationships in the db
        if ((Boolean) uplink
                .executeCypherQuery(
                        "start r = relationship(*) delete r return count(*) >= 0")
                .getValue(0, 0)) {
            // delete the nodes
            return (Boolean) uplink
                    .executeCypherQuery(
                            "start n = node(*) delete n return count(*) >= 0")
                    .getValue(0, 0);
        }
        return false;
    }

}
