package com.dlz.db.modal.items;

import com.dlz.db.modal.condition.Condition;
import com.dlz.db.modal.options.DbOption;

import java.util.List;
import java.util.Map;

public interface SqlBuildContext {
    String table();
    Map<String, Object> values();
    Condition where();
    List<DbOption> options();
}
