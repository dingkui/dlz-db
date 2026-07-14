package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.DbOperation;

/** Canonical registry of all stable option points. */
public final class StandardOptionPoints {
    public static final OptionPointRegistry REGISTRY = createRegistry();

    private StandardOptionPoints() {
    }

    private static OptionPointRegistry createRegistry() {
        OptionPointRegistry.Builder builder = OptionPointRegistry.builder();

        registerAllOperations(builder, DataSourceRoutePoint.class, OptionPointMode.SINGLE);
        registerAllOperations(builder, TableRoutePoint.class, OptionPointMode.SINGLE);
        registerAllOperations(builder, ColumnNamePoint.class, OptionPointMode.SINGLE);
        registerAllOperations(builder, SqlBuildPoint.class, OptionPointMode.CHAIN);
        registerAllOperations(builder, JdbcParameterPoint.class, OptionPointMode.SINGLE);
        registerAllOperations(builder, BeforeExecutionPoint.class, OptionPointMode.CHAIN);
        registerAllOperations(builder, AfterExecutionPoint.class, OptionPointMode.CHAIN);
        registerAllOperations(builder, ExecutionErrorPoint.class, OptionPointMode.CHAIN);

        builder.register(GeneratedKeyPoint.class, OptionPointMode.SINGLE, DbOperation.INSERT);
        builder.register(InsertConflictPoint.class, OptionPointMode.SINGLE, DbOperation.INSERT);
        builder.register(InsertNullFieldPoint.class, OptionPointMode.SINGLE, DbOperation.INSERT);
        builder.register(InsertFieldPoint.class, OptionPointMode.MERGE, DbOperation.INSERT);
        builder.register(InsertValuePoint.class, OptionPointMode.CHAIN, DbOperation.INSERT);

        builder.register(UpdateSafetyPoint.class, OptionPointMode.SINGLE, DbOperation.UPDATE);
        builder.register(UpdateNullFieldPoint.class, OptionPointMode.SINGLE, DbOperation.UPDATE);
        builder.register(UpdateFieldPoint.class, OptionPointMode.MERGE, DbOperation.UPDATE);
        builder.register(UpdateValuePoint.class, OptionPointMode.CHAIN, DbOperation.UPDATE);
        builder.register(OptimisticLockPoint.class, OptionPointMode.MERGE, DbOperation.UPDATE);

        builder.register(DeleteModePoint.class, OptionPointMode.SINGLE, DbOperation.DELETE);
        builder.register(DeleteSafetyPoint.class, OptionPointMode.SINGLE, DbOperation.DELETE);
        builder.register(LogicDeleteValuePoint.class, OptionPointMode.SINGLE, DbOperation.DELETE);

        registerReadWrite(builder, WherePoint.class, OptionPointMode.MERGE);
        registerReadWrite(builder, ConditionValuePoint.class, OptionPointMode.CHAIN);
        registerReadWrite(builder, ConditionSafetyPoint.class, OptionPointMode.SINGLE);

        builder.register(SelectFieldPoint.class, OptionPointMode.MERGE, DbOperation.SELECT);
        builder.register(DeletedDataPoint.class, OptionPointMode.SINGLE, DbOperation.SELECT);
        builder.register(SelectLockPoint.class, OptionPointMode.SINGLE, DbOperation.SELECT);
        builder.register(PaginationPoint.class, OptionPointMode.SINGLE, DbOperation.SELECT);
        builder.register(CountPoint.class, OptionPointMode.SINGLE, DbOperation.SELECT);
        builder.register(RowMapperPoint.class, OptionPointMode.SINGLE, DbOperation.SELECT);
        builder.register(ReadValuePoint.class, OptionPointMode.CHAIN, DbOperation.SELECT);
        builder.register(ResultMapperPoint.class, OptionPointMode.SINGLE, DbOperation.SELECT);

        return builder.build();
    }

    private static <P extends OptionPoint> void registerAllOperations(
            OptionPointRegistry.Builder builder, Class<P> pointType, OptionPointMode mode) {
        builder.register(pointType, mode, DbOperation.values());
    }

    private static <P extends OptionPoint> void registerReadWrite(
            OptionPointRegistry.Builder builder, Class<P> pointType, OptionPointMode mode) {
        builder.register(pointType, mode, DbOperation.SELECT, DbOperation.UPDATE, DbOperation.DELETE);
    }
}
