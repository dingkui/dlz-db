package com.dlz.test.db.entity;

import com.dlz.db.annotation.TableName;
import lombok.Data;

/**
 * 测试用的 User 实体类
 */

@Data
@TableName("Test_User")
public class TestUser {
    private Long id;
    private String name;
    private Integer age;
    private String email;
}