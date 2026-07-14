package com.dlz.db.modal.options;

import com.dlz.db.modal.options.point.InsertNullFieldPoint;
import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.NullFieldMode;

/** 插入操作内置选项。 */
public final class InsertOption implements DbOption, InsertNullFieldPoint {
    public static final InsertOption IGNORE_NULL = new InsertOption("insert.null", "IGNORE_NULL", NullFieldMode.IGNORE);
    public static final InsertOption INCLUDE_NULL = new InsertOption("insert.null", "INCLUDE_NULL", NullFieldMode.INCLUDE);

    private final String key;
    private final String name;
    private final NullFieldMode nullFieldMode;

    private InsertOption(String key, String name, NullFieldMode nullFieldMode) {
        this.key = key;
        this.name = name;
        this.nullFieldMode = nullFieldMode;
    }

    @Override
    public NullFieldMode chooseInsertNullFields(CrudContext context) {
        return nullFieldMode;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public boolean supports(DbOperation operation) {
        return operation == DbOperation.INSERT;
    }

    @Override
    public String toString() {
        return name;
    }
}
