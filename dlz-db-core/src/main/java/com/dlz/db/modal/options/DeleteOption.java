package com.dlz.db.modal.options;

/** 删除操作内置选项。 */
public final class DeleteOption implements DbOption {
    public static final DeleteOption LOGIC = new DeleteOption("delete.mode", "LOGIC");
    public static final DeleteOption PHYSICAL = new DeleteOption("delete.mode", "PHYSICAL");

    private final String key;
    private final String name;

    private DeleteOption(String key, String name) {
        this.key = key;
        this.name = name;
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
