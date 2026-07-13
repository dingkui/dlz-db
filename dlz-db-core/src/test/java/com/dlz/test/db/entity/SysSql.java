package com.dlz.test.db.entity;

import com.dlz.db.annotation.IdType;
import com.dlz.db.annotation.TableId;
import com.dlz.db.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_sql")
public class SysSql {
    @TableId(value = "id",type = IdType.INPUT)
    private Long id;
    private String sqlKey;
    private String sqlValue;
    private String name;
    private String sqlRole;
    private Integer deleted;
}
