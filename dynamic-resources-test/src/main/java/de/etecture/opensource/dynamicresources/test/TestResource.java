package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicresources.api.DELETE;
import de.etecture.opensource.dynamicresources.api.GET;
import de.etecture.opensource.dynamicresources.api.PUT;
import de.etecture.opensource.dynamicresources.api.Resource;

/**
 *
 * @author rhk
 */
@Resource("/data/test/{id}")
@GET(
        description = "retrieves a TestResource with the given id",
        technology = "Neo4j",
        query = ""
        + "MATCH "
        + "  (this:Test) "
        + "WHERE "
        + "  this.id = {id} "
        + "RETURN "
        + "  this.id AS `id`, "
        + "  this.firstName AS `firstName`, "
        + "  this.lastName AS `lastName`")
@PUT(
        description = "updates a TestResource with the given id",
        technology = "Neo4j",
        query = ""
        + "MERGE "
        + "  (this:Test {"
        + "    id: {id} "
        + "  })"
        + "ON CREATE this SET "
        + "  this.firstName = {firstName}, "
        + "  this.lastName = {lastName}, "
        + "  this.`_created` = timestamp(), "
        + "  this.`_updated` = timestamp() "
        + "ON MATCH this SET "
        + "  this.firstName = {firstName}, "
        + "  this.lastName = {lastName}, "
        + "  this.`_updated` = timestamp() "
        + "RETURN "
        + "  this.id AS `id`, "
        + "  this.firstName AS `firstName`, "
        + "  this.lastName AS `lastName`")
@DELETE(
        description = "deletes a TestResource with the given id",
        technology = "Neo4j",
        query = ""
        + "MATCH "
        + "  (this:Test) "
        + "WHERE "
        + "  this.id = {id} "
        + "DELETE "
        + "  this")
public interface TestResource {

    String getId();

    String getFirstName();

    String getLastName();
}
