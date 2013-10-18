package de.etecture.opensource.dynamicresources.api;

/**
 * handles exceptions.
 *
 * @author rhk
 */
public interface ExceptionHandler {

    /**
     * called by the dynamic resource service to check, if this exception
     * handler is responsible for a specific type of exceptions.
     * <p>
     * implementors must return true, if the dynamic resource service should use
     * this exception handler by calling it's
     * {@link ExceptionHandler#handleException(java.lang.Class, java.lang.String, java.lang.Throwable)}
     * method.
     *
     * @param resourceClass
     * @param method
     * @param exceptionClass
     * @return
     */
    boolean isResponsibleFor(Class<?> resourceClass, String method,
            Class<? extends Throwable> exceptionClass);

    /**
     * called by the dynamic resource service when an exception was thrown while
     * requesting a resource.
     * <p>
     * implementors must return an instance of {@link Response} that is later
     * writen to the desired media type with the corresponding
     * {@link ResponseWriter}
     *
     * @param resourceClass the class of the resource that was requested.
     * @param method the method of the request
     * @param exception the exception that was raised
     * @return
     */
    Response<?> handleException(Class<?> resourceClass, String method,
            Throwable exception);
}
