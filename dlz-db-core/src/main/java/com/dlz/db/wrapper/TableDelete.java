package com.dlz.db.wrapper;

import com.dlz.db.internal.inf.IExecutorDelete;
import com.dlz.db.internal.para.AQuery;

/**
 * 构造单表的删除操作sql
 * @author dingkui
 *
 */
public class TableDelete extends AQuery<TableDelete> implements IExecutorDelete<TableDelete> {
	private static final long serialVersionUID = 8374167270612933157L;
	public TableDelete(String tableName){
		super(tableName);
	}
	@Override
	public TableDelete me() {
		return this;
	}

	@Override
	public String getSql() {
		return WrapperBuildUtil.MAKER_SQL_DELETE;
	}
}
