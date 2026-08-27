package com.dlz.db.internal.mapper.name;

public interface IConvertorToDbName {
	/**
	 * bean字段名转为数据库字段名
	 * @param beanKey
	 	 */
	String toDbName(String beanKey);
}
