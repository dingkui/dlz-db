package com.dlz.test.db.cases.modal.options;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.options.DbOperation;
import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.options.DbOptions;
import com.dlz.db.modal.options.InsertOption;
import com.dlz.db.modal.options.SelectOption;
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
}
