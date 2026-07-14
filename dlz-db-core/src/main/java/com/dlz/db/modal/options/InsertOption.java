package com.dlz.db.modal.options;

/** 插入操作内置选项。 */
public final class InsertOption implements DbOption {
    public static final InsertOption IGNORE_NULL = new InsertOption("insert.null", "IGNORE_NULL");
    public static final InsertOption INCLUDE_NULL = new InsertOption("insert.null", "INCLUDE_NULL");

    private final String key;
    private final String name;

    private InsertOption(String key, String name) {
        this.key = key;
        this.name = name;
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
