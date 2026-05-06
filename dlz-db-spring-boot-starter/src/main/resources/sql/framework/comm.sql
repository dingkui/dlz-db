<?xml version="1.0" encoding="UTF-8" ?>
<!--=========================================================================-->
<!--  Copyright bj 2015 All Rights Reserved. -->
<!--  @version	1.00												 -->
<!--=========================================================================-->

<sqlList>
    <!-- 通用数据库翻页语句，支持的数据库：
       dm8,
       mysql,
       postgresql,
       MariaDB,
       TiDB,
       SQLite,
       doris
    -->
    <sql sqlId="key.comm.pageSql"><![CDATA[
    ${_sql} ${_orderBy} [ LIMIT [#{_begin},]#{_pageSize} ]
    ]]></sql>

   <!-- postgresql数据库翻页语句:兼容common,但建议使用: limit size offset begin -->
    <sql sqlId="key.comm.pageSql._postgresql"><![CDATA[
     ${_sql} ${_orderBy} [ LIMIT #{_pageSize} [OFFSET #{_begin}] ]
    ]]></sql>

    <!-- oracle数据库翻页语句 -->
 	<sql sqlId="key.comm.pageSql._oracle"><![CDATA[
		[select * from (select a1.*,rownum rownum_ from ( ^#{_end}]
			[select * from ( ^#{_orderBy}]
				${_sql}
			[) ${_orderBy}]
		[) a1 where rownum <=#{_end} ) [where rownum_> #{_begin}]]
    ]]></sql>

    <!-- sqlserver数据库翻页语句 -->
    <sql sqlId="key.comm.pageSql._sqlserver"><![CDATA[
        SELECT * FROM (
          SELECT row_number() OVER(ORDER BY _tpc) rownum_,a2.* FROM(
            SELECT TOP ${_end} _tpc=null,a1.* FROM (
                ${_sql}
            ) a1 ${_orderBy}
          ) a2
        )a3 WHERE rownum_ > ${_begin}
    ]]></sql>
</sqlList>
