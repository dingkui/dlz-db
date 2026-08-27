package com.dlz.test.db.cases.modal.options.context;

import com.dlz.db.dialect.rowMapper.IRowMapper;
import com.dlz.db.option.DbOperation;
import com.dlz.db.option.DbOptions;
import com.dlz.db.option.point.context.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextValueObjectsTest {
    private static final SqlStatement STATEMENT = new SqlStatement("SELECT 1", Arrays.asList(1, "x"));
    private static final ExecutionContext EXECUTION = new ExecutionContext(
            ExecutionKind.SELECT, "user", STATEMENT, DbOptions.EMPTY);

    @Test
    void crudAndExecutionContextsExposeImmutableState() {
        CrudContext crud = new CrudContext(DbOperation.SELECT, "user", String.class, null);
        assertEquals(DbOperation.SELECT, crud.getOperation());
        assertEquals("user", crud.getTableName());
        assertEquals(String.class, crud.getEntityType());
        assertSame(DbOptions.EMPTY, crud.getOptions());
        assertThrows(IllegalArgumentException.class, () -> new CrudContext(null, "user", null, null));
        assertThrows(IllegalArgumentException.class, () -> new CrudContext(DbOperation.SELECT, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new CrudContext(DbOperation.SELECT, " ", null, null));
        assertSame(DbOptions.EMPTY, new CrudContext(DbOperation.SELECT, "user", null, DbOptions.EMPTY).getOptions());

        ExecutionContext execution = new ExecutionContext(ExecutionKind.BATCH, null, STATEMENT, null);
        assertEquals(ExecutionKind.BATCH, execution.getKind());
        assertNull(execution.getTableName());
        assertSame(STATEMENT, execution.getStatement());
        assertSame(DbOptions.EMPTY, execution.getOptions());
        assertThrows(IllegalArgumentException.class, () -> new ExecutionContext(null, "user", STATEMENT, null));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionContext(ExecutionKind.SELECT, "user", null, null));

        ExecutionErrorContext error = new ExecutionErrorContext(EXECUTION, new RuntimeException("x"), 5);
        assertSame(EXECUTION, error.getExecution());
        assertEquals("x", error.getError().getMessage());
        assertEquals(5, error.getElapsedNanos());
        assertThrows(IllegalArgumentException.class, () -> new ExecutionErrorContext(null, new RuntimeException(), 0));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionErrorContext(EXECUTION, null, 0));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionErrorContext(EXECUTION, new RuntimeException(), -1));

        ExecutionResultContext<String> result = new ExecutionResultContext<>(EXECUTION, "ok", 7);
        assertSame(EXECUTION, result.getExecution());
        assertEquals("ok", result.getResult());
        assertEquals(7, result.getElapsedNanos());
        assertDoesNotThrow(() -> new ExecutionResultContext<>(EXECUTION, null, 0));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionResultContext<>(null, "x", 0));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionResultContext<>(EXECUTION, "x", -1));
    }

    @Test
    void fieldAndValueContextsCopyCollections() {
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
    void sqlPaginationParameterAndMappingContexts() {
        SqlStatement statement = new SqlStatement(" SELECT 1 ", Arrays.asList(1, "x"));
        assertEquals(" SELECT 1 ", statement.getSql());
        assertEquals(Arrays.asList(1, "x"), statement.getParameters());
        assertEquals(Collections.emptyList(), new SqlStatement("SELECT 1", null).getParameters());
        assertThrows(UnsupportedOperationException.class, () -> statement.getParameters().add(2));
        assertThrows(IllegalArgumentException.class, () -> new SqlStatement(null, null));
        assertThrows(IllegalArgumentException.class, () -> new SqlStatement(" ", null));

        SqlContext sql = new SqlContext(ExecutionKind.UPDATE, "user", statement, null);
        assertEquals(ExecutionKind.UPDATE, sql.getKind());
        assertEquals("user", sql.getTableName());
        assertSame(statement, sql.getStatement());
        assertSame(DbOptions.EMPTY, sql.getOptions());
        assertSame(DbOptions.EMPTY, new SqlContext(ExecutionKind.SELECT, null, statement, DbOptions.EMPTY).getOptions());
        assertThrows(IllegalArgumentException.class, () -> new SqlContext(null, "user", statement, null));
        assertThrows(IllegalArgumentException.class, () -> new SqlContext(ExecutionKind.UPDATE, "user", null, null));

        Pagination pagination = new Pagination(10, 20);
        assertEquals(10, pagination.getOffset());
        assertEquals(20, pagination.getLimit());
        assertThrows(IllegalArgumentException.class, () -> new Pagination(-1, 20));
        assertThrows(IllegalArgumentException.class, () -> new Pagination(0, 0));

        PreparedStatement prepared = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(), new Class<?>[]{PreparedStatement.class}, (p, m, a) -> null);
        ParameterContext parameter = new ParameterContext(prepared, 1, "x",  VARCHAR, "name");
        assertSame(prepared, parameter.getStatement());
        assertEquals(1, parameter.getIndex());
        assertEquals("x", parameter.getValue());
        assertEquals(VARCHAR, parameter.getJdbcType());
        assertEquals("name", parameter.getFieldName());
        assertThrows(IllegalArgumentException.class, () -> new ParameterContext(null, 1, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new ParameterContext(prepared, 0, null, null, null));

        ResultMappingContext<String, Integer> mapping = new ResultMappingContext<>("1", Integer.class);
        assertEquals("1", mapping.getSource());
        assertEquals(Integer.class, mapping.getTargetType());
        assertThrows(IllegalArgumentException.class, () -> new ResultMappingContext<>("x", null));

        IRowMapper<String> mapper = (rs, row) -> "mapped";
        RowMapperContext<String> rowContext = new RowMapperContext<>(String.class, mapper, "SELECT 1");
        assertEquals(String.class, rowContext.getResultType());
        assertSame(mapper, rowContext.getDefaultMapper());
        assertEquals("SELECT 1", rowContext.getSql());
        assertThrows(IllegalArgumentException.class, () -> new RowMapperContext<>(null, mapper, null));
    }

    @Test
    void nameAndValueContextsAndEnums() {
        NameContext name = new NameContext(DbOperation.SELECT, "user", "userName");
        assertEquals(DbOperation.SELECT, name.getOperation());
        assertEquals("user", name.getTableName());
        assertEquals("userName", name.getSourceName());
        assertThrows(IllegalArgumentException.class, () -> new NameContext(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new NameContext(null, null, " "));

        ValueContext value = new ValueContext(DbOperation.INSERT, "user", "name", "NAME", "Alice", String.class);
        assertEquals(DbOperation.INSERT, value.getOperation());
        assertEquals("user", value.getTableName());
        assertEquals("name", value.getFieldName());
        assertEquals("NAME", value.getColumnName());
        assertEquals("Alice", value.getValue());
        assertEquals(String.class, value.getTargetType());

        assertArrayEquals(new DeletedDataMode[]{DeletedDataMode.EXCLUDE, DeletedDataMode.INCLUDE, DeletedDataMode.ONLY}, DeletedDataMode.values());
        assertArrayEquals(new DeleteMode[]{DeleteMode.PHYSICAL, DeleteMode.LOGICAL}, DeleteMode.values());
        assertArrayEquals(new ExecutionKind[]{ExecutionKind.SELECT, ExecutionKind.INSERT, ExecutionKind.UPDATE, ExecutionKind.DELETE, ExecutionKind.EXECUTE, ExecutionKind.BATCH}, ExecutionKind.values());
        assertArrayEquals(new InsertConflictMode[]{InsertConflictMode.ERROR, InsertConflictMode.IGNORE, InsertConflictMode.UPDATE}, InsertConflictMode.values());
        assertArrayEquals(new NullFieldMode[]{NullFieldMode.IGNORE, NullFieldMode.INCLUDE}, NullFieldMode.values());
        assertArrayEquals(new SelectLockMode[]{SelectLockMode.NONE, SelectLockMode.FOR_UPDATE, SelectLockMode.FOR_SHARE}, SelectLockMode.values());
    }

    private static final int VARCHAR = java.sql.Types.VARCHAR;
}
