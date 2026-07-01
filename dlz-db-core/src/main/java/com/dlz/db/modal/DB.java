package com.dlz.db.modal;

/**
 * dlz-db 唯一入口。
 * <p>只挂 8 个 final 字段（门面注册表），不挂任何业务方法——这是 5 年不变的核心。
 *
 * <p>8 个门面分三组：
 * <ul>
 *   <li>数据操作：pojo / table / jdbc / sql</li>
 *   <li>基础设施：tx / ds / batch</li>
 *   <li>辅助工具：config</li>
 * </ul>
 */
public class DB {
    private DB() {}

    /** 实体驱动，Lambda 类型安全。有实体类时用。 */
    public final static DbPojo pojo = new DbPojo();
    /** 表名驱动，无需实体。动态表名、报表、低代码时用。 */
    public final static DbTable table = new DbTable();
    /** 原生 SQL + ? 占位。一句 SQL 跑完的简单场景。 */
    public final static DbJdbc jdbc = new DbJdbc();
    /** 预设 SQL + #{} 命名参数。复用/管理/动态配置的复杂场景。 */
    public final static DbSql sql = new DbSql();
    /** 批量操作。大量数据写入。 */
    public final static DbBatch batch = new DbBatch();
    /** 事务执行器。需要原子性时用。 */
    public final static DbTx tx = new DbTx();
    /** 数据源管理。多租户、动态数据源、灰度。 */
    public final static DbDs ds = new DbDs();
    /** 配置与扩展注册。注册插件/拦截器/方言。 */
    public final static DbConfig config = new DbConfig();
}
