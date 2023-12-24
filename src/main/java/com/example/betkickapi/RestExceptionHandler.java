package com.example.betkickapi;

import com.example.betkickapi.apierror.ApiError;
import com.example.betkickapi.exception.AccountBalanceException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URISyntaxException;

import static org.springframework.http.HttpStatus.*;

/**
 * Global exception handler for RESTful API.
 * <p>
 * The {@code RestExceptionHandler} class provides centralized exception handling for common exceptions that may occur
 * during the processing of RESTful API requests. It extends the {@link ResponseEntityExceptionHandler} class and is
 * annotated with {@link ControllerAdvice} to allow global exception handling across multiple controllers. It uses the
 * {@link Order} annotation to set the status of this exception handler to be the highest.
 *
 * <p>This class handles various exceptions by overriding the respective methods provided by {@link ResponseEntityExceptionHandler}.
 * It builds a response entity with an {@link ApiError} object that encapsulates the error details and status code. The
 * response entity is then returned to the client in the desired format, such as JSON.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Builds a ResponseEntity that wraps an {@link ApiError}.
     *
     * @param apiError the ApiError
     * @return the ApiError object wrapped in a ResponseEntity
     */
    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * Handles for {@link MissingServletRequestParameterException}.
     * Triggered when a 'required' request parameter is missing.
     *
     * @param ex      the MissingServletRequestParameterException to be handled
     * @param headers the headers to use for the response
     * @param status  the status code to use for the response
     * @param request the current request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";
        return buildResponseEntity(new ApiError(status, error, ex));
    }


    /**
     * Handles {@link HttpMediaTypeNotSupportedException}. Triggered when a client POSTs,
     * PUTs, or PATCHes content of a type not supported by request handler.
     *
     * @param ex      the HttpMediaTypeNotSupportedException to be handled
     * @param headers the headers to use for the response
     * @param status  the status code to use for the response
     * @param request the current request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        return buildResponseEntity(new ApiError(status, builder.substring(0, builder.length() - 2), ex));
    }

    /**
     * Handles {@link MethodArgumentNotValidException}. Triggered when validation on an argument annotated with {@link Valid} fails.
     *
     * @param ex      the MethodArgumentNotValidException to be handled
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ApiError apiError = new ApiError(status);
        apiError.setMessage("Validation error");
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
        return buildResponseEntity(apiError);
    }

    /**
     * Handles {@link HttpMessageNotReadableException}. Triggered when the request has a malformed or invalid body.
     *
     * @param ex      the HttpMessageNotReadableException to be handled
     * @param headers the headers to use for the response
     * @param status  the status code to use for the response
     * @param request the current request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        log.info("{} to {}", servletWebRequest.getHttpMethod(), servletWebRequest.getRequest().getServletPath());
        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiError(status, error, ex));
    }

    /**
     * Handles {@link HttpMessageNotWritableException}. Triggered if the server encounters
     * an error while serializing an object into the desired format, such as JSON or XML.
     *
     * @param ex      the HttpMessageNotWritableException to be handled
     * @param headers the headers to use for the response
     * @param status  the status code to use for the response
     * @param request the current request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String error = "Error writing JSON output";
        return buildResponseEntity(new ApiError(status, error, ex));
    }

    /**
     * Handles {@link NoHandlerFoundException}. Triggered if the requested URL does not match any defined
     * routes or mappings in the web application or there is a missing or misconfigured handler or controller.
     *
     * @param ex      the NoHandlerFoundException to be handled
     * @param headers the headers to use for the response
     * @param status  the status code to use for the response
     * @param request the current request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ApiError apiError = new ApiError(status);
        apiError.setMessage(
                String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL()));
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * Handles {@link ConstraintViolationException}. Thrown when {@link Validated} fails.
     *
     * @param ex the ConstraintViolationException to be handled
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Validation error");
        apiError.addValidationErrors(ex.getConstraintViolations());
        return buildResponseEntity(apiError);
    }

    /**
     * Handles {@link jakarta.persistence.EntityNotFoundException}. Triggered when an operation attempts to find an entity
     * by a certain identifier or criteria, but the entity is not found in the data store.
     *
     * @param ex the EntityNotFoundException to be handled
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(jakarta.persistence.EntityNotFoundException ex) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * Handles {@link DataIntegrityViolationException} by inspecting the cause for different database causes.
     * If the cause is a {@link ConstraintViolationException}, specific constraint violations are handled based on the constraint name.
     *
     * @param ex      the DataIntegrityViolationException to be handled
     * @param request the current web request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {
        if (ex.getCause() instanceof ConstraintViolationException constraintViolationException) {
            String constraintName = constraintViolationException.getConstraintName();
            String errorMessage = "Constraint violation";

            // Handle specific constraint violations based on constraint name
            if (constraintName.equals("fk_showtime_movie")) {
                errorMessage = "Cannot delete the movie because it is referenced by one or several showtimes";
            }
            return buildResponseEntity(new ApiError(CONFLICT, errorMessage, ex.getCause()));
        }
        return buildResponseEntity(new ApiError(INTERNAL_SERVER_ERROR, ex));
    }

    /**
     * Handles {@link MethodArgumentTypeMismatchException}. Thrown when a method argument fails type conversion.
     *
     * @param ex      the MethodArgumentTypeMismatchException to be handled
     * @param request the current request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName()));
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * Handles {@link URISyntaxException}. Triggered when a URI syntax error occurs.
     *
     * @param ex      the URISyntaxException to be handled
     * @param request the current request
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @ExceptionHandler(URISyntaxException.class)
    protected ResponseEntity<Object> handleURISyntaxException(
            URISyntaxException ex,
            WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Invalid URI syntax");
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * Handles {@link IllegalStateException}.
     * Triggered when an illegal state is encountered.
     * Returns a BAD_REQUEST response with the error message.
     *
     * @param ex the IllegalStateException to be handled
     * @return the ApiError object wrapped in a ResponseEntity
     */
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(AccountBalanceException.class)
    protected ResponseEntity<Object> handleShowtimeOverlap(AccountBalanceException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }
}