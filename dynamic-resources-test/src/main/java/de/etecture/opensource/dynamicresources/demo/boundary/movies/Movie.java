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
package de.etecture.opensource.dynamicresources.demo.boundary.movies;

import de.etecture.opensource.dynamicrepositories.api.annotations.Query;
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Methods;
import de.etecture.opensource.dynamicresources.annotations.Resource;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@Resource(name = "MovieResource",
          path = "/movies/{title}",
          description = "A resource describing a plain movie")
@Methods(
        @Method(name = HttpMethods.GET,
                description = "get the movie with the title.",
                query = @Query(
                        technology = "Neo4j",
                        statement = ""
                        + "MATCH "
                        + "  (m:Movie {title : {title}}) "
                        + "OPTIONAL MATCH "
                        + "  (d:Person)-[:DIRECTED]->(m) "
                        + "OPTIONAL MATCH "
                        + "  (p:Person)-[:PRODUCED]->(m) "
                        + "OPTIONAL MATCH "
                        + "  (w:Person)-[:WROTE]->(m) "
                        + "OPTIONAL MATCH "
                        + "  (a:Person)-[r:ACTED_IN]->(m) "
                        + "WITH "
                        + "  m AS m, "
                        + "  CASE WHEN HAS(d.name) THEN {name: d.name} END AS d, "
                        + "  CASE WHEN HAS(p.name) THEN {name: p.name} END AS p, "
                        + "  CASE WHEN HAS(w.name) THEN {name: w.name} END AS w, "
                        + "  {key: {name: a.name}, value: r.roles} AS x "
                        + "RETURN "
                        + "  m.title AS title, "
                        + "  m.tagline AS tagline, "
                        + "  m.released AS released, "
                        + "  COLLECT(d) AS directors, "
                        + "  COLLECT(p) AS producers, "
                        + "  COLLECT(w) AS editors, "
                        + "  COLLECT(x) AS roles "
                )
        )
)
public interface Movie {

    String getTitle();

    String getTagline();

    String getReleased();

    Map<Person, Set<String>> getRoles();

    Set<Person> getDirectors();

    Set<Person> getProducers();

    Set<Person> getEditors();
}
