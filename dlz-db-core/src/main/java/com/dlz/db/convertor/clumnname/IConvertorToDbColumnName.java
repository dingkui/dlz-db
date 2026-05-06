package com.dlz.db.convertor.clumnname;

public interface IConvertorToDbColumnName {
	/**
	 * bean字段名转为数据库字段名
	 * @param beanKey
	 	 */
	String toDbColumnName(String beanKey);
}
