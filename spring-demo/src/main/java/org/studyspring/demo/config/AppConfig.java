package org.studyspring.demo.config;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 如果 @Configuration 配置类被 @Conditional 标记，则与该类关联的所有 @Bean 的工厂方法，@Import 注解和 @ComponentScan 注解也将受条件限制。
 */
@Configuration()
@ComponentScan("org.studyspring.**")
@EnableTransactionManagement // 开启事务
// Spring自己没有定义关于切面相关的注解，而是使用来自org.aspectj这个Jar包里面的注解的（但是没有用它的技术解析，这点需要明白）
//@EnableAspectJAutoProxy(proxyTargetClass=true, exposeProxy=true)  // 开启aop 设置cglib代理, 属性默认是false
@EnableScheduling
@EnableCaching
@EnableAsync
public class AppConfig {

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("mycache");
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		/**
		 * 设置数据源, 数据源格式很多, 有 DriverManagerDataSource 和 AbstractRoutingDataSource 或者 HikariDataSource
		 *
		 * AbstractRoutingDataSource 是动态数据源的
		 */
		transactionManager.setDataSource(dataSource());
		// 是不是要在部分失败的场景下, 默认true 全局回滚
		// 底层意思是 部分失败不回滚, 如果 sql1成功, sql2方法抛异常, 部分失败不回滚, 也就是 sql1和sql2都成功
		transactionManager.setGlobalRollbackOnParticipationFailure(true);
		return transactionManager;
	}

	/**
	 * DriverManagerDataSource 类型
	 * <p> 只是封装了一下连接mysql需要用的一些属性，比如userName,password,url,driver，就是获取Connection封装了一下
	 */
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8&verifyServerCertificate=false&useSSL=false&allowPublicKeyRetrieval=true");
		dataSource.setUsername("root");
		dataSource.setPassword("hello.world123");
 		return dataSource;
	}

	@Bean(name = "customExecutor")
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("CustomExecutor-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(10);

		executor.initialize();
		return executor;
	}

/**
	@Bean
	public DefaultPointcutAdvisor defaultPointcutAdvisor() {
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		pointcut.addMethodName("sayHello");

		DefaultPointcutAdvisor defaultPointcutAdvisor = new DefaultPointcutAdvisor();
		defaultPointcutAdvisor.setPointcut(pointcut);
		defaultPointcutAdvisor.setAdvice(new MyBeforeAdvice());

		return defaultPointcutAdvisor;
	}

	// 他就是 BeanPostProcessor, 他会在初始化后找到所有符合的 Advisor, 然后走advisor的逻辑
	@Bean
	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
		return defaultAdvisorAutoProxyCreator;
	}
*/

/**
	@Bean
	public BeanNameAutoProxyCreator myBeanNameAutoProxyCreator() {
		// 他就是 BeanPostProcessor
		BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
		beanNameAutoProxyCreator.setBeanNames("*Service");
		beanNameAutoProxyCreator.setInterceptorNames("myBeforeAdvice");
		beanNameAutoProxyCreator.setProxyTargetClass(true);
		return beanNameAutoProxyCreator;
	}
*/


}

