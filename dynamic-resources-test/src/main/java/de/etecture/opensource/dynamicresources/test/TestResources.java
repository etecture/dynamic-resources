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

import de.etecture.opensource.dynamicrepositories.api.annotations.Query;
import de.etecture.opensource.dynamicresources.annotations.declaration.Consumes;
import de.etecture.opensource.dynamicresources.annotations.declaration.Filter;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.etecture.opensource.dynamicresources.annotations.declaration.Method;
import de.etecture.opensource.dynamicresources.annotations.declaration.Resource;
import java.util.List;

/**
 *
 * @author rhk
 */
@Resource(uri = "/data/test",
          methods = {
    @Method(
            name = HttpMethods.GET,
            filters =
            @Filter(
            name = "query",
            defaultValue = "*"),
            query =
            @Query(
            technology = "Neo4j",
            statement = ""
            + "start this=node:testIndex({query}) "
            + "WITH "
            + "  CASE WHEN HAS(this.id) THEN "
            + "    {id: this.id, firstName: this.firstName, lastName: this.lastName}"
            + "  END AS t "
            + "RETURN "
            + "    collect(t) AS `allTestResources`")),
    @Method(
            name = HttpMethods.POST,
            description = "creates a new TestResources",
            consumes =
            @Consumes(
            requestType = TestResource.class),
            seeOther = TestResource.class,
            query =
            @Query(
            technology = "Neo4j",
            statement = ""
            + "CREATE "
            + "  (this:Test { "
            + "    id: {request}.id, "
            + "    firstName: {request}.firstName, "
            + "    lastName: {request}.lastName, "
            + "    `_created`: timestamp(), "
            + "    `_updated`: timestamp() "
            + "  }) "
            + "WITH this AS newly "
            + "MATCH "
            + "  (this:Test) "
            + "RETURN "
            + "  'id' as paramName, this.id as paramValue")),
    @Method(
            name = HttpMethods.DELETE,
            description = "deletes all TestResources",
            query =
            @Query(
            technology = "Neo4j",
            statement = ""
            + "MATCH "
            + "  (this:Test) "
            + "DELETE "
            + "  this"))})
public interface TestResources {

    int getCount();

    List<TestResource> getAllTestResources();
}
