package com.leandroruhl.betkickapi.apierror;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.leandroruhl.betkickapi.apierror.suberror.SubError;
import com.leandroruhl.betkickapi.apierror.suberror.ValidationError;
import jakarta.validation.ConstraintViolation;
import lombok.Data;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents an API error response.
 * <p>
 * The {@code ApiError} class encapsulates information about an API error response, including the HTTP status code,
 * timestamp, error message, debug message, and any sub-errors associated with the main error. It provides various
 * constructors and utility methods to conveniently build and add different types of errors to the response.
 */
@Data
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, property = "error", visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class ApiError {
    private HttpStatusCode status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;
    private List<SubError> subErrors;

    /**
     * Default constructor.
     * <p>
     * Initializes the timestamp to the current date and time.
     */
    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    /**
     * Constructs an {@code ApiError} with the specified HTTP status.
     *
     * @param status the HTTP status code
     */
    public ApiError(HttpStatusCode status) {
        this();
        this.status = status;
    }

    /**
     * Constructs an {@code ApiError} with the specified HTTP status and exception.
     *
     * @param status the HTTP status code
     * @param ex     the exception that caused the error
     */
    public ApiError(HttpStatusCode status, Throwable ex) {
        this();
        this.status = status;
        this.message = "Unexpected error";
        this.debugMessage = ex.getLocalizedMessage();
    }

    /**
     * Constructs an {@code ApiError} with the specified HTTP status, error message, and exception.
     *
     * @param status  the HTTP status code
     * @param message the error message
     * @param ex      the exception that caused the error
     */
    public ApiError(HttpStatusCode status, String message, Throwable ex) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }

    /**
     * Adds a list of object errors to the list of sub-errors.
     *
     * @param globalErrors the list of object errors to add
     */
    public void addValidationError(List<ObjectError> globalErrors) {
        globalErrors.forEach(this::addValidationError);
    }

    /**
     * Adds a list of field errors to the list of sub-errors.
     *
     * @param fieldErrors the list of field errors to add
     */
    public void addValidationErrors(List<FieldError> fieldErrors) {
        fieldErrors.forEach(this::addValidationError);
    }

    /**
     * Adds a list of constraint violation errors to the list of sub-errors.
     *
     * @param constraintViolations the set of constraint violation errors to add
     */
    public void addValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
        constraintViolations.forEach(this::addValidationError);
    }

    /**
     * Adds a constraint violation error to the list of sub-errors.
     *
     * @param cv the constraint violation error
     */
    private void addValidationError(ConstraintViolation<?> cv) {
        this.addValidationError(
                cv.getRootBeanClass().getSimpleName(),
                ((PathImpl) cv.getPropertyPath()).getLeafNode().asString(),
                cv.getInvalidValue(),
                cv.getMessage());
    }

    /**
     * Adds a field error to the list of sub-errors.
     *
     * @param fieldError the field error to add
     */
    private void addValidationError(FieldError fieldError) {
        this.addValidationError(
                fieldError.getObjectName(),
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage());
    }

    /**
     * Adds an object error to the list of sub-errors.
     *
     * @param objectError the object error to add
     */
    private void addValidationError(ObjectError objectError) {
        this.addValidationError(
                objectError.getObjectName(),
                objectError.getDefaultMessage());
    }

    /**
     * Adds a validation error to the list of sub-errors.
     *
     * @param object        the object associated with the validation error
     * @param field         the field associated with the validation error
     * @param rejectedValue the rejected value associated with the validation error
     * @param message       the error message
     */
    private void addValidationError(String object, String field, Object rejectedValue, String message) {
        addSubError(new ValidationError(object, field, rejectedValue, message));
    }

    /**
     * Adds an object error to the list of sub-errors.
     *
     * @param object  the object associated with the validation error
     * @param message the error message
     */
    private void addValidationError(String object, String message) {
        addSubError(new ValidationError(object, message));
    }

    /**
     * Adds a sub-error to the list of sub-errors.
     *
     * @param subError the sub-error to add
     */
    private void addSubError(SubError subError) {
        if (subErrors == null) {
            subErrors = new ArrayList<>();
        }
        subErrors.add(subError);
    }
}