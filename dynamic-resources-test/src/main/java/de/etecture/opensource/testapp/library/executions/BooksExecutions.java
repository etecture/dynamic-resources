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
package de.etecture.opensource.testapp.library.executions;

import de.etecture.opensource.dynamicresources.annotations.Application;
import de.etecture.opensource.dynamicresources.annotations.Body;
import de.etecture.opensource.dynamicresources.annotations.Executes;
import de.etecture.opensource.dynamicresources.annotations.Method;
import de.etecture.opensource.dynamicresources.annotations.Resource;
import de.etecture.opensource.dynamicresources.annotations.Succeed;
import de.etecture.opensource.dynamicresources.api.ExecutionContext;
import de.etecture.opensource.dynamicresources.api.HttpHeaders;
import de.etecture.opensource.dynamicresources.api.ResourceException;
import de.etecture.opensource.dynamicresources.api.StatusCodes;
import de.etecture.opensource.dynamicresources.api.accesspoints.ApplicationAccessor;
import de.etecture.opensource.dynamicresources.api.accesspoints.Applications;
import de.etecture.opensource.dynamicresources.api.accesspoints.TypedResourceAccessor;
import de.etecture.opensource.dynamicresources.api.events.AfterExecutionEvent;
import de.etecture.opensource.dynamicresources.metadata.ApplicationNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceMethodNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResourceNotFoundException;
import de.etecture.opensource.dynamicresources.metadata.ResponseTypeNotSupportedException;
import de.etecture.opensource.testapp.library.Author;
import de.etecture.opensource.testapp.library.Book;
import de.etecture.opensource.testapp.library.Books;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import de.herschke.neo4j.uplink.api.Node;
import de.herschke.neo4j.uplink.cdi.Remote;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author rhk
 * @version
 * @since
 */
@Singleton
@Startup
public class BooksExecutions {

    @Inject
    @Remote(url = "http://localhost:17474/db/data")
    Neo4jUplink uplink;
    @Inject
    @Resource(name = "Author")
    TypedResourceAccessor<Author> accessor;
    @Inject
    @Application(name = "Library")
    ApplicationAccessor app;
    //@Inject
    //@URI("/library/authors/1234567890")
    //TypedResourceAccessor<Author> blabla;
    //@Inject
    //@URI("/library/books/1234567890")
    //ResourceAccessor xxxxaBook;
    @Inject
    Applications applications;

    @PostConstruct
    void init() {
        try {
            System.out.println(applications.selectByName("Library")
                    .selectByName("Books").getMetadata().getName());
            System.out.println(applications.selectByName("Library")
                    .selectByName("Author").select(Author.class).getMetadata()
                    .getName());
            System.out.println(applications.selectByName("Library")
                    .selectByName("Author").select(Author.class).method("GET")
                    .getMetadata().getStatusCode());
        } catch (ResourceNotFoundException | ApplicationNotFoundException |
                ResponseTypeNotSupportedException |
                ResourceMethodNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Executes(resource = "Book",
              method = "POST")
    public Book createNewBook(ExecutionContext<Books, Book> context,
            @Body Book newBook) throws Neo4jServerException, ResourceException {
        Node bookNode = uplink.buildNode()
                .history()
                .creator()
                .property("title", newBook.getTitle())
                .property("subTitle", newBook.getSubTitle())
                .property("ISBN", newBook.getISBN())
                .label("Book")
                .create();

//        for (Map.Entry<Author, Set<AuthorRole>> e : newBook.getAuthors()
//                .entrySet()) {
//            Author author = (Author) accessor.pathParam("id", Long.toString(
//                    e.getKey().getId())).select(Author.class).put(e.getKey());
//            for (AuthorRole role : e.getValue()) {
//                uplink.buildRelationship()
//                        .creator()
//                        .history()
//                        .property("role", role.name())
//                        .create(bookNode.getId(), e.getKey().getId(), "WROTE_BY");
//            }
//
//        }
        return newBook;
    }

    public void afterPostNewBook(
            @Observes
            @Succeed
            @Resource(name = "Book")
            @Method(name = "POST") AfterExecutionEvent event) {
        String location = event.getExecutionContext().getResourceMethod()
                .getResource().getApplication().getResources().get("Books")
                .getPath().buildCompleteUri(((Book) event.getOriginalEntity())
                .getISBN());
        event.setNewEntity(null);
        event.setStatusCode(StatusCodes.SEE_OTHER);
        event.addHeaderValue(HttpHeaders.LOCATION, location);
    }
}
