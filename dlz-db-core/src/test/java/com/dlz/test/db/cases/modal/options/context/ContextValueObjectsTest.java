package com.dlz.test.db.cases.modal.options.context;

import com.dlz.db.option.DbOperation;
import com.dlz.db.option.DbOptions;
import com.dlz.db.option.point.context.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextValueObjectsTest {

    @Test
    void crudContextsExposeImmutableState() {
        CrudContext crud = new CrudContext(DbOperation.SELECT, "user", String.class, null);
        assertEquals(DbOperation.SELECT, crud.getOperation());
        assertEquals("user", crud.getTableName());
        assertEquals(String.class, crud.getEntityType());
        assertSame(DbOptions.EMPTY, crud.getOptions());
        assertThrows(IllegalArgumentException.class, () -> new CrudContext(null, "user", null, null));
        assertThrows(IllegalArgumentException.class, () -> new CrudContext(DbOperation.SELECT, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new CrudContext(DbOperation.SELECT, " ", null, null));
        assertSame(DbOptions.EMPTY, new CrudContext(DbOperation.SELECT, "user", null, DbOptions.EMPTY).getOptions());
    }

    @Test
    void fieldContextsCopyCollections() {
        List<String> fields = new ArrayList<>(Arrays.asList("id", "name"));
        FieldContext context = new FieldContext(DbOperation.UPDATE, "user", String.class, fields);
        fields.add("age");
        assertEquals(Arrays.asList("id", "name"), context.getFieldNames());
        assertThrows(UnsupportedOperationException.class, () -> context.getFieldNames().add("x"));
        assertEquals(DbOperation.UPDATE, context.getOperation());
        assertEquals("user", context.getTableName());
        assertEquals(String.class, context.getEntityType());
        assertEquals(Collections.emptyList(), new FieldContext(DbOperation.SELECT, "user", null, null).getFieldNames());
        assertThrows(IllegalArgumentException.class, () -> new FieldContext(null, "user", null, null));
        assertThrows(IllegalArgumentException.class, () -> new FieldContext(DbOperation.SELECT, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new FieldContext(DbOperation.SELECT, "", null, null));
    }

    @Test
    void fieldValueAndContributionAreImmutable() {
        FieldValue value = new FieldValue("name", null);
        assertEquals("name", value.getFieldName());
        assertNull(value.getValue());
        assertThrows(IllegalArgumentException.class, () -> new FieldValue(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> new FieldValue(" ", "x"));

        FieldValue write = new FieldValue("name", "Alice");
        FieldContribution contribution = new FieldContribution(
                Arrays.asList("id"), Arrays.asList(write));
        assertEquals(Collections.singletonList("id"), contribution.getSelectedFields());
        assertEquals(Collections.singletonList(write), contribution.getWriteValues());
        assertEquals(Collections.emptyList(), FieldContribution.EMPTY.getSelectedFields());
        assertEquals(Collections.emptyList(), new FieldContribution(null, null).getWriteValues());
        assertThrows(UnsupportedOperationException.class, () -> contribution.getWriteValues().add(write));
    }

    @Test
    void valueContextExposesState() {
        ValueContext value = new ValueContext(DbOperation.INSERT, "user", "name", "NAME", "Alice", String.class);
        assertEquals(DbOperation.INSERT, value.getOperation());
        assertEquals("user", value.getTableName());
        assertEquals("name", value.getFieldName());
        assertEquals("NAME", value.getColumnName());
        assertEquals("Alice", value.getValue());
        assertEquals(String.class, value.getTargetType());
    }

    @Test
    void enumsExposeExpectedValues() {
        assertArrayEquals(new DeletedDataMode[]{DeletedDataMode.EXCLUDE, DeletedDataMode.INCLUDE, DeletedDataMode.ONLY}, DeletedDataMode.values());
        assertArrayEquals(new DeleteMode[]{DeleteMode.PHYSICAL, DeleteMode.LOGICAL}, DeleteMode.values());
        assertArrayEquals(new NullFieldMode[]{NullFieldMode.IGNORE, NullFieldMode.INCLUDE}, NullFieldMode.values());
        assertArrayEquals(new SelectLockMode[]{SelectLockMode.NONE, SelectLockMode.FOR_UPDATE, SelectLockMode.FOR_SHARE}, SelectLockMode.values());
    }
}
