package com.dlz.db.mapper.name;

import java.util.Locale;

public class NameConvertToUper implements INameConverter {
	@Override
	public String toFieldName(String dbKey) {
		return dbKey.toUpperCase(Locale.ROOT);
	}

	@Override
	public String toDbName(String beanKey) {
		return beanKey.toLowerCase(Locale.ROOT);
	}
}
