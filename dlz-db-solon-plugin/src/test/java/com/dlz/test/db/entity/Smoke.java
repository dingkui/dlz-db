package com.dlz.test.db.entity;

import com.dlz.db.annotation.IdType;
import com.dlz.db.annotation.TableId;
import com.dlz.db.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("smoke")
public class Smoke {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String name;
}
