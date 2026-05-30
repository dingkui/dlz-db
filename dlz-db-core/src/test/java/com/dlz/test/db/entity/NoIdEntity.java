package com.dlz.test.db.entity;

import com.dlz.db.annotation.TableName;
import lombok.Data;

@Data
@TableName("test_no_id")
public class NoIdEntity {
    private String name;
}
