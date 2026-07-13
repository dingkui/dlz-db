<?xml version="1.0" encoding="UTF-8" ?>
<!--=========================================================================-->
<!--  Copyright bj 2015 All Rights Reserved.			 				 -->
<!--  sqlTest.sql															 -->
<!--																		 -->
<!--  [概要描述]															 	 -->
<!--  测试													     -->
<!--																		 -->
<!--																		 -->
<!--  @history	2011-08-12 ver1.00          							     -->
<!--  @author	dingkui											    		 -->
<!--  @version	1.00														 -->
<!--=========================================================================-->

<sqlList>
	<!--
		测试
	-->
	<sql sqlId="key.test"><![CDATA[
	    SELECT * from dual xxx
 	]]></sql>
	<!--
		测试
	-->
	<sql sqlId="key.test22"><![CDATA[
	    SELECT * from dual xxx
 	]]></sql>
	<!--
		测试
	-->
	<sql sqlId="key.sqlTest.update"><![CDATA[
	   UPDATE JOB_AD set AD_text=#{adText} WHERE  ad_id IN (${ad_id})
 	]]></sql>
	<!--
		测试
	-->
	<sql sqlId="key.sqlTest.insert"><![CDATA[
	   INSERT INTO JOB_AD (ad_id,ad_name,AD_text)VALUES(SEQ_JOB_AD.NEXTVAL,#{adName},#{adText})
 	]]></sql>
	<!--
		测试
	-->
	<sql sqlId="key.sqlTest.sqlUtilTest"><![CDATA[
	   WHERE  1=1
	   [and a=#{a}]   --a参数存在则添加该条件:"and a=#{a}"
	   [and b=#{b}]   --a参数存在则添加该条件:"and b=#{a}"
	   [and c=2 ^#{c}]   --a参数存在则添加该条件:"and b=#{a}"
	   [and d=${d}]   --a参数存在则添加该条件:"and b=#{a}"
	   [and d=ddd ^${d}]   --a参数存在则添加该条件:"and b=#{a}"
	   [
	   	and d=#{d}    --d或者c存在则添加该条件"and d=#{d}"
	   	and d1=#{d1}    --d或者c存在则添加该条件"and d=#{d}"
	   	and d2=#{d2}    --d或者c存在则添加该条件"and d=#{d}"
	    [and c=#{c}]  --c存在则添加   "and d=#{d} and c=#{c}"
	   ]  			  --d和c都不存在则不添加该条件
	   ${xxxx}
 	]]></sql>
 	
 	
 	<!--
		测试
	-->
	<sql sqlId="key.sqlTest.sqlUtil"><![CDATA[
	   SELECT * from bb ${key.sqlTest.sqlUtilTest}
 	]]></sql>
 	<!--
		测试4_6_2
	-->
	<sql sqlId="key.conditionTest4_6_2"><![CDATA[
	   age > #{minAge} [AND age < #{maxAge}]
 	]]></sql>
 	<!--
		测试5_1_4
	-->
	<sql sqlId="key.pageAndOrderTest5_1_4"><![CDATA[
	   SELECT * FROM user WHERE status = #{status}
 	]]></sql>
 	
	
</sqlList>










