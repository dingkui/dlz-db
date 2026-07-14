package com.dlz.db.modal.options;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.options.point.OptionPointBindings;
import com.dlz.db.modal.options.point.StandardOptionPoints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 单次数据库操作解析后的不可变 Option 集合。 */
public final class DbOptions implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Map<DbOperation, DbOptions> EMPTY_BY_OPERATION = createEmptyOptions();
    public static final DbOptions EMPTY = EMPTY_BY_OPERATION.get(DbOperation.SELECT);

    private final DbOperation operation;
    private final Map<String, DbOption> options;
    private final OptionPointBindings pointBindings;

    private DbOptions(DbOperation operation, Map<String, DbOption> options, boolean bindPoints) {
        this.operation = operation;
        this.options = options;
        this.pointBindings = bindPoints ? OptionPointBindings.bind(this, StandardOptionPoints.REGISTRY) : null;
    }

    private static Map<DbOperation, DbOptions> createEmptyOptions() {
        Map<DbOperation, DbOptions> emptyOptions = new EnumMap<>(DbOperation.class);
        for (DbOperation operation : DbOperation.values()) {
            emptyOptions.put(operation, new DbOptions(operation, Collections.emptyMap(), true));
        }
        return Collections.unmodifiableMap(emptyOptions);
    }

    public static DbOptions resolve(DbOperation operation, DbOption... options) {
        if (operation == null) {
            throw new DbParameterException("operation must not be null");
        }
        if (options == null || options.length == 0) {
            return EMPTY_BY_OPERATION.get(operation);
        }
        Map<String, DbOption> resolved = new LinkedHashMap<>();
        for (DbOption option : options) {
            if (option == null) {
                throw new DbParameterException("DbOption must not be null");
            }
            if (!option.supports(operation)) {
                throw new DbParameterException(option.getClass().getSimpleName()
                        + "不适用于" + operation + "操作");
            }
            String key = option.key();
            if (key == null || key.trim().isEmpty()) {
                throw new DbParameterException(option.getClass().getName() + "的Option key不能为空");
            }
            DbOption previous = resolved.putIfAbsent(key, option);
            if (previous != null) {
                throw new DbParameterException("重复或冲突的DbOption: " + key);
            }
        }
        return new DbOptions(operation, Collections.unmodifiableMap(resolved), true);
    }

    public DbOperation getOperation() {
        return operation;
    }

    public OptionPointBindings getPointBindings() {
        return pointBindings == null
                ? OptionPointBindings.bind(this, StandardOptionPoints.REGISTRY)
                : pointBindings;
    }

    public boolean has(DbOption option) {
        if (option == null) {
            return false;
        }
        DbOption found = options.get(option.key());
        return option.equals(found);
    }

    public <T extends DbOption> T get(Class<T> type) {
        for (DbOption option : options.values()) {
            if (type.isInstance(option)) {
                return type.cast(option);
            }
        }
        return null;
    }

    public List<DbOption> asList() {
        return Collections.unmodifiableList(new ArrayList<>(options.values()));
    }

    public boolean isEmpty() {
        return options.isEmpty();
    }
}
