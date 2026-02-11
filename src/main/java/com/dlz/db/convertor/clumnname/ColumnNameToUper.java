package com.dlz.db.convertor.clumnname;

public class ColumnNameToUper implements IColumnNameConvertor {


	@Override
	public String toFieldName(String dbKey) {
		return dbKey.toUpperCase();
	}

	@Override
	public String toDbColumnName(String beanKey) {
		return beanKey;
	}
}
