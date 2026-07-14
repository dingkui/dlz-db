package com.dlz.db.modal.options;

import com.dlz.db.modal.options.point.DeleteModePoint;
import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.DeleteMode;

/** 删除操作内置选项。 */
public final class DeleteOption implements DbOption, DeleteModePoint {
    public static final DeleteOption LOGIC = new DeleteOption("delete.mode", "LOGIC", DeleteMode.LOGICAL);
    public static final DeleteOption PHYSICAL = new DeleteOption("delete.mode", "PHYSICAL", DeleteMode.PHYSICAL);

    private final String key;
    private final String name;
    private final DeleteMode mode;

    private DeleteOption(String key, String name, DeleteMode mode) {
        this.key = key;
        this.name = name;
        this.mode = mode;
    }

    @Override
    public DeleteMode chooseDeleteMode(CrudContext context) {
        return mode;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public boolean supports(DbOperation operation) {
        return operation == DbOperation.DELETE;
    }

    @Override
    public String toString() {
        return name;
    }
}
