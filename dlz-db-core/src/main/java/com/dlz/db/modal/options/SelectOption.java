package com.dlz.db.modal.options;

/** 查询操作内置选项。 */
public final class SelectOption implements DbOption {
    public static final SelectOption INCLUDE_DELETED = new SelectOption("select.deleted", "INCLUDE_DELETED");
    public static final SelectOption FOR_UPDATE = new SelectOption("select.lock", "FOR_UPDATE");

    private final String key;
    private final String name;

    private SelectOption(String key, String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public boolean supports(DbOperation operation) {
        return operation == DbOperation.SELECT;
    }

    @Override
    public String toString() {
        return name;
    }
}
