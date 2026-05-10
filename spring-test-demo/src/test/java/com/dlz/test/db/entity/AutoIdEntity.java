package com.dlz.test.db.entity;

import com.dlz.db.annotation.IdType;
import com.dlz.db.annotation.TableId;
import com.dlz.db.annotation.TableName;
import lombok.Data;

@Data
@TableName("test_auto_id")
public class AutoIdEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
}
