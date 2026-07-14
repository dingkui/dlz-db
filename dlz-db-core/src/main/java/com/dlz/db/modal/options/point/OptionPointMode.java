package com.dlz.db.modal.options.point;

/** Processing strategy for options bound to an extension point. */
public enum OptionPointMode {
    /** Exactly zero or one option may be bound. */
    SINGLE,
    /** All options run sequentially in stable order. */
    CHAIN,
    /** All options participate in one aggregate decision. */
    MERGE
}
