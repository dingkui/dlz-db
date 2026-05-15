package com.dlz.test.db.config;

import com.dlz.db.support.DBHolder;
import com.dlz.db.support.PojoCache;
import com.dlz.db.support.resouce.DlzResourceLoader;
import com.dlz.db.annotation.TableField;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 测试基类 - 自动初始化 MockDbProvider
 * 所有需要数据库功能的测试类继承此类即可
 */
public abstract class BaseDBTest {

    @BeforeAll
    static void initMockDb() throws Exception {
        // 设置 MockDbProvider
        MockDbProvider mockProvider = new MockDbProvider();
        DBHolder.setDbProvider(mockProvider);

        // 扫描当前测试类所在的包
        Set<Class<?>> classes = DlzResourceLoader.scan("com.dlz.test.db.entity", null);
        assertNotNull(classes);


        // 为 TestUser 类预置表字段信息到缓存中
        // 表名是 TEST_USER（根据类名转换）
        HashMap<String, Integer> tableColumns = new HashMap<>();
        tableColumns.put("ID", 1);      // BIGINT
        tableColumns.put("NAME", 2);    // VARCHAR
        tableColumns.put("AGE", 3);     // INTEGER
        tableColumns.put("EMAIL", 4);   // VARCHAR

        // CacheMap 继承自 ConcurrentHashMap，可以直接 put
        try {
            java.lang.reflect.Field cacheField = PojoCache.class.getDeclaredField("tableColumnsInfoCache");
            cacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.concurrent.ConcurrentHashMap<String, HashMap<String, Integer>> cache =
                    (java.util.concurrent.ConcurrentHashMap<String, HashMap<String, Integer>>) cacheField.get(null);
            cache.put("TEST_USER", tableColumns);
            
            // 遍历所有扫描到的实体类，为每个类预置表字段信息
            classes.forEach(clazz -> {
                try {
                    // 获取表名
                    String tableName = PojoCache.getTableName(clazz);
                    
                    // 获取所有字段（使用反射获取所有声明的字段）
                    Field[] declaredFields = clazz.getDeclaredFields();
                    
                    // 构建字段映射：数据库列名 -> JDBC类型索引
                    HashMap<String, Integer> columnsMap = new HashMap<>();
                    int index = 1;
                    
                    for (Field field : declaredFields) {
                        // 检查字段是否应该被排除（@TableField(exist=false)）
                        TableField tableFieldAnnotation = field.getAnnotation(TableField.class);
                        if (tableFieldAnnotation != null && !tableFieldAnnotation.exist()) {
                            continue; // 跳过不存在的字段
                        }
                        
                        // 获取数据库列名（通过 PojoCache 转换）
                        String columnName = PojoCache.getColumnName(field.getName());
                        
                        // 根据字段类型设置 JDBC 类型索引（简化版本，实际可以根据需要扩展）
                        Integer jdbcType = getJdbcTypeIndex(field.getType());
                        
                        columnsMap.put(columnName, jdbcType != null ? jdbcType : index);
                        index++;
                    }
                    
                    // 将字段信息放入缓存
                    if (!columnsMap.isEmpty()) {
                        cache.put(tableName, columnsMap);
                        System.out.println("已为表 [" + tableName + "] 注入 " + columnsMap.size() + " 个字段");
                    }
                } catch (Exception e) {
                    System.err.println("处理实体类 [" + clazz.getName() + "] 时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup mock table columns", e);
        }

        System.out.println("---------------------------------------------------------");
    }
    
    /**
     * 根据 Java 类型获取简化的 JDBC 类型索引
     * @param type Java 类型
     * @return JDBC 类型索引
     */
    private static Integer getJdbcTypeIndex(Class<?> type) {
        if (type == null) {
            return null;
        }
        
        // String -> VARCHAR (12)
        if (String.class.isAssignableFrom(type)) {
            return 12;
        }
        
        // Integer, int -> INTEGER (4)
        if (type == Integer.class || type == int.class) {
            return 4;
        }
        
        // Long, long -> BIGINT (-5)
        if (type == Long.class || type == long.class) {
            return -5;
        }
        
        // Double, double -> DOUBLE (8)
        if (type == Double.class || type == double.class) {
            return 8;
        }
        
        // Float, float -> FLOAT (6)
        if (type == Float.class || type == float.class) {
            return 6;
        }
        
        // Boolean, boolean -> BOOLEAN (16)
        if (type == Boolean.class || type == boolean.class) {
            return 16;
        }
        
        // Date, java.sql.Date, java.sql.Timestamp -> TIMESTAMP (93)
        if (java.util.Date.class.isAssignableFrom(type) || 
            java.sql.Date.class.isAssignableFrom(type) ||
            java.sql.Timestamp.class.isAssignableFrom(type)) {
            return 93;
        }
        
        // BigDecimal -> DECIMAL (3)
        if (java.math.BigDecimal.class.isAssignableFrom(type)) {
            return 3;
        }
        
        // Byte, byte -> TINYINT (-6)
        if (type == Byte.class || type == byte.class) {
            return -6;
        }
        
        // Short, short -> SMALLINT (5)
        if (type == Short.class || type == short.class) {
            return 5;
        }
        
        // 默认返回 null，让调用方使用递增索引
        return null;
    }
}
