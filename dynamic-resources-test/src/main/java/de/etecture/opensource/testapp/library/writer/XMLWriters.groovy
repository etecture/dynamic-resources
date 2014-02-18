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

package de.etecture.opensource.testapp.library.writer;

import de.etecture.opensource.testapp.library.Book;
import de.etecture.opensource.testapp.library.Books;
import de.etecture.opensource.dynamicresources.api.MediaType
import de.etecture.opensource.dynamicresources.annotations.Produces;
import de.etecture.opensource.dynamicresources.api.ResponseWriter;
import groovy.xml.MarkupBuilder

/**
 *
 * @author rhk
 */
@Produces(contentType = Books.class, mimeType = ["application/xml",
        "text/xml"])
class BooksWriter_XML implements ResponseWriter<Books> {

    void processElement(Books _books, Writer writer, MediaType mimetype) throws IOException {
        def xml = new MarkupBuilder(writer)
        xml.books(count:_books.getBooks().size()) {
        
            _books.getBooks().each {
                _book -> book(isbn:_book.getISBN())
            } 
        }
    }

    int getContentLength(Books books, MediaType mime) {
        return -1;
    }
}

@Produces(contentType = Book.class, mimeType = ["application/xml", "text/xml"])
class BookWriter_XML implements ResponseWriter<Book> {
    
    void processElement(Book _book, Writer writer, MediaType mimetype) throws IOException {
        def xml = new MarkupBuilder(writer)
        xml.book(isbn:_book.getISBN()) {
            title(_book.getTitle())
            "sub-title"(_book.getSubTitle())
        }
    }

    int getContentLength(Book book, MediaType mime) {
        return -1;
    }
}