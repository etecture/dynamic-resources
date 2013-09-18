package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicresources.api.DELETE;
import de.etecture.opensource.dynamicresources.api.GET;
import de.etecture.opensource.dynamicresources.api.POST;
import de.etecture.opensource.dynamicresources.api.Resource;
import java.util.List;

/**
 *
 * @author rhk
 */
@Resource("/data/test")
@GET(
        description = "retrieves all existing testResources",
        technology = "Neo4j",
        query = ""
        + "MATCH "
        + "  (this:Test) "
        + "RETURN "
        + "  COUNT(this) AS `count`, collect(this) AS `allTestResources`")
@POST(
        description = "creates a new TestResources",
        technology = "Neo4j",
        query = ""
        + "CREATE "
        + "  (this:Test { "
        + "    id: {id}, "
        + "    firstName: {firstName}, "
        + "    lastName: {lastName}, "
        + "    `_created`: timestamp(), "
        + "    `_updated`: timestamp() "
        + "  }) "
        + "WITH this AS newly "
        + "MATCH "
        + "  (this:Test) "
        + "RETURN "
        + "  collect(this) AS `allTestResources`")
@DELETE(
        description = "deletes all TestResources",
        technology = "Neo4j",
        query = ""
        + "MATCH "
        + "  (this:Test) "
        + "DELETE "
        + "  this")
public interface TestResources {

    int getCount();

    List<TestResource> getAllTestResources();
}
