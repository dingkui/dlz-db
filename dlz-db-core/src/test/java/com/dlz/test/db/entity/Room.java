package com.dlz.test.db.entity;

import com.dlz.db.core.anno.IdType;
import com.dlz.db.core.anno.TableId;
import com.dlz.db.core.anno.TableName;
import lombok.Data;

@Data
@TableName("dh_room")
public class Room {
    @TableId(value = "id",type = IdType.INPUT)
    private Long id;
    private String equipmentId;
    private String equipmentId2;
    private String xxId1;
    private String xxId2;
    private String xxId3;
}
