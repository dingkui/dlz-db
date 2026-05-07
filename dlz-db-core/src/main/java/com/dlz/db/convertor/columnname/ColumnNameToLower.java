package com.dlz.db.convertor.columnname;

public class ColumnNameToLower implements IColumnNameConvertor {

	@Override
	public String toFieldName(String dbKey) {
		return dbKey.toLowerCase();
	}

	@Override
	public String toDbColumnName(String beanKey) {
		return beanKey;
	}
}
