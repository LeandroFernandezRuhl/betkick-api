package com.leandroruhl.betkickapi.apierror;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.leandroruhl.betkickapi.apierror.suberror.ValidationError;

/**
 * Custom TypeIdResolver implementation that resolves the type identifier using the lowercased simple name of the value's class.
 * <p>
 * The LowerCaseClassNameResolver is responsible for generating the type identifier for subclasses of SubError
 * (like {@link ValidationError}  when they are serialized as part of the ApiError object.
 * It ensures that the appropriate subclass type is retained during serialization and can be correctly mapped back during deserialization.
 * <p>
 * By using the custom type identifier resolver, the ApiError class can handle polymorphic behavior in its list of SubError objects.
 */
public class LowerCaseClassNameResolver extends TypeIdResolverBase {
    /**
     * Returns the type identifier for the given value.
     *
     * @param value the value for which to determine the type identifier
     * @return the type identifier as a lowercased simple name of the value's class
     */
    @Override
    public String idFromValue(Object value) {
        return value.getClass().getSimpleName().toLowerCase();
    }

    /**
     * Returns the type identifier for the given value and suggested type.
     *
     * @param value         the value for which to determine the type identifier
     * @param suggestedType the suggested type to consider during type identifier resolution (unused in this implementation)
     * @return the type identifier as a lowercased simple name of the value's class
     */
    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    /**
     * Returns the mechanism used for type identification, which is CUSTOM in this case.
     *
     * @return the type identification mechanism as JsonTypeInfo.Id.CUSTOM
     */
    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}