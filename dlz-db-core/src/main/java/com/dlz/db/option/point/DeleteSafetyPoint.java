package com.dlz.db.option.point;

import com.dlz.db.option.point.context.CrudContext;

/** DELETE 安全策略的排他桩点。 */
public interface DeleteSafetyPoint extends OptionPoint {
    boolean allowUnsafeDelete(CrudContext context);
}
