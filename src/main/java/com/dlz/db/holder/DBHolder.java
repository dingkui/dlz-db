package com.dlz.db.holder;

import com.dlz.comm.util.StringUtils;
import com.dlz.comm.util.ValUtil;
import com.dlz.db.convertor.rowMapper.ResultMapRowMapper;
import com.dlz.db.dao.IDlzDao;
import com.dlz.db.service.ICommService;
import com.dlz.db.service.impl.CommServiceImpl;
import com.dlz.spring.holder.SpringHolder;
import com.dlz.spring.redis.excutor.JedisExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.function.Function;

/**
 * 数据库配置信息
 */
@Slf4j
public class DBHolder {
    public static IDlzDao dao;
    public static ICommService service;
    public static JedisExecutor jedis;

    public static IDlzDao getDao() {
        if (dao == null) {
            dao = SpringHolder.getBean(IDlzDao.class);
        }
        return dao;
    }

    public static ICommService getService() {
        if (service == null) {
            service = SpringHolder.registerBean(CommServiceImpl.class);
            if (log.isInfoEnabled()) {
                log.debug("commService:" + CommServiceImpl.class.getName());
            }
        }
        return service;
    }

    public static JedisExecutor getJedis() {
        if (jedis == null) {
            jedis = SpringHolder.getBean(JedisExecutor.class);
        }
        return jedis;
    }

    public static long sequence(String tableName, long initSeq) {
        String key = "seq:" + tableName;
        Long seq = getJedis().incrBy(key, initSeq);
        if (seq == initSeq) {
            try {
                final String fistColumn = getDao().getFistColumn("select max(id) from " + tableName, String.class);
                if (fistColumn == null || !StringUtils.isNumber(fistColumn)) {
                    return seq;
                }
                seq = ValUtil.toBigDecimalZero(fistColumn).longValue() + initSeq;
                if (seq > initSeq) {
                    jedis.set(key, seq.toString());
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return seq;
    }

    public static long sequence(Class<?> beanClass, long initSeq) {
        return sequence(BeanInfoHolder.getTableName(beanClass), initSeq);
    }

    public static <R> R doDb(Function<ICommService, R> s) {
        return s.apply(getService());
    }

    public static <R> R doDao(Function<IDlzDao, R> s) {
        return s.apply(getDao());
    }
}
