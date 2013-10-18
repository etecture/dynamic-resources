package de.etecture.opensource.dynamicresources.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * represents the response object for a ReST request.
 *
 * @param <T>
 * @author rhk
 */
public class Response<T> {

    private final T entity;
    private int status;
    private final Map<String, List<Object>> header = new HashMap<>();

    public Response(T entity, int status) {
        this.entity = entity;
        this.status = status;
    }

    public T getEntity() {
        return entity;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void addHeader(String headerName, Object value) {
        List<Object> values = header.get(headerName);
        if (values == null) {
            values = new ArrayList<>();
            header.put(headerName, values);
        }
        values.add(value);
    }

    public List<Object> getHeader(String headerName) {
        if (header.containsKey(headerName)) {
            return Collections.unmodifiableList(header.get(headerName));
        } else {
            return Collections.emptyList();
        }
    }

    public Set<Map.Entry<String, List<Object>>> getHeaders() {
        return header.entrySet();
    }
}
