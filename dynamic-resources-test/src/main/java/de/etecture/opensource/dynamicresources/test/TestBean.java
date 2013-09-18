package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicresources.api.Resources;
import javax.ejb.Stateless;

/**
 *
 * @author rhk
 */
@Stateless
public class TestBean {

    //@Inject
    Resources<TestResource> testResources;

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
