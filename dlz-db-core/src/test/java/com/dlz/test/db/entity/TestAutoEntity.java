package com.dlz.test.db.entity;

import com.dlz.db.annotation.IdType;
import com.dlz.db.annotation.TableId;
import com.dlz.db.annotation.TableName;
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