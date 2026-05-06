package com.dlz.test.db.entity;

import com.dlz.db.annotation.IdType;
import com.dlz.db.annotation.TableId;
import com.dlz.db.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * 实体类：预测记录
 * @author dk
 */
@Data
@TableName("Vip")
public class Vip implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 主键 */
    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private Integer level;
}
