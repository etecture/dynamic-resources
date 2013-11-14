dynamic-resources
=================

Dynamically create ReST-Resources by using CDI


Features
--------

* hides the resource-implementation and http-request handling

* automatically resolve the resource-implementation for the http-request

* automatically execute the desired query

* automatically marshall the query-result to the resource-type

* automatically resolve the desired reader for the given MIME and version

* automatically resolve the desired writer for the given MIME and version

* supports version number ranges

* supports combined MIME types (e.g. `application/vnd.mewa.customer.v1.0.1+xml`) 

* automatically checks the security-constraints (e.g. `isUserInRole()`)

* accept different query technologies (for even more encapsulation)

* automatically handle the `OPTIONS` http-request.

* provides an URI-Builder to link between resources

* provides Exception-Handlers (similiar to jaxrs/jersey)

* provides Resource-Interceptors for manipulating resource-handling

* ...


Introduction
------------

See: [this presentation](https://github.com/etecture/dynamic-resources/blob/master/ETECTURE%20-%20Dynamic%20Resources%20Framework.pdf) for details.


Example Resource
----------------

~~~~~
@Resource(
  uri = "/customers/{id}",
  methods = {
    @Method(
      name = "GET", query = @Query(
         "MATCH (c:Customer) ..."
      )
    ), @Method(
      name = "PUT",
      query = @Query(
         "CREATE (c:Customer) ..."
      )
    ) 
  }
)
public interface Customer {
  
  String getId();
  
  String getName();
  
  Address getAddress();

  // ... 
}￼
~~~~~

Resource resolution and handling:
---------------------------------

* parses the URI

* looks up the Resource-Type (an annotated interface) for this URI

* is there a ResourceMethodHandler for the http request method?

* is there a Method defined for the Resource-Type that match the http request? ! does the Accept-Header match with the produced MIME-Types?

* does the Content-Type-Header match with the consumed MIME-Types?

* are the security constraints redeemed?

* resolves the RequestReader and read the request body to build the request object

* build the query parameters (path-params, uri-params, request object)

* looks up and executes the query
  (the result is converted in a proxy-instance of the Resource-Type)

* resolves the ResponseWriter and write the response to the http response body


