package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;
import com.dlz.db.modal.options.point.context.InsertConflictMode;

/** INSERT 冲突处理策略的排他桩点。 */
public interface InsertConflictPoint extends OptionPoint {
    InsertConflictMode resolveInsertConflict(CrudContext context);
}
