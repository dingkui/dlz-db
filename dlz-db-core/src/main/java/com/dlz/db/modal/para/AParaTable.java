package com.dlz.db.modal.para;

import com.dlz.db.inf.ISqlPara;
import com.dlz.db.modal.options.DbOptionAware;
import com.dlz.db.modal.options.DbOptions;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.bean.TableInfo;
import com.dlz.db.util.DbConvertUtil;

/**
 * 构造单表的增删改查操作sql
 *
 * @author dingkui
 */
public abstract class AParaTable<T extends AParaTable> extends ParaMap<T>
        implements ISqlPara, DbOptionAware {
    private static final long serialVersionUID = 8374167270612933157L;
    private final String tableName;
    private transient volatile TableInfo tableInfo;
    private DbOptions dbOptions = DbOptions.EMPTY;

    protected AParaTable(String tableName) {
        DbConvertUtil.validateDbName(tableName,"表名");
        this.tableName = tableName;
    }
    public String getTableName() {
        return tableName;
    }

    public T options(DbOptions options) {
        this.dbOptions = options == null ? DbOptions.EMPTY : options;
        return me();
    }

    @Override
    public final DbOptions getDbOptions() {
        return dbOptions;
    }

    /**
     * 按需取得当前数据源下的完整表元数据。普通不依赖元数据的 Wrapper 不会触发数据库查询。
     */
    public final TableInfo getTableInfo() {
        TableInfo local = tableInfo;
        if (local == null) {
            synchronized (this) {
                local = tableInfo;
                if (local == null) {
                    local = PojoCache.getTableInfo(tableName);
                    tableInfo = local;
                }
            }
        }
        return local;
    }

    public abstract String getSql();
}


