package com.dlz.db.solon;

import com.dlz.db.core.DlzDbProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Solon 数据库配置属性。
 * <p>纯 POJO；由 {@link DlzDbSolonPlugin} 通过 {@code Solon.cfg().getBean("dlz.db", SolonDbProperties.class)}
 * 从配置前缀 {@code dlz.db} 自动绑定字段。</p>
 *
 * <p>使用示例（{@code app.yml}）：</p>
 * <pre>
 * dlz:
 *   db:
 *     sqllist:
 *       - app/*
 *     useDbSql: false
 *     log:
 *       showRunSql: true
 * </pre>
 *
 * @since 7.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SolonDbProperties extends DlzDbProperties {
}
