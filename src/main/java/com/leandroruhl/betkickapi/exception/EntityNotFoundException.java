package com.leandroruhl.betkickapi.exception;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when an entity is not found.
 * <p>
 * This exception is thrown when an attempt to retrieve an entity from the database fails because the entity does not exist.
 * It is a runtime exception, indicating that the requested entity was not found based on the provided search parameters.
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code EntityNotFoundException} with the specified entity class and search parameters.
     *
     * @param entityClass     the class of the entity that was not found
     * @param searchParamsMap the search parameters used to retrieve the entity
     */
    public EntityNotFoundException(Class<?> entityClass, String... searchParamsMap) {
        super(EntityNotFoundException.generateMessage(entityClass.getSimpleName(), toMap(searchParamsMap)));
    }

    /**
     * Generates the exception message based on the entity class and search parameters.
     *
     * @param entity       the name of the entity
     * @param searchParams the search parameters used to retrieve the entity
     * @return the formatted exception message
     */
    private static String generateMessage(String entity, Map<String, String> searchParams) {
        return StringUtils.capitalize(entity) + " was not found for parameters " + searchParams;
    }

    /**
     * Converts the search parameters into a map.
     *
     * @param entries the search parameters as key-value pairs
     * @return the map containing the search parameters
     * @throws IllegalArgumentException if the number of entries is not even
     */
    private static Map<String, String> toMap(String... entries) {
        if (entries.length % 2 == 1) // if number of entries is not even
            throw new IllegalArgumentException("Invalid entries");

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            String key = entries[i];
            String value = entries[i + 1];
            map.put(key, value);
        }
        return map;
    }

}