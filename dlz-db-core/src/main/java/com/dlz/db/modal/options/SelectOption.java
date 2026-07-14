package com.dlz.db.modal.options;

import com.dlz.db.modal.options.point.DeletedDataPoint;
import com.dlz.db.modal.options.point.SelectLockPoint;
import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.DeletedDataMode;
import com.dlz.db.modal.options.point.context.SelectLockMode;

/** 查询操作内置选项。 */
public class SelectOption implements DbOption {
    public static final SelectOption INCLUDE_DELETED = new IncludeDeletedOption();
    public static final SelectOption FOR_UPDATE = new ForUpdateOption();

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

    private static final class IncludeDeletedOption extends SelectOption implements DeletedDataPoint {
        private IncludeDeletedOption() {
            super("select.deleted", "INCLUDE_DELETED");
        }

        @Override
        public DeletedDataMode chooseDeletedData(CrudContext context) {
            return DeletedDataMode.INCLUDE;
        }
    }

    private static final class ForUpdateOption extends SelectOption implements SelectLockPoint {
        private ForUpdateOption() {
            super("select.lock", "FOR_UPDATE");
        }

        @Override
        public SelectLockMode chooseSelectLock(CrudContext context) {
            return SelectLockMode.FOR_UPDATE;
        }
    }
}
