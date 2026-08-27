package com.dlz.test.db.cases.modal.options;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.option.*;
import com.dlz.db.option.point.*;
import com.dlz.db.option.point.context.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        InsertFieldOption option = new InsertFieldOption();

        DbOptions options = DbOptions.resolve(DbOperation.INSERT, option);

        assertEquals(DbOperation.INSERT, options.getOperation());
        assertEquals(option, options.getPointBindings().merge(InsertFieldPoint.class).get(0));
    }

    @Test
    void shouldSkipPointOutsideOperationMetadata() {
        DbOption crossOperation = new InsertNullOption();

        // 跨操作 Option 实现了不属于当前操作的桩点时跳过而非报错
        DbOptions options = DbOptions.resolve(DbOperation.SELECT, crossOperation);
        assertNull(options.getPointBindings().single(InsertNullFieldPoint.class));
        assertTrue(options.getPointBindings().isEmpty(InsertNullFieldPoint.class));

        // 同一 Option 在匹配的操作下正常绑定
        DbOptions insert = DbOptions.resolve(DbOperation.INSERT, crossOperation);
        assertNotNull(insert.getPointBindings().single(InsertNullFieldPoint.class));
    }

    private static final class InsertFieldOption implements DbOption, InsertFieldPoint {
        @Override
        public String key() {
            return "insert-field-test";
        }

        @Override
        public FieldContribution contributeInsertFields(FieldContext context) {
            return FieldContribution.EMPTY;
        }
    }

    private static final class InsertNullOption implements DbOption, InsertNullFieldPoint {
        @Override
        public String key() {
            return "insert-null-test";
        }

        @Override
        public NullFieldMode chooseInsertNullFields(CrudContext context) {
            return NullFieldMode.IGNORE;
        }
    }
}
