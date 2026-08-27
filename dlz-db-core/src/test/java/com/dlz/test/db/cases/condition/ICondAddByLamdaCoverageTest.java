package com.dlz.test.db.cases.condition;

import com.dlz.db.internal.enums.DbOperateEnum;
import com.dlz.db.internal.inf.ICondAddByLambda;
import com.dlz.db.internal.condition.Condition;
import com.dlz.test.db.entity.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ICondAddByLambdaCoverageTest {
    @Test
    void allLambdaConditionOverloadsAndDynamicSwitches() {
        Probe probe = new Probe();
        assertSame(probe, probe.between(User::getAge, 18, 60));
        assertSame(probe, probe.between(true, User::getAge, 18, 60));
        assertSame(probe, probe.between(User::getAge, "18,60"));
        assertSame(probe, probe.between(true, User::getAge, Arrays.asList(18, 60)));
        assertSame(probe, probe.notBetween(User::getAge, 1, 2));
        assertSame(probe, probe.notBetween(true, User::getAge, 1, 2));
        assertSame(probe, probe.notBetween(User::getAge, "1,2"));
        assertSame(probe, probe.notBetween(true, User::getAge, Arrays.asList(1, 2)));

        assertSame(probe, probe.isNotNull(User::getName));
        assertSame(probe, probe.isNotNull(true, User::getName));
        assertSame(probe, probe.isNull(User::getName));
        assertSame(probe, probe.isNull(true, User::getName));
        assertSame(probe, probe.eq(User::getName, "alice"));
        assertSame(probe, probe.eq(true, User::getName, "alice"));
        assertSame(probe, probe.ne(User::getName, "bob"));
        assertSame(probe, probe.ne(true, User::getName, "bob"));

        assertSame(probe, probe.gt(User::getAge, 18));
        assertSame(probe, probe.gt(true, User::getAge, 18));
        assertSame(probe, probe.ge(User::getAge, 18));
        assertSame(probe, probe.ge(true, User::getAge, 18));
        assertSame(probe, probe.lt(User::getAge, 60));
        assertSame(probe, probe.lt(true, User::getAge, 60));
        assertSame(probe, probe.le(User::getAge, 60));
        assertSame(probe, probe.le(true, User::getAge, 60));

        assertSame(probe, probe.like(User::getName, "ali"));
        assertSame(probe, probe.like(true, User::getName, "ali"));
        assertSame(probe, probe.likeLeft(User::getName, "ali"));
        assertSame(probe, probe.likeLeft(true, User::getName, "ali"));
        assertSame(probe, probe.likeRight(User::getName, "ce"));
        assertSame(probe, probe.likeRight(true, User::getName, "ce"));
        assertSame(probe, probe.notLike(User::getName, "x"));
        assertSame(probe, probe.notLike(true, User::getName, "x"));
        assertSame(probe, probe.in(User::getId, Arrays.asList(1, 2)));
        assertSame(probe, probe.in(true, User::getId, "sql:SELECT id FROM user"));
        assertSame(probe, probe.notIn(User::getId, "1,2"));
        assertSame(probe, probe.notIn(true, User::getId, Arrays.asList(3, 4)));
        assertSame(probe, probe.op(User::getName, DbOperateEnum.eq, "alice"));

        int beforeDisabled = probe.conditions.size();
        probe.between(false, User::getAge, 1, 2);
        probe.between(false, User::getAge, "1,2");
        probe.notBetween(false, User::getAge, 1, 2);
        probe.notBetween(false, User::getAge, "1,2");
        probe.isNotNull(false, User::getName);
        probe.isNull(false, User::getName);
        probe.eq(false, User::getName, "x");
        probe.ne(false, User::getName, "x");
        probe.gt(false, User::getAge, 1);
        probe.ge(false, User::getAge, 1);
        probe.lt(false, User::getAge, 1);
        probe.le(false, User::getAge, 1);
        probe.like(false, User::getName, "x");
        probe.likeLeft(false, User::getName, "x");
        probe.likeRight(false, User::getName, "x");
        probe.notLike(false, User::getName, "x");
        probe.in(false, User::getId, Arrays.asList(1));
        probe.notIn(false, User::getId, Arrays.asList(1));
        assertEquals(beforeDisabled, probe.conditions.size());
        assertEquals("user", probe.getTableName());
        assertFalse(probe.conditions.isEmpty());
    }

    private static final class Probe implements ICondAddByLambda<Probe, User> {
        private final List<Condition> conditions = new ArrayList<>();

        @Override
        public void addChildren(Condition child) {
            conditions.add(child);
        }

        @Override
        public String getTableName() {
            return "user";
        }

        @Override
        public Probe me() {
            return this;
        }
    }
}
