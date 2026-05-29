package com.dlz.test.db.entity;

import com.dlz.db.annotation.TableId;
import com.dlz.db.annotation.TableName;
import com.dlz.kit.json.JSONMap;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName()
@ApiModel("测试")
public class MapColumnBean extends BaseEntity {
    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    @TableId(value = "id")
    private Long id;


    private TestBean t1;
    private JSONMap t2;
}
