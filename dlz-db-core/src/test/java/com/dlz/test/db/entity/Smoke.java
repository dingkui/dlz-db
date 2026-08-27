package com.dlz.test.db.entity;

import com.dlz.db.core.anno.IdType;
import com.dlz.db.core.anno.TableId;
import com.dlz.db.core.anno.TableName;
import lombok.Data;

@Data
@TableName("smoke")
public class Smoke {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String name;
}
