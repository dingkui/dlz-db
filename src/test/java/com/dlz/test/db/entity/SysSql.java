package com.dlz.test.db.entity;

import com.dlz.db.annotation.IdType;
import com.dlz.db.annotation.TableId;
import lombok.Data;

@Data
public class SysSql {
    @TableId(value = "id",type = IdType.INPUT)
    private Long id;
    private String sqlKey;
    private String sqlValue;
    private String name;
    private String sqlRole;
}
