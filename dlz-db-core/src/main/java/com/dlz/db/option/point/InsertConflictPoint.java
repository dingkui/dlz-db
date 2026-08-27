package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;
import com.dlz.db.option.point.context.InsertConflictMode;

/** INSERT 冲突处理策略的排他桩点。 */
public interface InsertConflictPoint extends OptionPoint {
    InsertConflictMode resolveInsertConflict(CrudContext context);
}
