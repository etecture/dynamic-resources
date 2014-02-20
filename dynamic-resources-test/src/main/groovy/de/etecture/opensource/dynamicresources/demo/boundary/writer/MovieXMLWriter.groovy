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

package de.etecture.opensource.dynamicresources.demo.boundary.movies.writer

import de.etecture.opensource.dynamicresources.api.ResponseWriter
import de.etecture.opensource.dynamicresources.demo.boundary.movies.Movie
import de.etecture.opensource.dynamicresources.annotations.Produces
import de.etecture.opensource.dynamicresources.api.MediaType
import java.io.Writer
import groovy.xml.MarkupBuilder

/**
 *
 * @author rhk
 */
@Produces(contentType = Movie.class, mimeType = ["application/vnd.etecture.demo.movie+xml"])
class MovieXMLWriter implements ResponseWriter<Movie> {
	
    
    public int getContentLength(Movie movie, MediaType mediatype) {
        return -1;
    }
    
    public void processElement(Movie _movie, Writer writer, MediaType mediatype) {
        def xml = new MarkupBuilder(writer)
        xml.movie() {
            title(_movie.getTitle())
            'tag-line'(_movie.getTagline())
            actors() {
                _movie.getRoles().each { _actor, _roles ->
                    xml.actor() {
                        name(_actor.getName())
                        _roles.each {_role ->
                            xml.role(_role)        
                        }
                    }
                    
                }
            }
            producers() {
                _movie.getProducers().each { _producer ->
                    xml.producer(_producer.getName())
                }
            }
            directors() {
                _movie.getDirectors().each { _director ->
                    xml.director(_director.getName())
                }
            }
            editors() {
                _movie.getEditors().each { _editor ->
                    xml.editor(_editor.getName())
                }
            }
        }
    }
    
}

