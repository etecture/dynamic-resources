/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.etecture.opensource.dynamicresources.api;

import java.nio.charset.Charset;

/**
 *
 * @author rhk
 */
public interface MediaType {

    String alternative();

    String category();

    Charset encoding();

    boolean isCompatibleTo(String... mediaTypes);

    boolean isCompatibleTo(MediaType... mediaTypes);

    String space();

    String subType();

    Version version();
}
