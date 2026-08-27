package com.dlz.test.db.entity;

import com.dlz.db.core.anno.IdType;
import com.dlz.db.core.anno.TableId;
import com.dlz.db.core.anno.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 测试实体 - AUTO类型ID
 */
@Setter
@Getter
@TableName("TestAutoEntity")
public class TestAutoEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
}