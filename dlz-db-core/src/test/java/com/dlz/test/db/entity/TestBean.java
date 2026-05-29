package com.dlz.test.db.entity;

import com.dlz.kit.json.JSONMap;
import com.dlz.kit.util.JacksonUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class TestBean {

    private Long id;

    private JSONMap t;

    /**
     * Jackson 反序列化时，支持从 JSON 字符串构造 TestBean。
     * 场景：数据库 VARCHAR 列存储序列化 JSON 字符串，读取后需要还原为对象。
     */
    @JsonCreator
    public static TestBean fromString(String json) {
        return JacksonUtil.readValue(json, TestBean.class);
    }
}
