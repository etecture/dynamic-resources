/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.api.Query;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.etecture.opensource.dynamicresources.api.Method;
import de.etecture.opensource.dynamicresources.api.Resource;

/**
 *
 * @author rhk
 */
@Resource(uri = "/data/test/{id}",
          methods = {
    @Method(
            name = HttpMethods.GET,
            description = "retrieves a TestResource with the given id",
            interceptors = {SpecificResourceInterceptor.class}),
    @Method(
            name = HttpMethods.PUT,
            description = "updates a TestResource with the given id",
            query =
            @Query(
            technology = "Neo4j",
            value = ""
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
            + "  this.lastName AS `lastName`")),
    @Method(
            name = HttpMethods.DELETE,
            description = "deletes a TestResource with the given id",
            query =
            @Query(
            technology = "Neo4j",
            value = ""
            + "MATCH "
            + "  (this:Test) "
            + "WHERE "
            + "  this.id = {id} "
            + "DELETE "
            + "  this"))})
public interface TestResource {

    String getId();

    String getFirstName();

    String getLastName();
}
