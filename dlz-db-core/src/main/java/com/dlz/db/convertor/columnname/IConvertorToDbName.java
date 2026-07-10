package com.dlz.db.convertor.columnname;

public interface IConvertorToDbName {
	/**
	 * bean字段名转为数据库字段名
	 * @param beanKey
	 	 */
	String toDbName(String beanKey);
}
