<?xml version="1.0" encoding="UTF-8" ?>
<!--=========================================================================-->
<!--  Copyright dlz 2024 All Rights Reserved.			 				 -->
<!--  demo/user.sql															 -->
<!--																		 -->
<!--  [概要描述]															 	 -->
<!--  演示预设SQL配置													     -->
<!--																		 -->
<!--  @history	2024-01-01 ver1.00          							     -->
<!--  @author	dlz											    		     -->
<!--  @version	1.00														 -->
<!--=========================================================================-->

<sqlList>
    <!--
        用户查询预设SQL
        对应文档中的示例：key.demo.user.find
    -->
    <sql sqlId="key.demo.user.find"><![CDATA[
      SELECT * FROM user 
      WHERE 1=1
       [AND name LIKE #{name}]  --支持注释 name为空时，该条件自动忽略
       [AND status IN (${status})]  --status 支持是集合时自动拼接：(1,2,3)/('ok','ng')。
    ]]></sql>
    
    <!--
        基础WHERE条件片段
        用于演示SQL片段引用功能
    -->
    <sql sqlId="key.demo.user.baseWhere"><![CDATA[
      WHERE 1=1 [AND status IN (${status})]
    ]]></sql>
    
    <!--
        使用基础片段的完整查询
        演示${key.xxx}引用其他预设SQL片段
    -->
    <sql sqlId="key.demo.user.findWithBase"><![CDATA[
      SELECT * FROM user ${key.demo.user.baseWhere}
    ]]></sql>
    
    <!--
        按年龄范围查询
        演示多个动态条件
    -->
    <sql sqlId="key.demo.user.findByAge"><![CDATA[
      SELECT * FROM user 
      WHERE 1=1
       [AND age >= #{minAge}]
       [AND age <= #{maxAge}]
       [AND status = #{status}]
    ]]></sql>
    
    <!--
        复杂条件查询
        演示嵌套动态条件
    -->
    <sql sqlId="key.demo.user.complexQuery"><![CDATA[
      SELECT * FROM user 
      WHERE 1=1
      [
        AND (
          [name LIKE #{name}]
          [OR age = #{age}]
        )
      ]
      [AND status IN (${status})]
    ]]></sql>
</sqlList>