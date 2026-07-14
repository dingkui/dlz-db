package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;

/** UPDATE 安全策略的排他桩点。 */
public interface UpdateSafetyPoint extends OptionPoint {
    boolean allowUnsafeUpdate(CrudContext context);
}
