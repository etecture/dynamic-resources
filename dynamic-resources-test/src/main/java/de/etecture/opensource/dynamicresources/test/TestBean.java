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

import de.etecture.opensource.dynamicresources.api.accesspoints.MethodsForResponse;
import de.etecture.opensource.dynamicresources.api.accesspoints.Resources;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 *
 * @author rhk
 */
@Singleton
@Startup
public class TestBean {

    @Inject
    MethodsForResponse<TestResource> testResources;
    @Inject
    Resources allResources;

    @PostConstruct
    public void init(){
        try {
            createTestResource("1234567890", "blibla", "blubb");
            System.out.println("####################");
            System.out.println("lookup TestResources");
            TestResources trs = allResources.select(TestResources.class).get();
            System.out.println("result is: " + trs.getCount());
            System.out.println("lookup TestResource with id 1234567890");
            TestResource tr = testResources.pathParam("id", "1234567890")
                    .get();
            System.out.println("result is: " + tr.getFirstName() + " " + tr
                    .getLastName());
            System.out.println("####################");
        } catch (Throwable ex) {
            System.out.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @PreDestroy
    public void destruct() {
        try {
            deleteTestResource("1234567890");
        } catch (Throwable ex) {
            System.out.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public TestResource createTestResource(
            final String id,
            final String firstName,
            final String lastName) throws Throwable {

        TestResource testResource =
                new TestResourceImpl(id, firstName, lastName);

        return testResources.pathParam("id", id).put(testResource);
    }

    public TestResource findTestResource(String id) throws Throwable {
        return testResources.pathParam("id", id).get();
    }

    public void deleteTestResource(String id) throws Throwable {
        testResources.pathParam("id", id).delete();
    }
}
