package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicresources.api.Resources;
import javax.annotation.PostConstruct;
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
    Resources<TestResources> testResourcesList;
    @Inject
    Resources<TestResource> testResources;

    @PostConstruct
    public void init() {
        System.out.println("####################");
        System.out.println("lookup TestResources");
        TestResources trs = testResourcesList.GET();
        System.out.println("result is: " + trs.getCount());
        System.out.println("lookup TestResource with id 1234567890");
        TestResource tr = testResources.select("id", "1234567890").GET();
        System.out.println("result is: " + tr.getFirstName() + " " + tr
                .getLastName());
        System.out.println("####################");
    }

    public TestResource createTestResource(
            final String id,
            final String firstName,
            final String lastName) {

        TestResource testResource = new TestResource() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getFirstName() {
                return firstName;
            }

            @Override
            public String getLastName() {
                return lastName;
            }
        };

        return testResources.select("id", id).PUT(testResource);
    }

    public TestResource findTestResource(String id) {
        return testResources.select("id", id).GET();
    }
}
