package com.dlz.test.db.entity;

import com.dlz.db.core.anno.TableId;
import com.dlz.db.core.anno.TableName;
import com.dlz.kit.json.JSONMap;
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
    @ApiModelProperty(value = "主键")
    @TableId(value = "id")
    private Long id;


    private TestBean t1;
    private JSONMap t2;
}
