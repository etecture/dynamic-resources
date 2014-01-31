/*
 *  This file is part of the ETECTURE Open Source Community Projects.
 * 
 *  Copyright (c) 2013 by:
 * 
 *  ETECTURE GmbH
 *  Darmstädter Landstraße 112
 *  60598 Frankfurt
 *  Germany
 * 
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 * 
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 * 
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 * 
 *  3. Neither the name of the author nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package de.etecture.opensource.dynamicresources.test

import de.etecture.opensource.dynamicresources.api.Consumes
import groovy.util.XmlSlurper
import org.xml.sax.SAXParseException
import groovy.json.JsonBuilder
import de.etecture.opensource.dynamicresources.api.RequestReader
import org.json.simple.JSONAware

/**
 *
 * @author rhk
 */
@Consumes(requestType = TestResource.class,
    mimeType = ["application/xml", "text/xml"],
    version = "1.0.0")
class TestResourceReader_XML implements RequestReader<TestResource> {

    interface JSONTestResource extends TestResource, JSONAware {}
    
    public TestResource processRequest(Reader reader, String mediaType) throws IOException {
        println("read TestResource from xml.")
        try {
            def ct = new XmlSlurper().parse(reader)
            return [getId: {ct.'@id'.text()}, getFirstName: {ct.'first-name'.text()}, getLastName: {ct.'last-name'.text()}] as TestResource
        } catch(SAXParseException ex) {
            return null;
        }
    }
}

