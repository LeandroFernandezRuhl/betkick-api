package com.leandroruhl.betkickapi.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;

/**
 * The AbstractPersistableEntity class serves as a base class for entities
 * with a persistent identity.
 *
 * <p>Extending this class prevents JPA from making unnecessary select statements
 * when inserting an entity that already has its own ID.
 *
 * @param <T> The type of the entity's identifier.
 */
@MappedSuperclass
public abstract class AbstractPersistableEntity<T extends Serializable>
        implements Persistable<T> {

    @Transient
    private boolean isNew = true;

    /**
     * Checks whether the entity is new (has not been persisted yet).
     *
     * @return {@code true} if the entity is new, {@code false} otherwise.
     */
    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Sets the flag indicating whether the entity is new.
     *
     * @param isNew {@code true} if the entity is new, {@code false} otherwise.
     */
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Marks the entity as not new after it has been loaded or persisted.
     */
    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}
