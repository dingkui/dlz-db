package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.DbOperation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Immutable registry of stable option points and their processing modes. */
public final class OptionPointRegistry implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final OptionPointRegistry EMPTY = builder().build();

    private final Map<Class<? extends OptionPoint>, OptionPointDefinition<?>> definitions;

    private OptionPointRegistry(Map<Class<? extends OptionPoint>, OptionPointDefinition<?>> definitions) {
        this.definitions = definitions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public OptionPointDefinition<?> get(Class<? extends OptionPoint> pointType) {
        return definitions.get(pointType);
    }

    public List<OptionPointDefinition<?>> asList() {
        return Collections.unmodifiableList(new ArrayList<>(definitions.values()));
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    public static final class Builder {
        private final Map<Class<? extends OptionPoint>, OptionPointDefinition<?>> definitions =
                new LinkedHashMap<>();

        public <P extends OptionPoint> Builder register(Class<P> pointType, OptionPointMode mode) {
            return register(pointType, mode, DbOperation.values());
        }

        public <P extends OptionPoint> Builder register(Class<P> pointType, OptionPointMode mode,
                                                         DbOperation... operations) {
            if (pointType == null) {
                throw new IllegalArgumentException("pointType must not be null");
            }
            if (!pointType.isInterface() || pointType == OptionPoint.class) {
                throw new IllegalArgumentException("pointType must be an OptionPoint sub-interface");
            }
            if (mode == null) {
                throw new IllegalArgumentException("mode must not be null");
            }
            if (operations == null || operations.length == 0) {
                throw new IllegalArgumentException("operations must not be empty");
            }
            Set<DbOperation> supported = EnumSet.noneOf(DbOperation.class);
            for (DbOperation operation : operations) {
                if (operation == null) {
                    throw new IllegalArgumentException("operation must not be null");
                }
                supported.add(operation);
            }
            if (definitions.containsKey(pointType)) {
                throw new IllegalArgumentException("Option point already registered: " + pointType.getName());
            }
            definitions.put(pointType, new OptionPointDefinition<>(pointType, mode, supported));
            return this;
        }

        public OptionPointRegistry build() {
            Map<Class<? extends OptionPoint>, OptionPointDefinition<?>> copy =
                    new LinkedHashMap<>(definitions);
            return new OptionPointRegistry(Collections.unmodifiableMap(copy));
        }
    }
}
