package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.ForEntity;
import de.etecture.opensource.dynamicresources.api.JSONWriter;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author rhk
 */
@ForEntity(Exception.class)
public class ExceptionJSONWriter implements JSONWriter {

    @Override
    public void process(Object element, JsonGenerator jg, UriInfo uriInfo) {
        Throwable ex = (Throwable) element;
        jg.writeStartObject();
        internalProcess(jg, ex);
        jg.writeEnd();
    }

    private void internalProcess(JsonGenerator jg, Throwable ex) {
        jg.write("exception", ex.getClass().getSimpleName());
        jg.write("message", ex.getMessage());
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
}
