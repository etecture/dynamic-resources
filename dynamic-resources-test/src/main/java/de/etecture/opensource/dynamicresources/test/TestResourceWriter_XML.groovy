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

import de.etecture.opensource.dynamicresources.api.MediaType
import de.etecture.opensource.dynamicresources.api.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import groovy.xml.MarkupBuilder

/**
 *
 * @author rhk
 */
@Produces(contentType = TestResource.class, mimeType = ["application/xml",
        "text/xml"])
class TestResourceWriter_XML implements ResponseWriter<TestResource> {

    def addressesToXml(xml, tr) {
        xml.addresses() {[
           address(street:'blabla'), 
           address(street:'cxxxxx') 
        ]}
    }

    def nameToXml(xml, tr) {
        xml.'first-name'(tr.getFirstName())
        xml.'last-name'(tr.getLastName())
    }

    void processElement(TestResource tr, Writer writer, MediaType mimetype) throws IOException {
        def addresses = [ ]
        def xml = new MarkupBuilder(writer)
        xml.testResource(id:tr.getId()) {
            nameToXml(xml, tr)            
            addressesToXml(xml, tr)
            groovy('rocks!')
        }
    }

    int getContentLength(TestResource tr, MediaType mime) {
        return -1;
    }
}

