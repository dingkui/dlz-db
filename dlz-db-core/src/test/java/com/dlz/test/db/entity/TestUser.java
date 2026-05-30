package com.dlz.test.db.entity;

import com.dlz.db.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 测试用的 User 实体类 - 用于 Lambda 条件测试
 */
@Data
@TableName("Test_User")
public class TestUser {
    private Long id;
    private String name;
    private Integer age;
    private String email;
    private Integer status;
    private Date updateTime;
}
