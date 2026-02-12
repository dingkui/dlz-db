package com.dlz.test.db.entity;

import com.dlz.db.annotation.TableField;
import com.dlz.db.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_b_dict")
@ApiModel("测试")
public class Dict extends BaseEntity {
    @ApiModelProperty("xasd：啊实打实\n\"xas\"")
    private String dictStatus;
    @ApiModelProperty("int")
    @TableField("xxss")
    private Integer a2;
    @ApiModelProperty("boolean：")
    private Boolean a3;
    @ApiModelProperty("Long：")
    private Long a4;
    @ApiModelProperty("Float：")
    private Float a5;
    @ApiModelProperty("BigDecimal：")
    private BigDecimal a6;
    @ApiModelProperty("Object：")
    @TableField(exist=false)
    private Object a7;
    private Object isDeleted;
}
