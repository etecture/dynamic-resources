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

import de.etecture.opensource.dynamicrepositories.api.annotations.Param;
import de.etecture.opensource.dynamicrepositories.executor.Technology;
import de.etecture.opensource.dynamicresources.api.HttpMethods;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.test.api.BodyGenerator;
import de.etecture.opensource.dynamicresources.test.api.Expect;
import de.etecture.opensource.dynamicresources.test.api.ParamSet;
import de.etecture.opensource.dynamicresources.test.api.ParamSets;
import de.etecture.opensource.dynamicresources.test.api.Request;
import de.etecture.opensource.dynamicresources.test.api.Response;
import de.etecture.opensource.dynamicresources.test.junit.ResourceTestRunner;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@RunWith(ResourceTestRunner.class)
@ParamSets({
    @ParamSet(
            name = ResourceTestIT.PARAM_ID,
            pathParameter =
            @Param(name = ResourceTestIT.PARAM_ID,
                   generator = RandomIdGenerator.class)),
    @ParamSet(
            name = ResourceTestIT.PARAMETER_SET,
            includes = ResourceTestIT.PARAM_ID,
            pathParameter = {
        @Param(name = ResourceTestIT.PARAM_FIRSTNAME,
               value = "Robert"),
        @Param(name = ResourceTestIT.PARAM_LASTNAME,
               value = "Herschke")})
})
@Technology("Neo4j")
public class ResourceTestIT {

    protected final static String PARAMETER_SET = "default";
    protected final static String PARAM_ID = "id";
    protected final static String PARAM_FIRSTNAME = "firstName";
    protected final static String PARAM_LASTNAME = "lastName";

    public static class TestResourceGenerator implements
            BodyGenerator<TestResource> {

        public TestResource generateBody(Request request,
                Map<String, Object> parameter) {
            return new TestResourceImpl(
                    (String) parameter.get(PARAM_ID),
                    (String) parameter.get(PARAM_FIRSTNAME),
                    (String) parameter.get(PARAM_LASTNAME));
        }
    }

    @Request(method = HttpMethods.GET,
             resource = TestResource.class,
             parameterSet = PARAMETER_SET)
    @Expect(status = StatusCodes.OK)
    public void testGetTestResource(
            @Response TestResource testResource,
            @Param(name = PARAM_ID) String id,
            @Param(name = PARAM_FIRSTNAME) String firstName,
            @Param(name = PARAM_LASTNAME) String lastName) {
        assertEquals(id, testResource.getId());
        assertEquals(firstName, testResource.getFirstName());
        assertEquals(lastName, testResource.getLastName());
    }

    @Request(method = HttpMethods.GET,
             resource = TestResources.class,
             beforeRequest = IndexConstructor.class,
             queryParameter =
            @Param(name = "query",
                   value = "name:Muster*"))
    @Expect(status = StatusCodes.OK)
    public void testQueryTestResources(
            @Response TestResources testResources) {
        assertNotNull(testResources);
        assertNotNull(testResources.getAllTestResources());
        assertFalse(testResources.getAllTestResources().isEmpty());
        assertEquals(2, testResources.getAllTestResources().size());
        Set<String> firstNames = new HashSet<String>();
        Set<String> lastNames = new HashSet<String>();
        for (TestResource tr : testResources.getAllTestResources()) {
            firstNames.add(tr.getFirstName());
            lastNames.add(tr.getLastName());
        }
        assertEquals(2, firstNames.size());
        assertTrue(firstNames.containsAll(Arrays.asList("Max", "Manuela")));
        assertEquals(2, lastNames.size());
        assertTrue(lastNames.containsAll(Arrays.asList("Mustermann",
                "Musterfrau")));
    }

    @Request(method = HttpMethods.PUT,
             resource = TestResource.class,
             bodyGenerator = TestResourceGenerator.class,
             parameterSet = PARAMETER_SET)
    @Expect(status = StatusCodes.CREATED)
    public void testPutTestResource_testResourceDoesNotExists(
            @Response TestResource testResource,
            @Param(name = PARAM_ID) String id,
            @Param(name = PARAM_FIRSTNAME) String firstName,
            @Param(name = PARAM_LASTNAME) String lastName) {
        assertEquals(id, testResource.getId());
        assertEquals(firstName, testResource.getFirstName());
        assertEquals(lastName, testResource.getLastName());
    }

    @Request(method = HttpMethods.PUT,
             resource = TestResource.class,
             bodyGenerator = TestResourceGenerator.class,
             parameterSet = PARAMETER_SET)
    @Expect(status = StatusCodes.CREATED)
    public void testPutTestResource_testResourceAlreadyExists(
            @Response TestResource testResource,
            @Param(name = PARAM_ID) String id,
            @Param(name = PARAM_FIRSTNAME) String firstName,
            @Param(name = PARAM_LASTNAME) String lastName) {
        assertEquals(id, testResource.getId());
        assertEquals(firstName, testResource.getFirstName());
        assertEquals(lastName, testResource.getLastName());
    }

    @Request(method = HttpMethods.DELETE,
             resource = TestResource.class,
             parameterSet = PARAMETER_SET)
    public void testDeleteTestResource_testResourceDoesExists(
            @Response de.etecture.opensource.dynamicresources.api.Response<Boolean> response,
            @Param(name = "whatever",
                   value = "1234",
                   type = Long.class) long justForDemonstrationPurpose) {
        assertEquals("wrong status-code - ", StatusCodes.NO_CONTENT, response
                .getStatus());
        assertEquals(1234l, justForDemonstrationPurpose);
    }
}
