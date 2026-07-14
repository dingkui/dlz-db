package com.dlz.db.modal.options;

/** 更新操作内置选项。 */
public final class UpdateOption implements DbOption {
    public static final UpdateOption IGNORE_NULL = new UpdateOption("update.null", "IGNORE_NULL");
    public static final UpdateOption INCLUDE_NULL = new UpdateOption("update.null", "INCLUDE_NULL");

    private final String key;
    private final String name;

    private UpdateOption(String key, String name) {
        this.key = key;
        this.name = name;
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
