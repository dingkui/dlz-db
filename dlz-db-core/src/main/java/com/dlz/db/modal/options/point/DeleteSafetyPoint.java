package com.dlz.db.modal.options.point;

import com.dlz.db.modal.options.point.context.CrudContext;

/** DELETE 安全策略的排他桩点。 */
public interface DeleteSafetyPoint extends OptionPoint {
    boolean allowUnsafeDelete(CrudContext context);
}
