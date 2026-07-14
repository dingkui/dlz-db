package com.dlz.db.modal.options.point.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 单个 Option 对字段聚合的不可变贡献。 */
public final class FieldContribution {
    public static final FieldContribution EMPTY = new FieldContribution(Collections.emptyList(), Collections.emptyList());

    private final List<String> selectedFields;
    private final List<FieldValue> writeValues;

    public FieldContribution(List<String> selectedFields, List<FieldValue> writeValues) {
        this.selectedFields = selectedFields == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(selectedFields));
        this.writeValues = writeValues == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(writeValues));
    }

    public List<String> getSelectedFields() { return selectedFields; }
    public List<FieldValue> getWriteValues() { return writeValues; }
}
