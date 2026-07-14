package com.dlz.db.modal.options;

import com.dlz.db.exception.DbParameterException;

public final class TxOptions {
    public enum Propagation { REQUIRED, REQUIRES_NEW, NESTED }
    private final Propagation propagation;
    private final boolean readOnly;
    private TxOptions(Propagation propagation, boolean readOnly) {
        this.propagation = propagation; this.readOnly = readOnly;
    }
    public static TxOptions defaults() { return new TxOptions(Propagation.REQUIRED, false); }
    public static TxOptions of(Propagation propagation, boolean readOnly) {
        if (propagation == null) throw new DbParameterException("propagation must not be null");
        return new TxOptions(propagation, readOnly);
    }
    public Propagation propagation() { return propagation; }
    public boolean readOnly() { return readOnly; }
}
