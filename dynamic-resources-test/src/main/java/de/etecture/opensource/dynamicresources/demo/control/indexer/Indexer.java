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
package de.etecture.opensource.dynamicresources.demo.control.indexer;

import de.herschke.converters.api.Converters;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@Stateless
public class Indexer {

    private static final Logger LOG = Logger.getLogger("Indexer");

    @Inject
    Converters converters;
    @Inject
    Neo4jUplink uplink;

    public void onAddIndexEvent(@Observes
            @IndexEvent.WithAction(IndexEvent.Action.ADD) IndexEvent evt)
            throws Neo4jServerException {
        // first, create the index, to assume, the index exists
        createIndexForEvent(evt.getIndexName());

        // add the node to the index
        addNodeToIndex(evt);
    }

    public void onRemoveIndexEvent(
            @Observes
            @IndexEvent.WithAction(IndexEvent.Action.REMOVE) IndexEvent evt)
            throws Neo4jServerException {
        // first, create the index, to assume, the index exists
        createIndexForEvent(evt.getIndexName());

        // remove the node from the index
        removeNodeFromIndex(evt, false);
    }

    public void onReplaceIndexEvent(
            @Observes
            @IndexEvent.WithAction(IndexEvent.Action.REPLACE) IndexEvent evt)
            throws Neo4jServerException {
        // first, create the index, to assume, the index exists
        createIndexForEvent(evt.getIndexName());

        // remove the node from the index
        removeNodeFromIndex(evt, true);

        // add the node to the index
        addNodeToIndex(evt);
    }

    private void addNodeToIndex(
            IndexEvent evt)
            throws Neo4jServerException {
        for (Map.Entry<String, Object> property : evt.getProperties().entrySet()) {
            LOG.log(Level.FINER,
                    "add node with id: {0} to index with name: {1} using query: {2} = {3}",
                    new Object[]{
                        evt.getNodeId(),
                        evt.getIndexName(),
                        property.getKey(),
                        property.getValue()});
            uplink.addNodeToLegacyIndex(
                    evt.getIndexName(),
                    evt.getNodeId(),
                    property.getKey(),
                    converters.toString(property.getValue()));
        }
    }

    private void createIndexForEvent(String indexName) throws
            Neo4jServerException {
        LOG.log(Level.FINER, "create a legacy node index with name: {0}",
                indexName);
        // Note: Neo4j REST API is idempotent,
        // ... so creating an existing index will not produce any errors.
        uplink.createLegacyNodeIndex(indexName,
                                     Collections
                                     .singletonMap("type", (Object) "exact"));
    }

    private void removeNodeFromIndex(IndexEvent evt, boolean complete) throws
            Neo4jServerException {
        if (complete || evt.getProperties().isEmpty()) {
            LOG.log(Level.FINER,
                    "remove node with id: {0} from index with name: {1}",
                    new Object[]{
                        evt.getNodeId(),
                        evt.getIndexName()});
            uplink
                    .removeNodeFromLegacyIndex(evt.getIndexName(), evt
                                               .getNodeId());
        } else {
            for (Map.Entry<String, Object> property : evt.getProperties()
                    .entrySet()) {
                if (property.getValue() != null) {
                    LOG.log(Level.FINER,
                            "remove node with id: {0} from index with name: {1} and query: {2} = {3}",
                            new Object[]{
                                evt.getNodeId(),
                                evt.getIndexName(),
                                property.getKey(),
                                property.getValue()});
                    uplink.removeNodeFromLegacyIndex(
                            evt.getIndexName(),
                            evt.getNodeId(),
                            property.getKey(),
                            converters.toString(property.getValue()));
                } else {
                    LOG.log(Level.FINER,
                            "remove node with id: {0} from index with name: {1} and key: {2}",
                            new Object[]{
                                evt.getNodeId(),
                                evt.getIndexName(),
                                property.getKey()});
                    uplink.removeNodeFromLegacyIndex(
                            evt.getIndexName(),
                            evt.getNodeId(),
                            property.getKey());
                }
            }
        }
    }
}
