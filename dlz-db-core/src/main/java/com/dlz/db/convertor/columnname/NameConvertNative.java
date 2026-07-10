package com.dlz.db.convertor.columnname;

public class NameConvertNative implements INameConverter {

	@Override
	public String toFieldName(String dbKey) {
		return dbKey;
	}

	@Override
	public String toDbName(String beanKey) {
		return beanKey;
	}
}
