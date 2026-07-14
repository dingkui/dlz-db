package com.dlz.db.modal.options.point.context;

/** SQL 执行形态，补充 CRUD 之外的原生执行与批处理。 */
public enum ExecutionKind {
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    EXECUTE,
    BATCH
}
