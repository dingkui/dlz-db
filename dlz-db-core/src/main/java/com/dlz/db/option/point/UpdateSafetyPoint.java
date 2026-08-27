package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;

/** UPDATE 安全策略的排他桩点。 */
public interface UpdateSafetyPoint extends OptionPoint {
    boolean allowUnsafeUpdate(CrudContext context);
}
