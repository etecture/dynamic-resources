package de.etecture.opensource.dynamicresources.test;

import de.etecture.opensource.dynamicrepositories.api.EntityNotFoundException;
import de.etecture.opensource.dynamicresources.api.Entity;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collections;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

/**
 *
 * @author rhk
 */
@Produces(mimeType = "application/json")
public enum JSONTest implements ResponseWriter {

    @Entity(String.class)
    BLABLA {
        @Override
        protected void process(Object element, JsonGenerator generator) {
            generator.writeStartObject().write("test", element.toString())
                    .writeEnd();
        }
    },
    @Entity(EntityNotFoundException.class)
    NOT_FOUND {
        @Override
        protected void process(Object element, JsonGenerator generator) {
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
    @Entity(TestResources.class)
    TESTS {
        @Override
        protected void process(Object element, JsonGenerator generator) {
            generator.writeStartObject().write("count",
                    ((TestResources) element).getCount()).writeEnd();
        }
    },
    @Entity(TestResource.class)
    TEST {
        @Override
        protected void process(Object element, JsonGenerator generator) {
            TestResource resource = (TestResource) element;
            generator.writeStartObject();
            generator.write("id", resource.getId());
            generator.write("firstName", resource.getFirstName());
            generator.write("lastName", resource.getLastName());
            generator.writeEnd();
        }
    };
    private static final JsonGeneratorFactory JSON_FACTORY = Json
            .createGeneratorFactory(Collections.<String, Object>singletonMap(
            JsonGenerator.PRETTY_PRINTING, "true"));

    protected abstract void process(Object element, JsonGenerator generator);

    @Override
    public void processElement(Object element, Writer writer, MediaType mimetype)
            throws IOException {
        JsonGenerator jg = JSON_FACTORY.createGenerator(writer);
        process(element, jg);
        jg.flush();
        jg.close();
    }
}
