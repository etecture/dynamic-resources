package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.spi.Technology;
import de.etecture.opensource.dynamicrepositories.spi.ConnectionResolver;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import de.herschke.neo4j.uplink.cdi.Remote;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@Technology("Neo4j")
public class SampleConnectionResolver implements ConnectionResolver<Neo4jUplink> {

    @Inject
    @Remote(url = "http://localhost:17474/db/data")
    Neo4jUplink uplink;

    @Override
    public Neo4jUplink getConnection(String name) {
        return uplink;
    }
}
