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
package de.etecture.opensource.dynamicresources.test;

import de.herschke.neo4j.uplink.api.Neo4jUplink;
import de.herschke.neo4j.uplink.cdi.Remote;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class IndexConstructor implements Runnable {

    private static final String QUERY =
            ""
            + "CREATE "
            + "  (t1:Test { "
            + "    id: '1111', "
            + "    firstName: 'Max', "
            + "    lastName: 'Mustermann' "
            + "  }), "
            + "  (t2:Test { "
            + "    id: '2222', "
            + "    firstName: 'Manuela', "
            + "    lastName: 'Musterfrau' "
            + "  }), "
            + "  (t3:Test { "
            + "    id: '3333', "
            + "    firstName: 'Stink', "
            + "    lastName: 'Stiefel' "
            + "  }), "
            + "  (t4:Test { "
            + "    id: '4444', "
            + "    firstName: 'Test', "
            + "    lastName: 'Dummy' "
            + "  }) "
            + "WITH count(*) as created "
            + "MATCH (t:Test) "
            + "RETURN "
            + "  ID(t) AS `nodeId`,"
            + "  [{key:'name', value:t.lastName}] AS `indexProperties`"
            + "";

    public interface NodeToIndex {

        Long getNodeId();

        Map<String, Object> getIndexProperties();
    }
    @Inject
    @Remote(url = "http://localhost:17474/db/data")
    Neo4jUplink uplink;

    public void run() {
        try {
            uplink.createLegacyNodeIndex("testIndex", Collections
                    .<String, Object>singletonMap("type", "exact"));
            for (NodeToIndex node : uplink.executeCypherQuery(NodeToIndex.class,
                    QUERY)) {
                for (Entry<String, Object> entry : node.getIndexProperties()
                        .entrySet()) {
                    uplink.addNodeToIndex("testIndex", node.getNodeId(), entry
                            .getKey(), entry.getValue().toString());
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
