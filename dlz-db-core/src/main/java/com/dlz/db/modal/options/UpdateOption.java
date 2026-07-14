package com.dlz.db.modal.options;

import com.dlz.db.modal.options.point.UpdateNullFieldPoint;
import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.NullFieldMode;

/** 更新操作内置选项。 */
public final class UpdateOption implements DbOption, UpdateNullFieldPoint {
    public static final UpdateOption IGNORE_NULL = new UpdateOption("update.null", "IGNORE_NULL", NullFieldMode.IGNORE);
    public static final UpdateOption INCLUDE_NULL = new UpdateOption("update.null", "INCLUDE_NULL", NullFieldMode.INCLUDE);

    private final String key;
    private final String name;
    private final NullFieldMode nullFieldMode;

    private UpdateOption(String key, String name, NullFieldMode nullFieldMode) {
        this.key = key;
        this.name = name;
        this.nullFieldMode = nullFieldMode;
    }

    @Override
    public NullFieldMode chooseUpdateNullFields(CrudContext context) {
        return nullFieldMode;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public boolean supports(DbOperation operation) {
        return operation == DbOperation.UPDATE;
    }

    @Override
    public String toString() {
        return name;
    }
}
