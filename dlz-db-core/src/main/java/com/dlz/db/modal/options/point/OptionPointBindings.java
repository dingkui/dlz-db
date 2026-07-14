package com.dlz.db.modal.options.point;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.options.DbOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable, pre-classified option buckets for one wrapper binding.
 * Runtime point execution only performs a map lookup and never scans option interfaces.
 */
public final class OptionPointBindings implements Serializable {
    private static final long serialVersionUID = 1L;

    private final OptionPointRegistry registry;
    private final Map<Class<? extends OptionPoint>, List<DbOption>> buckets;

    private OptionPointBindings(OptionPointRegistry registry,
                                Map<Class<? extends OptionPoint>, List<DbOption>> buckets) {
        this.registry = registry;
        this.buckets = buckets;
    }

    public static OptionPointBindings bind(DbOptions options, OptionPointRegistry registry) {
        if (options == null) {
            throw new DbParameterException("DbOptions must not be null");
        }
        if (registry == null) {
            throw new DbParameterException("OptionPointRegistry must not be null");
        }

        Map<Class<? extends OptionPoint>, List<DbOption>> classified = new LinkedHashMap<>();
        for (OptionPointDefinition<?> definition : registry.asList()) {
            List<DbOption> matches = match(options, definition.getPointType());
            if (!matches.isEmpty() && !definition.supports(options.getOperation())) {
                throw new DbParameterException("Option point " + definition.getPointType().getName()
                        + " is not applicable to " + options.getOperation());
            }
            if (definition.supports(options.getOperation())) {
                validateAndSort(definition, matches);
                classified.put(definition.getPointType(),
                        Collections.unmodifiableList(new ArrayList<>(matches)));
            } else {
                classified.put(definition.getPointType(), Collections.emptyList());
            }
        }
        return new OptionPointBindings(registry, Collections.unmodifiableMap(classified));
    }

    public OptionPointRegistry getRegistry() {
        return registry;
    }

    public <P extends OptionPoint> P single(Class<P> pointType) {
        requireMode(pointType, OptionPointMode.SINGLE);
        List<DbOption> options = bucket(pointType);
        return options.isEmpty() ? null : pointType.cast(options.get(0));
    }

    public <P extends OptionPoint> List<P> chain(Class<P> pointType) {
        requireMode(pointType, OptionPointMode.CHAIN);
        return typedBucket(pointType);
    }

    public <P extends OptionPoint> List<P> merge(Class<P> pointType) {
        requireMode(pointType, OptionPointMode.MERGE);
        return typedBucket(pointType);
    }

    public boolean isEmpty(Class<? extends OptionPoint> pointType) {
        return bucket(pointType).isEmpty();
    }

    private static List<DbOption> match(DbOptions options, Class<? extends OptionPoint> pointType) {
        List<DbOption> matches = new ArrayList<>();
        for (DbOption option : options.asList()) {
            if (pointType.isInstance(option)) {
                matches.add(option);
            }
        }
        return matches;
    }

    private static void validateAndSort(OptionPointDefinition<?> definition, List<DbOption> matches) {
        if (definition.getMode() == OptionPointMode.SINGLE && matches.size() > 1) {
            throw new DbParameterException("Multiple DbOptions bound to SINGLE point: "
                    + definition.getPointType().getName());
        }
        if (definition.getMode() != OptionPointMode.SINGLE) {
            Collections.sort(matches, Comparator.comparingInt(option -> ((OptionPoint) option).order()));
        }
    }

    private void requireMode(Class<? extends OptionPoint> pointType, OptionPointMode expected) {
        OptionPointDefinition<?> definition = requireDefinition(pointType);
        if (definition.getMode() != expected) {
            throw new IllegalArgumentException("Option point " + pointType.getName()
                    + " uses " + definition.getMode() + ", not " + expected);
        }
    }

    private OptionPointDefinition<?> requireDefinition(Class<? extends OptionPoint> pointType) {
        if (pointType == null) {
            throw new IllegalArgumentException("pointType must not be null");
        }
        OptionPointDefinition<?> definition = registry.get(pointType);
        if (definition == null) {
            throw new IllegalArgumentException("Option point is not registered: " + pointType.getName());
        }
        return definition;
    }

    private List<DbOption> bucket(Class<? extends OptionPoint> pointType) {
        requireDefinition(pointType);
        return buckets.get(pointType);
    }

    private <P extends OptionPoint> List<P> typedBucket(Class<P> pointType) {
        List<DbOption> options = bucket(pointType);
        List<P> result = new ArrayList<>(options.size());
        for (DbOption option : options) {
            result.add(pointType.cast(option));
        }
        return Collections.unmodifiableList(result);
    }
}
