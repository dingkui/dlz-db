package com.dlz.db.modal.options.point;

/**
 * Stable extension point implemented by {@code DbOption} implementations.
 * One option may implement more than one point interface.
 */
public interface OptionPoint {
    /** Lower values run first. Registration order is retained for equal values. */
    default int order() {
        return 0;
    }
}
