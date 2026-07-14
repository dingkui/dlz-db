package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.DbOperation;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Immutable metadata for one registered option point. */
public final class OptionPointDefinition<P extends OptionPoint> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Class<P> pointType;
    private final OptionPointMode mode;
    private final Set<DbOperation> operations;

    OptionPointDefinition(Class<P> pointType, OptionPointMode mode, Set<DbOperation> operations) {
        this.pointType = pointType;
        this.mode = mode;
        this.operations = Collections.unmodifiableSet(EnumSet.copyOf(operations));
    }

    public Class<P> getPointType() {
        return pointType;
    }

    public OptionPointMode getMode() {
        return mode;
    }

    public Set<DbOperation> getOperations() {
        return operations;
    }

    public boolean supports(DbOperation operation) {
        return operation != null && operations.contains(operation);
    }
}
