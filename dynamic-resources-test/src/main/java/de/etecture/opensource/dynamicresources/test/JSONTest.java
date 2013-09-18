package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.api.EntityNotFoundException;
import de.etecture.opensource.dynamicresources.api.ForEntity;
import de.etecture.opensource.dynamicresources.api.JSONWriter;
import java.io.Serializable;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author rhk
 */
public enum JSONTest implements JSONWriter {

    @ForEntity(String.class)
    BLABLA {
        @Override
        public void process(Object element, JsonGenerator generator,
                UriInfo uriInfo) {
            generator.writeStartObject().write("test", element.toString())
                    .writeEnd();
        }
    },
    @ForEntity(EntityNotFoundException.class)
    NOT_FOUND {
        @Override
        public void process(Object element, JsonGenerator generator,
                UriInfo uriInfo) {
            final EntityNotFoundException enfe =
                    (EntityNotFoundException) element;
            final Serializable entityId =
                    enfe.getEntityId();
            generator.writeStartObject()
                    .write("id", entityId.toString())
                    .write("entity", enfe.getEntityClass().getSimpleName())
                    .writeEnd();
        }
    },
    @ForEntity(TestResources.class)
    TESTS {
        @Override
        public void process(Object element, JsonGenerator generator,
                UriInfo uriInfo) {
            generator.writeStartObject().write("count",
                    ((TestResources) element).getCount()).writeEnd();
        }
    },
    @ForEntity(TestResource.class)
    TEST {
        @Override
        public void process(Object element, JsonGenerator generator,
                UriInfo uriInfo) {
            TestResource resource = (TestResource) element;
            generator.writeStartObject();
            generator.write("id", resource.getId());
            generator.write("firstName", resource.getFirstName());
            generator.write("lastName", resource.getLastName());
            generator.writeEnd();
        }
    }
}
