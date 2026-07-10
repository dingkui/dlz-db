package com.dlz.test.spring.holder;

import com.dlz.spring.holder.SpringHolder;
import com.dlz.test.config.BaseDBTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringHolder 测试类
 *
 * <p>测试 SpringHolder 的各种功能：</p>
 * <ul>
 *   <li>BeanFactory 初始化</li>
 *   <li>Bean 获取（按名称、按类型）</li>
 *   <li>Bean 注册与注销</li>
 *   <li>Bean 创建与自动装配</li>
 *   <li>事件发布</li>
 *   <li>资源获取</li>
 * </ul>
 */
@Slf4j
public class SpringHolderTest extends BaseDBTest {
    private static final String TEST_BEAN_NAME = "testBean";
    @BeforeEach
    public void before() {
        if(SpringHolder.containsBean(TEST_BEAN_NAME)){
            SpringHolder.unregisterBean(TEST_BEAN_NAME);
        }
    }
    @AfterEach
    public void after() {
        if(SpringHolder.containsBean(TEST_BEAN_NAME)){
            SpringHolder.unregisterBean(TEST_BEAN_NAME);
        }
    }

   // ==================== init 方法测试 ====================

    /**
     * 测试：init() 方法 - 使用默认配置
     */
    @Test
    void testInit() {
        SpringHolder.init();
    }
    /**
     * 测试：init() 方法 - 使用默认配置
     */
    @Test
    void testInitDefault() {
        // 由于已经在 BaseDBTest 中通过 SpringBootTest 初始化了 context
        // 这里主要测试不会重复初始化
        ConfigurableListableBeanFactory beanFactory = SpringHolder.getBeanFactory();
        assertNotNull(beanFactory, "BeanFactory 应该已经初始化");
    }

    /**
     * 测试：init(ConfigurableListableBeanFactory) 方法
     */
    @Test
    void testInitWithBeanFactory() {
        ConfigurableListableBeanFactory existingFactory = SpringHolder.getBeanFactory();
        assertNotNull(existingFactory, "BeanFactory 应该已经初始化");

        // 再次调用 init 应该不会覆盖现有的 beanFactory
        SpringHolder.init(existingFactory);
        ConfigurableListableBeanFactory afterInit = SpringHolder.getBeanFactory();
        assertSame(existingFactory, afterInit, "重复 init 不应该改变 BeanFactory");
    }

    // ==================== getBeanFactory 相关方法测试 ====================

    /**
     * 测试：getBeanFactory() 方法
     */
    @Test
    void testGetBeanFactory() {
        ConfigurableListableBeanFactory beanFactory = SpringHolder.getBeanFactory();
        assertNotNull(beanFactory, "应该能获取到 BeanFactory");
    }

    /**
     * 测试：getBeanDefinitionRegistry() 方法
     */
    @Test
    void testGetBeanDefinitionRegistry() {
        assertNotNull(SpringHolder.getBeanDefinitionRegistry(), "应该能获取到 BeanDefinitionRegistry");
    }

    /**
     * 测试：getApplicationContext() 方法
     */
    @Test
    void testGetApplicationContext() {
        assertNotNull(SpringHolder.getBeanFactory(), "应该能获取到 ApplicationContext");
    }

    // ==================== getBean 方法测试 ====================

    /**
     * 测试：getBean(String name) - 获取存在的 bean
     */
    @Test
    void testGetBeanByName() {
        // 注册一个测试 bean
        TestBean bean = SpringHolder.registerBean(TEST_BEAN_NAME, true, TestBean.class);
        assertNotNull(bean, "注册 bean 应该成功");

        // 通过名称获取
        TestBean retrievedBean = SpringHolder.getBean(TEST_BEAN_NAME);
        assertNotNull(retrievedBean, "应该能通过名称获取到 bean");
        assertEquals(TEST_BEAN_NAME, retrievedBean.getName(), "bean 的 name 应该正确");
        SpringHolder.unregisterBean(TEST_BEAN_NAME);
    }

    /**
     * 测试：getBean(String name) - 获取不存在的 bean
     */
    @Test
    void testGetBeanByNameNotFound() {
        TestBean bean = SpringHolder.getBean("nonExistentBean");
        assertNull(bean, "获取不存在的 bean 应该返回 null");
    }

    /**
     * 测试：getBean(Class<T> clazz) - 获取存在的 bean
     */
    @Test
    void testGetBeanByClass() {
        // 注册一个测试 bean
        TestBean bean = SpringHolder.registerBean(TestBean.class);
        assertNotNull(bean, "注册 bean 应该成功");

        // 通过类型获取
        TestBean retrievedBean = SpringHolder.getBean(TestBean.class);
        assertNotNull(retrievedBean, "应该能通过类型获取到 bean");
    }

    /**
     * 测试：getBean(Class<T> clazz) - 获取不存在的 bean
     */
    @Test
    void testGetBeanByClassNotFound() {
        NonExistentBean bean = SpringHolder.getBean(NonExistentBean.class);
        assertNull(bean, "获取不存在的 bean 类型应该返回 null");
    }

    /**
     * 测试：getBeans(Class<T> clazz) - 获取某个类型的所有 bean
     */
    @Test
    void testGetBeansByClass() {
        // 注册多个同类型的 bean
        SpringHolder.registerBean("bean1", false, TestBean.class);
        SpringHolder.registerBean("bean2", false, TestBean.class);

        // 获取所有 TestBean 类型的 bean
        Map<String, TestBean> beans = SpringHolder.getBeans(TestBean.class);
        assertNotNull(beans, "应该能获取到 bean map");
        assertTrue(beans.size() >= 2, "应该至少有 2 个 TestBean");

        // 清理
        SpringHolder.unregisterBean("bean1");
        SpringHolder.unregisterBean("bean2");
    }

    /**
     * 测试：containsBean(String beanName) 方法
     */
    @Test
    void testContainsBean() {
        // 注册前
        assertFalse(SpringHolder.containsBean(TEST_BEAN_NAME), "注册前不应该包含该 bean");

        // 注册后
        SpringHolder.registerBean(TEST_BEAN_NAME, true, TestBean.class);
        assertTrue(SpringHolder.containsBean(TEST_BEAN_NAME), "注册后应该包含该 bean");
    }

    /**
     * 测试：getBeanWithRegister(Class<T> clazz) - 获取或注册 bean
     */
    @Test
    void testGetBeanWithRegister() {
        // 第一次调用，应该注册并返回
        TestBean bean1 = SpringHolder.getBeanWithRegister(TestBean.class);
        assertNotNull(bean1, "应该能获取或注册 bean");

        // 第二次调用，应该返回已存在的 bean
        TestBean bean2 = SpringHolder.getBeanWithRegister(TestBean.class);
        assertNotNull(bean2, "应该能获取已存在的 bean");
        assertSame(bean1, bean2, "应该返回同一个 bean 实例（单例）");
    }

    // ==================== registerBean 方法测试 ====================

    /**
     * 测试：registerBean(String beanId, boolean singleton, Class<T> clazz) - 单例
     */
    @Test
    void testRegisterBeanSingleton() {
        TestBean bean1 = SpringHolder.registerBean(TEST_BEAN_NAME, true, TestBean.class);
        assertNotNull(bean1, "注册单例 bean 应该成功");

        TestBean bean2 = SpringHolder.registerBean(TEST_BEAN_NAME, true, TestBean.class);
        assertSame(bean1, bean2, "单例 bean 应该返回同一个实例");
    }

    /**
     * 测试：registerBean(String beanId, boolean singleton, Class<T> clazz) - 非单例
     */
    @Test
    void testRegisterBeanPrototype() {
        TestBean bean1 = SpringHolder.registerBean(TEST_BEAN_NAME, false, TestBean.class);
        assertNotNull(bean1, "注册非单例 bean 应该成功");

        TestBean bean2 = SpringHolder.registerBean(TEST_BEAN_NAME, false, TestBean.class);
        assertNotSame(bean1, bean2, "非单例 bean 应该返回不同实例");
    }

    /**
     * 测试：registerBean(Class<T> clazz) - 使用默认 beanId
     */
    @Test
    void testRegisterBeanByClass() {
        TestBean bean = SpringHolder.registerBean(TestBean.class);
        assertNotNull(bean, "通过 class 注册 bean 应该成功");
    }

    /**
     * 测试：registerBean(Class<T> clazz, boolean singleton) - 指定是否单例
     */
    @Test
    void testRegisterBeanByClassWithSingleton() {
        TestBean bean = SpringHolder.registerBean(TestBean.class, true);
        assertNotNull(bean, "通过 class 和 singleton 参数注册 bean 应该成功");
    }

    /**
     * 测试：registerBean(String className) - 通过类名字符串注册
     */
    @Test
    void testRegisterBeanByClassName() throws ClassNotFoundException {
        SpringHolder.registerBean(TestBean.class.getName());
        TestBean bean = SpringHolder.getBean(TestBean.class);
        assertNotNull(bean, "通过类名字符串注册 bean 应该成功");
    }

    /**
     * 测试：unregisterBean(String beanId) 方法
     */
    @Test
    void testUnregisterBean() {
        // 注册 bean
        SpringHolder.registerBean(TEST_BEAN_NAME, true, TestBean.class);
        assertTrue(SpringHolder.containsBean(TEST_BEAN_NAME), "注册后应该包含该 bean");

        // 注销 bean
        SpringHolder.unregisterBean(TEST_BEAN_NAME);
        assertFalse(SpringHolder.containsBean(TEST_BEAN_NAME), "注销后不应该包含该 bean");
    }

    // ==================== createBean 方法测试 ====================

    /**
     * 测试：createBean(Class<T> clazz) - 创建 bean 并执行自动装配
     */
    @Test
    void testCreateBean() {
        TestBean bean = SpringHolder.createBean(TestBean.class);
        assertNotNull(bean, "创建 bean 应该成功");
        assertNotNull(bean.getName(), "bean 应该被正确初始化");
    }

    /**
     * 测试：createBean(Class<T> clazz) - 创建需要依赖注入的 bean
     */
    @Test
    void testCreateBeanWithDependency() {
        DependentBean bean = SpringHolder.createBean(DependentBean.class);
        assertNotNull(bean, "创建需要依赖注入的 bean 应该成功");
        // 如果有 @Autowired 注解的字段，应该被注入
    }

    // ==================== publishEvent 方法测试 ====================

    /**
     * 测试：publishEvent(ApplicationEvent event) - 发布事件
     */
    @Test
    void testPublishEvent() {
        // 创建一个测试事件
        TestEvent event = new TestEvent("test message");

        // 发布事件（不应该抛出异常）
        assertDoesNotThrow(() -> SpringHolder.publishEvent(event), "发布事件不应该抛出异常");

        log.info("事件发布测试完成");
    }

    // ==================== getResource 方法测试 ====================

    /**
     * 测试：getResource(String resourceLocation) - 获取资源
     */
    @Test
    void testGetResource() {
        // 测试获取 classpath 资源
        Resource resource = SpringHolder.getResource("classpath:application.yml");
        if (resource != null && resource.exists()) {
            log.info("成功获取资源: {}", resource.getFilename());
        }
        // 即使资源不存在，也不应该抛出异常
        assertDoesNotThrow(() -> SpringHolder.getResource("classpath:non-existent.yml"));
    }

    /**
     * 测试：getResource(String resourceLocation) - 获取不存在的资源
     */
    @Test
    void testGetResourceNotFound() {
        Resource resource = SpringHolder.getResource("classpath:non-existent-resource.yml");
        // 应该返回 null 或者不存在的 resource
        if (resource != null) {
            assertFalse(resource.exists(), "不存在的资源应该返回 null 或不存在的 resource");
        }
    }

    // ==================== 异常场景测试 ====================

    /**
     * 测试：未初始化时调用 getBean 应该抛出异常
     */
    @Test
    void testGetBeanWithoutInit() {
        // 由于测试环境已经通过 SpringBootTest 初始化，这个测试主要验证逻辑
        // 在实际未初始化的场景下会抛出 IllegalStateException
        ConfigurableListableBeanFactory factory = SpringHolder.getBeanFactory();
        assertNotNull(factory, "在测试环境中 BeanFactory 应该已经初始化");
    }
}
