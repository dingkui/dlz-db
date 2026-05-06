package com.dlz.test.db.entity;

import com.dlz.db.annotation.TableName;
import lombok.Data;

@Data
@TableName("DEPARTMENT")
public class Department {
    private Long id;
    private String status;
    private String type;
}
