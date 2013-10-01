package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Entity;
import de.etecture.opensource.dynamicresources.api.MediaType;
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

/**
 *
 * @author rhk
 */
@Entity(Exception.class)
@Produces(mimeType = "application/json")
public class ExceptionJSONWriter implements ResponseWriter<Throwable> {

    private static final JsonGeneratorFactory JSON_FACTORY = Json
            .createGeneratorFactory(Collections.<String, Object>singletonMap(
            JsonGenerator.PRETTY_PRINTING, "true"));

    private void internalProcess(JsonGenerator jg, Throwable ex) {
        jg.write("exception", ex.getClass().getSimpleName());
        if (ex.getMessage() == null) {
            jg.writeNull("message");
        } else {
            jg.write("message", ex.getMessage());
        }
        jg.writeStartArray("trace");
        for (StackTraceElement ste : ex.getStackTrace()) {
            jg.writeStartObject();
            jg.write("class", ste.getClassName());
            if (ste.getFileName() != null) {
                jg.write("file", ste.getFileName());
            }
            if (ste.getMethodName() != null) {
                jg.write("method", ste.getMethodName());
            }
            jg.write("line", ste.getLineNumber());
            jg.writeEnd();
        }
        jg.writeEnd();
        if (ex.getCause() != null) {
            jg.writeStartObject("cause");
            internalProcess(jg, ex.getCause());
            jg.writeEnd();
        }
    }

    @Override
    public void processElement(Throwable element, Writer writer,
            MediaType mimetype) throws
            IOException {
        JsonGenerator jg = JSON_FACTORY.createGenerator(writer);
        jg.writeStartObject();
        internalProcess(jg, element);
        jg.writeEnd();
        jg.flush();
        jg.close();
    }
}
