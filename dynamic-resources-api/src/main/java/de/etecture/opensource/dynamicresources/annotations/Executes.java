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
package de.etecture.opensource.dynamicresources.annotations;

import de.etecture.opensource.dynamicrepositories.api.annotations.ParamName;
import de.etecture.opensource.dynamicresources.api.Response;
import de.etecture.opensource.dynamicresources.api.ResponseException;
import de.etecture.opensource.dynamicresources.contexts.ExecutionContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * Declares any bean method as an execution handler for a resource.
 * <p>
 * The method must match the following specification:
 * <ul>
 * <li>The method <b>MUST</b> be an accessible member of a CDI bean, so that the
 * framework is able to call this method. Any CDI beans are allowed. Dependent
 * on the scope of the bean, the bean is created before the method will be
 * executed to handle a resource method request.
 * <li>The method <b>MUST NOT</b> be
 * <code>static</code> nor
 * <code>final</code>. It <i>should</i> be
 * <code>public</code>, but <i>may</i> be
 * <code>protected</code> or
 * <code>private</code>. (allthough this is not recommend.)
 * <li>If the method returns any value, this value is used as the response
 * entity of the resource methid execution.
 * <li>If the method returns void, the response entity is
 * <code>null</code>.
 * <li>The method may return an instance of {@link Response} instead of an
 * entity
 * <li>The method may throw any exception. All execption types other then
 * {@link ResponseException} will be handled as response entity. A
 * {@link ResponseException} is handled directly.
 * <li>The method may contain exactly one argument with a type that is a
 * subclass of {@link ExecutionContext}. If the method is called, then this
 * argument will contain the current ExecutionContext.
 * <li>The method may contain arguments annotated with &#64;{@link ParamName}.
 * If the method is called, then these arguments will contain the parameter
 * values for an execution context parameter with the specified name. If no such
 * parameter was defined for the execution context, then the value of the
 * argument will be
 * <code>null</code>.
 * <li>The method may contain exactly one argument annotated with
 * &#64;{@link Body}. If the method is called, then this argument will contain
 * the request body object.
 * </ul>
 * <hr>
 * <p>
 * Here are examples of &#64;{@link Executes} methods:
 * <pre>
 * &#64;Executes
 * public void simplestPossibleExecutorMethod();
 * </pre> This is the simplest possible executor method. It handles ALL kind of
 * resource executions.
 * <hr>
 * <pre>
 * &#64;Executes(resource="Customer", method="GET")
 * public Customer &#47;*(1)*&#47; getCustomer(
 *   QueryExecutionContext &#47;*(2)*&#47; context,
 *   &#64;ParamName("id") String id) throws CustomerNotFoundException;
 * </pre> This executor method handles all 'GET' requests to the 'Customer'
 * resource. It is responsible to handle query-based executions and it assumes
 * that a (either path- or query/filter-) parameter with name 'id' is provided
 * and given within the
 * <code>id</code> argument of the method.
 * <dl><dt>Hint (1):</dt><dd>Due to the method returns a value of type
 * <code>Customer</code>, the actual qualifier for this method is extended to
 * only handle response types of this type. This means, that
 * <pre>&#64;Executes(responseType=Customer.class, ... ) </pre> is optional in
 * this case.</dd>
 * <dt>Hint (2):</dt><dd>Due to the argument
 * <code>QueryExecutionContext context</code>, this method only handles requests
 * with this type of execution. This means, that
 * <pre>&#64;Executes(contextType=QueryExecutionContext.class, ...)</pre> is
 * optional in this case.</dd></dl>
 * <hr>
 * <pre>
 * &#64;Executes(application="CustomerServices", resource="Customers", method="PUT|POST")
 * public void createOrAddCustomer(&#64;Body Customer newCustomer);
 * </pre> This method handles requests of the 'Customers' resource for either
 * 'PUT' or 'POST' request methods. Additionally, it assumes, that the method is
 * called with the request body in the
 * <code>Customer newCustomer</code> argument.
 *
 * @author rhk
 * @version
 * @since
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Executes {

    static interface AnyExecutionContext extends
            ExecutionContext<Object, Object> {
    }

    /**
     * the type of the ExecutionContext, this handler is responsible for.
     * <p>
     * may be overridden when the annotated method contains an argument that is
     * a subtype of {@link ExecutionContext}.
     *
     * @return
     */
    Class<? extends ExecutionContext<?, ?>> contextType() default AnyExecutionContext.class;

    /**
     * the name pattern of the method, this handler is responsible for.
     * <p>
     * the pattern is a either a regular expression or a distinct method name.
     * <p>
     * defaults to '.*' which means, any method name matches.
     *
     * @return
     */
    String method() default ".*";

    /**
     * the name pattern of the resource, this handler is responsible for.
     * <p>
     * the pattern is either a regular expression or a distinct resource name.
     * <p>
     * defaults to '.*' which means, any resource name matches.
     *
     * @return
     */
    String resource() default ".*";

    /**
     * the name pattern of the application, this handler is responsible for.
     * <p>
     * the pattern is either a regular expression or a distinct application
     * name.
     * <p>
     * defaults to '.*' which means, any application name matches.
     *
     * @return
     */
    String application() default ".*";

    /**
     * the type of the response, this handler is responsible for.
     * <p>
     * defaults to {@link Object}, so any response type is handled by this
     * handler.
     * <p>
     * if the annotated method contains a return type, then the return type of
     * the method overrides the value of this annotation parameter.
     *
     * @return
     */
    Class<?> responseType() default Object.class;

    /**
     * the type of the request, this handler is responsible for.
     * <p>
     * defaults to {@link Object}, so any request type is handled by this
     * handler.
     * <p>
     * if the annotated method contains an argument, annotated with
     * &#64;{@link Body}, then the type of this argument overrides the value of
     * this annotation parameter.
     *
     * @return
     */
    Class<?> requestType() default Object.class;
}
