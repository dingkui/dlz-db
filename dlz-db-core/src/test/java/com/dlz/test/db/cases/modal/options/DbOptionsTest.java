package com.dlz.test.db.cases.modal.options;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.options.DbOperation;
import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.options.DbOptions;
import com.dlz.db.modal.options.DeleteOption;
import com.dlz.db.modal.options.InsertOption;
import com.dlz.db.modal.options.SelectOption;
import com.dlz.db.modal.options.point.DeleteModePoint;
import com.dlz.db.modal.options.point.DeletedDataPoint;
import com.dlz.db.modal.options.point.InsertNullFieldPoint;
import com.dlz.db.modal.options.point.InsertValuePoint;
import com.dlz.db.modal.options.point.SelectLockPoint;
import com.dlz.db.modal.options.point.UpdateSafetyPoint;
import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.DeleteMode;
import com.dlz.db.modal.options.point.context.DeletedDataMode;
import com.dlz.db.modal.options.point.context.SelectLockMode;
import com.dlz.db.modal.options.point.context.ValueContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbOptionsTest {

    @Test
    void shouldResolveSupportedOptions() {
        DbOptions options = DbOptions.resolve(
                DbOperation.SELECT,
                SelectOption.INCLUDE_DELETED,
                SelectOption.FOR_UPDATE);

        assertTrue(options.has(SelectOption.INCLUDE_DELETED));
        assertTrue(options.has(SelectOption.FOR_UPDATE));
        assertEquals(2, options.asList().size());
    }

    @Test
    void shouldRejectNullOption() {
        assertThrows(DbParameterException.class,
                () -> DbOptions.resolve(DbOperation.SELECT, (DbOption) null));
    }

    @Test
    void shouldRejectUnsupportedOption() {
        assertThrows(DbParameterException.class,
                () -> DbOptions.resolve(DbOperation.SELECT, InsertOption.IGNORE_NULL));
    }

    @Test
    void shouldRejectDuplicateOrConflictingOptions() {
        assertThrows(DbParameterException.class,
                () -> DbOptions.resolve(
                        DbOperation.INSERT,
                        InsertOption.IGNORE_NULL,
                        InsertOption.INCLUDE_NULL));
        assertThrows(DbParameterException.class,
                () -> DbOptions.resolve(
                        DbOperation.INSERT,
                        InsertOption.IGNORE_NULL,
                        InsertOption.IGNORE_NULL));
    }

    @Test
    void shouldSupportCustomPluginOption() {
        DbOption custom = new DbOption() {
        };

        DbOptions options = DbOptions.resolve(DbOperation.UPDATE, custom);

        assertEquals(custom, options.get(custom.getClass()));
    }

    @Test
    void shouldExecuteBuiltInOptionsThroughConcretePoints() {
        DbOptions select = DbOptions.resolve(DbOperation.SELECT,
                SelectOption.INCLUDE_DELETED, SelectOption.FOR_UPDATE);
        CrudContext selectContext = new CrudContext(DbOperation.SELECT, "test_table", null, select);

        DeletedDataPoint deletedData = select.getPointBindings().single(DeletedDataPoint.class);
        SelectLockPoint selectLock = select.getPointBindings().single(SelectLockPoint.class);
        assertEquals(DeletedDataMode.INCLUDE, deletedData.chooseDeletedData(selectContext));
        assertEquals(SelectLockMode.FOR_UPDATE, selectLock.chooseSelectLock(selectContext));

        DbOptions delete = DbOptions.resolve(DbOperation.DELETE, DeleteOption.PHYSICAL);
        DeleteModePoint deleteMode = delete.getPointBindings().single(DeleteModePoint.class);
        assertEquals(DeleteMode.PHYSICAL, deleteMode.chooseDeleteMode(
                new CrudContext(DbOperation.DELETE, "test_table", null, delete)));
    }

    @Test
    void shouldKeepBuiltInOptionCompatibilityApi() {
        assertEquals("select.deleted", SelectOption.INCLUDE_DELETED.key());
        assertEquals("select.lock", SelectOption.FOR_UPDATE.key());
        assertEquals("delete.mode", DeleteOption.PHYSICAL.key());
        assertTrue(SelectOption.INCLUDE_DELETED.supports(DbOperation.SELECT));
        assertTrue(DeleteOption.LOGIC.supports(DbOperation.DELETE));
    }

    @Test
    void shouldCacheEmptyOptionsByOperation() {
        DbOptions first = DbOptions.resolve(DbOperation.INSERT);
        DbOptions second = DbOptions.resolve(DbOperation.INSERT, new DbOption[0]);

        assertEquals(DbOperation.INSERT, first.getOperation());
        assertTrue(first == second);
        assertTrue(first.getPointBindings().single(InsertNullFieldPoint.class) == null);
    }

    @Test
    void shouldPreClassifyStandardPointBindings() {
        InsertValueOption option = new InsertValueOption();

        DbOptions options = DbOptions.resolve(DbOperation.INSERT, option);

        assertEquals(DbOperation.INSERT, options.getOperation());
        assertEquals(option, options.getPointBindings().chain(InsertValuePoint.class).get(0));
    }

    @Test
    void shouldRejectPointOutsideOperationMetadata() {
        DbOption option = new DbOption() {
            @Override
            public String key() {
                return "invalid-update-safety";
            }
        };
        UpdateSafetyPoint point = new UpdateSafetyPoint() {
            @Override
            public boolean allowUnsafeUpdate(CrudContext context) {
                return false;
            }
        };
        DbOption invalid = new UpdateSafetyDbOption(option, point);

        assertThrows(DbParameterException.class,
                () -> DbOptions.resolve(DbOperation.SELECT, invalid));
    }

    private static final class InsertValueOption implements DbOption, InsertValuePoint {
        @Override
        public String key() {
            return "insert-value-test";
        }

        @Override
        public Object convertInsertValue(ValueContext context) {
            return context.getValue();
        }
    }

    private static final class UpdateSafetyDbOption implements DbOption, UpdateSafetyPoint {
        private final DbOption option;
        private final UpdateSafetyPoint point;

        private UpdateSafetyDbOption(DbOption option, UpdateSafetyPoint point) {
            this.option = option;
            this.point = point;
        }

        @Override
        public String key() {
            return option.key();
        }

        @Override
        public boolean allowUnsafeUpdate(CrudContext context) {
            return point.allowUnsafeUpdate(context);
        }
    }
}
