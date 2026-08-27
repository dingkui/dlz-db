package com.dlz.test.db.entity;

import com.dlz.db.core.anno.TableName;
import lombok.Data;

@Data
@TableName("DEPARTMENT")
public class Department {
    private Long id;
    private String status;
    private String type;
}
