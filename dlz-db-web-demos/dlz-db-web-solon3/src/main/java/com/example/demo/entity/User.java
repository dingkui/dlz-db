package com.example.demo.entity;

import com.dlz.db.core.anno.TableId;
import com.dlz.db.core.anno.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户实体类。
 *
 * <p>说明：</p>
 * <ol>
 *   <li>@TableName 指定表名，不写则驼峰转下划线（User → user）</li>
 *   <li>@TableId 标记主键</li>
 *   <li>含 deleted 字段时自动启用逻辑删除</li>
 *   <li>启动时 auto-update: true 会自动建表/加字段</li>
 * </ol>
 */
@Data
@TableName("user")
public class User {

    @TableId
    private Long id;

    private String name;

    private Integer age;

    private String email;

    private Integer deleted;

    private Date createTime;
}
