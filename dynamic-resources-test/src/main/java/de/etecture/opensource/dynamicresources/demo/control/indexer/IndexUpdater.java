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
 *//*
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
package de.etecture.opensource.dynamicresources.demo.control.indexer;

import de.herschke.neo4j.uplink.api.CypherResultMappingException;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@Singleton
public class IndexUpdater {

    private static final Logger LOG = Logger.getLogger("IndexUpdater");
    private static final String NEXT_NODE_QUERY
            = ""
            + "MATCH "
            + "  (m:Movie) "
            + "RETURN "
            + "  ID(m) AS nodeId, "
            + "  'movies' AS indexName, "
            + "  COLLECT({key: 'title', value: m.title}) AS indexProperties "
            + "SKIP {skip}"
            + "LIMIT {limit} ";
    private static final long limit = 10;
    @Inject
    Event<IndexEvent> events;
    @Inject
    Neo4jUplink uplink;
    private long current = 0;

    public interface NodeToIndex {

        Long getNodeId();

        String getIndexName();

        Map<String, Object> getIndexProperties();
    }

    @Schedule(hour = "*",
              minute = "*",
              second = "*/15",
              persistent = false)
    public void onTimer() {
        try {
            LOG.info("scheduled index updater started.");

            LOG.log(Level.FINE,
                    "requesting {0} nodes to index, starting at node: {1}",
                    new Object[]{
                        limit,
                        current});
            // queries the nodes to index.
            Map<String, Object> params = new HashMap<>();
            params.put("skip", (Object) current);
            params.put("limit", (Object) limit);
            List<NodeToIndex> nodes = uplink.executeCypherQuery(
                    NodeToIndex.class,
                    NEXT_NODE_QUERY,
                    params);

            LOG.log(Level.FINE,
                    "nodes to index requested. found {0} nodes.",
                    nodes.size());
            if (nodes.isEmpty()) {
                // start again at the beginning.
                current = 0;
                LOG.log(Level.FINE, "restart index updater.");
            } else {
                // for each node to be indexed...
                for (NodeToIndex node : nodes) {
                    // ... create and fire an IndexEvent
                    LOG.log(Level.FINER,
                            "add node with id: {0} to index.",
                            node.getNodeId());
                    fireEvent(
                            new IndexEvent(
                                    IndexEvent.Action.REPLACE,
                                    node.getNodeId(),
                                    node.getIndexName()
                            )
                            .properties(node.getIndexProperties())
                    );
                }

                // increment the index to get the next event
                current += limit;
            }
            LOG.log(Level.INFO, "scheduled index updater done for now.");
        } catch (Neo4jServerException |
                CypherResultMappingException ex) {
            LOG.log(Level.SEVERE, "cannot request nodes to index, due to: ", ex);
        }
    }

    private void fireEvent(IndexEvent evt) {
        events
                .select(new IndexEvent.WithActionLiteral(evt.getAction()))
                .fire(evt);
    }

}
