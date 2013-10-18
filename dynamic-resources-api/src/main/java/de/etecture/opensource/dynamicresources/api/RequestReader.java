package de.etecture.opensource.dynamicresources.api;

import java.io.IOException;
import java.io.Reader;

/**
 * represents the reader that converts the request to an entity
 *
 * @author rhk
 */
public interface RequestReader<T> {

    /**
     * called by the dynamic resource service to read a corresponding entity
     *
     * @param reader
     * @param mediaType
     * @return
     * @throws IOException
     */
    T processRequest(Reader reader, String mediaType) throws IOException;
}
