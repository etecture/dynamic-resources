
package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.spi.ConnectionResolver;
import de.etecture.opensource.dynamicrepositories.spi.Technology;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.enterprise.inject.Default;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@Singleton
@Technology("Neo4j")
@Default
public class DefaultNeo4jConnectionResolver implements
        ConnectionResolver<Neo4jUplink> {

    @EJB
    Neo4jUplink uplink;

    @Override
    public Neo4jUplink getConnection(String name) {
        return uplink;
    }
}
