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

import de.etecture.opensource.dynamicrepositories.api.Query;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import static org.junit.Assert.*;

/**
 *
 * @author rhk
 * @version
 * @since
 */
public class ResourceTestTest {

    private final static String TECHNOLOGY = "NEO4J";
    private final static String ID = "1234567890";

    @ResourceTest(method = HttpMethods.GET,
                  pathParameter =
            @Parameter(name = "id",
                       value = ID))
    @PrepareDatabase(
            @Query(
            technology = TECHNOLOGY,
            value =
            "MERGE (t:Test {id: '1234567890', firstName: 'Robert', lastName: 'Herschke'}) RETURN count(t) = 1"))
    @CleanupDatabase(
            @Query(
            technology = TECHNOLOGY,
            value =
            "MATCH (t:Test {id: '1234567890'}) DELETE t RETURN count(*)=1"))
    public void testGetTestResource(@Response TestResource testResource) {
        assertEquals(ID, testResource.getId());
        assertEquals("Robert", testResource.getFirstName());
        assertEquals("Herschke", testResource.getLastName());
    }

    @ResourceTest(method = HttpMethods.PUT,
                  pathParameter =
            @Parameter(name = "id",
                       value = ID))
    @PrepareDatabase(
            @Query(
            technology = TECHNOLOGY,
            value =
            "MATCH (t:Test {id: '1234567890'}) DELETE t RETURN true"))
    @CleanupDatabase(
            @Query(
            technology = TECHNOLOGY,
            value =
            "MATCH (t:Test {id: '1234567890'}) DELETE t RETURN count(*)=1"))
    public void testPutTestResource_testResourceDoesNotExists(
            @Response TestResource testResource) {
        assertEquals(ID, testResource.getId());
        assertEquals("Robert", testResource.getFirstName());
        assertEquals("Herschke", testResource.getLastName());
    }

    @ResourceTest(method = HttpMethods.PUT,
                  pathParameter =
            @Parameter(name = "id",
                       value = ID))
    @PrepareDatabase(
            @Query(
            technology = TECHNOLOGY,
            value =
            "MERGE (t:Test {id: '1234567890', firstName: 'Robert', lastName: 'Herschke'}) RETURN count(t) = 1"))
    @CleanupDatabase(
            @Query(
            technology = TECHNOLOGY,
            value =
            "MATCH (t:Test {id: '1234567890'}) DELETE t RETURN count(*)=1"))
    public void testPutTestResource_testResourceAlreadyExists(
            @Response TestResource testResource) {
        assertEquals(ID, testResource.getId());
        assertEquals("Robert", testResource.getFirstName());
        assertEquals("Herschke", testResource.getLastName());
    }

    @ResourceTest(method = HttpMethods.PUT,
                  pathParameter =
            @Parameter(name = "id",
                       value = ID))
    @PrepareDatabase(
            @Query(
            technology = TECHNOLOGY,
            value =
            "MERGE (t:Test {id: '1234567890', firstName: 'Robert', lastName: 'Herschke'}) RETURN count(t) = 1"))
    @CleanupDatabase(
            @Query(
            technology = TECHNOLOGY,
            value =
            "MATCH (t:Test {id: '1234567890'}) DELETE t RETURN count(*)=1"))
    public void testDeleteTestResource_testResourceDoesExists() {
    }
}
