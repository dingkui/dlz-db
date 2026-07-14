package com.dlz.test.db.cases.modal.options;

import com.dlz.db.exception.DbParameterException;
import com.dlz.db.modal.options.DbOperation;
import com.dlz.db.modal.options.DbOption;
import com.dlz.db.modal.options.DbOptions;
import com.dlz.db.modal.options.point.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionPointBindingsTest {

    @Test
    void shouldClassifyOneOptionIntoAllImplementedPoints() {
        MultiPointOption option = new MultiPointOption("multi", 10);
        OptionPointBindings bindings = OptionPointBindings.bind(
                DbOptions.resolve(DbOperation.SELECT, option), registry());

        assertSame(option, bindings.single(SinglePoint.class));
        assertSame(option, bindings.chain(ChainPoint.class).get(0));
        assertTrue(bindings.merge(MergePoint.class).isEmpty());
    }

    @Test
    void shouldSortChainStablyByOrder() {
        ChainOption firstEqual = new ChainOption("first-equal", 10);
        ChainOption low = new ChainOption("low", -1);
        ChainOption secondEqual = new ChainOption("second-equal", 10);
        OptionPointBindings bindings = OptionPointBindings.bind(
                DbOptions.resolve(DbOperation.SELECT, firstEqual, low, secondEqual), registry());

        List<ChainPoint> chain = bindings.chain(ChainPoint.class);

        assertSame(low, chain.get(0));
        assertSame(firstEqual, chain.get(1));
        assertSame(secondEqual, chain.get(2));
        assertThrows(UnsupportedOperationException.class, () -> chain.add(low));
    }

    @Test
    void shouldRejectMultipleOptionsForSinglePoint() {
        DbOptions options = DbOptions.resolve(DbOperation.SELECT,
                new SingleOption("one"), new SingleOption("two"));

        assertThrows(DbParameterException.class,
                () -> OptionPointBindings.bind(options, registry()));
    }

    @Test
    void shouldRejectDuplicateAndInvalidRegistrations() {
        assertThrows(IllegalArgumentException.class, () -> OptionPointRegistry.builder()
                .register(SinglePoint.class, OptionPointMode.SINGLE)
                .register(SinglePoint.class, OptionPointMode.SINGLE));
        assertThrows(IllegalArgumentException.class, () -> OptionPointRegistry.builder()
                .register(OptionPoint.class, OptionPointMode.CHAIN));
    }

    @Test
    void shouldRegisterCompleteStandardPointMatrix() {
        OptionPointRegistry registry = StandardOptionPoints.REGISTRY;

        assertEquals(32, registry.asList().size());
        assertPoints(registry, OptionPointMode.SINGLE, allOperations(),
                DataSourceRoutePoint.class, TableRoutePoint.class, ColumnNamePoint.class,
                JdbcParameterPoint.class);
        assertPoints(registry, OptionPointMode.CHAIN, allOperations(),
                SqlBuildPoint.class, BeforeExecutionPoint.class, AfterExecutionPoint.class,
                ExecutionErrorPoint.class);
        assertPoints(registry, OptionPointMode.SINGLE, operations(DbOperation.INSERT),
                GeneratedKeyPoint.class, InsertConflictPoint.class, InsertNullFieldPoint.class);
        assertPoints(registry, OptionPointMode.MERGE, operations(DbOperation.INSERT),
                InsertFieldPoint.class);
        assertPoints(registry, OptionPointMode.CHAIN, operations(DbOperation.INSERT),
                InsertValuePoint.class);
        assertPoints(registry, OptionPointMode.SINGLE, operations(DbOperation.UPDATE),
                UpdateSafetyPoint.class, UpdateNullFieldPoint.class);
        assertPoints(registry, OptionPointMode.MERGE, operations(DbOperation.UPDATE),
                UpdateFieldPoint.class, OptimisticLockPoint.class);
        assertPoints(registry, OptionPointMode.CHAIN, operations(DbOperation.UPDATE),
                UpdateValuePoint.class);
        assertPoints(registry, OptionPointMode.SINGLE, operations(DbOperation.DELETE),
                DeleteModePoint.class, DeleteSafetyPoint.class, LogicDeleteValuePoint.class);
        assertPoints(registry, OptionPointMode.MERGE, readWriteOperations(), WherePoint.class);
        assertPoints(registry, OptionPointMode.CHAIN, readWriteOperations(), ConditionValuePoint.class);
        assertPoints(registry, OptionPointMode.SINGLE, readWriteOperations(), ConditionSafetyPoint.class);
        assertPoints(registry, OptionPointMode.MERGE, operations(DbOperation.SELECT), SelectFieldPoint.class);
        assertPoints(registry, OptionPointMode.SINGLE, operations(DbOperation.SELECT),
                DeletedDataPoint.class, SelectLockPoint.class, PaginationPoint.class, CountPoint.class,
                RowMapperPoint.class, ResultMapperPoint.class);
        assertPoints(registry, OptionPointMode.CHAIN, operations(DbOperation.SELECT), ReadValuePoint.class);
    }

    private static void assertPoints(OptionPointRegistry registry, OptionPointMode mode,
                                     List<DbOperation> operations,
                                     Class<? extends OptionPoint>... pointTypes) {
        for (Class<? extends OptionPoint> pointType : pointTypes) {
            assertEquals(mode, registry.get(pointType).getMode(), pointType.getSimpleName());
            for (DbOperation operation : DbOperation.values()) {
                assertEquals(operations.contains(operation), registry.get(pointType).supports(operation),
                        pointType.getSimpleName() + " / " + operation);
            }
        }
    }

    private static List<DbOperation> allOperations() {
        return operations(DbOperation.values());
    }

    private static List<DbOperation> readWriteOperations() {
        return operations(DbOperation.SELECT, DbOperation.UPDATE, DbOperation.DELETE);
    }

    private static List<DbOperation> operations(DbOperation... operations) {
        return java.util.Arrays.asList(operations);
    }

    @Test
    void shouldRequireRegisteredPointAndCorrectAccessMode() {
        OptionPointBindings bindings = OptionPointBindings.bind(DbOptions.EMPTY, registry());

        assertThrows(IllegalArgumentException.class, () -> bindings.chain(SinglePoint.class));
        assertThrows(IllegalArgumentException.class, () -> bindings.chain(UnregisteredPoint.class));
    }

    private static OptionPointRegistry registry() {
        return OptionPointRegistry.builder()
                .register(SinglePoint.class, OptionPointMode.SINGLE)
                .register(ChainPoint.class, OptionPointMode.CHAIN)
                .register(MergePoint.class, OptionPointMode.MERGE)
                .build();
    }

    private interface SinglePoint extends OptionPoint {
    }

    private interface ChainPoint extends OptionPoint {
    }

    private interface MergePoint extends OptionPoint {
    }

    private interface UnregisteredPoint extends OptionPoint {
    }

    private static final class MultiPointOption extends BaseOption implements SinglePoint, ChainPoint {
        private MultiPointOption(String key, int order) {
            super(key, order);
        }
    }

    private static final class SingleOption extends BaseOption implements SinglePoint {
        private SingleOption(String key) {
            super(key, 0);
        }
    }

    private static final class ChainOption extends BaseOption implements ChainPoint {
        private ChainOption(String key, int order) {
            super(key, order);
        }
    }

    private abstract static class BaseOption implements DbOption, OptionPoint {
        private final String key;
        private final int order;

        private BaseOption(String key, int order) {
            this.key = key;
            this.order = order;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public int order() {
            return order;
        }
    }
}
