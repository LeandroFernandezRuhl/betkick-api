package com.leandroruhl.betkickapi.apierror.suberror;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a validation error within the API error response.
 * <p>
 * The {@code ValidationError} class extends {@link SubError} and represents a validation error associated with
 * a specific object and field within the API error response. It contains information about the object, field,
 * rejected value, and error message. It provides constructors to conveniently create validation errors.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class ValidationError extends SubError {
    private String object;
    private String field;
    private Object rejectedValue;
    private String message;

    /**
     * Constructs an {@code ValidationError} with the specified object and error message.
     *
     * @param object  the object associated with the validation error
     * @param message the error message
     */
    public ValidationError(String object, String message) {
        this.object = object;
        this.message = message;
    }
}