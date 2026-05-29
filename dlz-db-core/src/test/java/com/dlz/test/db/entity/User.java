package com.dlz.test.db.entity;

import com.dlz.db.annotation.IdType;
import com.dlz.db.annotation.TableId;
import com.dlz.db.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId(type=IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
    private Date createTime;
    private String sex;
    private String flag;
    private String score;
    private String deptId;
    private String level;
    private int retryCount;
    private String address;
    private String phone;
    private String vip;
    private String email;
    private String remark;
    private String status;
    private String createUser;
    private String updateUser;
    private Date updateTime;
    private String deleted;
    private String deleteUser;
    private Date deleteTime;
    private String isExpired;
    private String code;
    private String type;
    private String city;
    private String description;

}
