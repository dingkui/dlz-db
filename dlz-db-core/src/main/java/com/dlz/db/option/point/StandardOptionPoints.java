package com.dlz.db.option.point;

import com.dlz.db.option.DbOperation;

/**
 * 稳定桩点注册表：只登记框架已真实接线的桩点。
 *
 * <p>桩点接线遵循"先接线、后公开"原则：未接线的设计保存在
 * {@code FUTURE-POINTS.md}（同包），不进入本注册表，避免出现
 * "实现后静默不生效"的空头 API。
 */
public final class StandardOptionPoints {
    public static final OptionPointRegistry REGISTRY = createRegistry();

    private StandardOptionPoints() {
    }

    private static OptionPointRegistry createRegistry() {
        OptionPointRegistry.Builder builder = OptionPointRegistry.builder();

        builder.register(InsertNullFieldPoint.class, OptionPointMode.SINGLE, DbOperation.INSERT);
        builder.register(InsertFieldPoint.class, OptionPointMode.MERGE, DbOperation.INSERT);

        builder.register(UpdateNullFieldPoint.class, OptionPointMode.SINGLE, DbOperation.UPDATE);

        builder.register(DeleteModePoint.class, OptionPointMode.SINGLE, DbOperation.DELETE);
        builder.register(LogicDeleteValuePoint.class, OptionPointMode.SINGLE, DbOperation.DELETE);

        builder.register(WherePoint.class, OptionPointMode.MERGE,
                DbOperation.SELECT, DbOperation.UPDATE, DbOperation.DELETE);
        builder.register(DeletedDataPoint.class, OptionPointMode.SINGLE, DbOperation.SELECT);
        builder.register(SelectLockPoint.class, OptionPointMode.SINGLE, DbOperation.SELECT);

        return builder.build();
    }
}
