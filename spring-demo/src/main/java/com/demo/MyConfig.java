package com.demo;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import java.util.Arrays;

@ComponentScan("com.demo")
@MapperScan("com.demo.mapper")
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@EnableAsync
@Configuration
@EnableScheduling
@PropertySource("application.properties")
public class MyConfig {


	@Bean
	public ConcurrentMapCacheManager cacheManager() {
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
		cacheManager.setCacheNames(Arrays.asList("cache1", "cache2"));
		return cacheManager;
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(50);
		executor.setThreadNamePrefix("zhouyu-");
		executor.initialize();

		return executor;
	}

//	@Bean
//	public TaskScheduler taskScheduler() {
//		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
//		taskScheduler.setPoolSize(5);
//		taskScheduler.setThreadNamePrefix("zhouyu-");
//		taskScheduler.initialize();
//
//		return taskScheduler;
//	}

//	@Bean
//	public SchedulingConfigurer schedulingConfigurer() {
//		return new SchedulingConfigurer() {
//			@Override
//			public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//				taskRegistrar.addTriggerTask(new Runnable() {
//					@Override
//					public void run() {
//						System.out.println("任务...");
//					}
//				}, new Trigger() {
//					@Override
//					public Instant nextExecution(TriggerContext triggerContext) {
//						Instant lastCompletion = triggerContext.lastCompletion();
//						// 获取上一次任务执行完成的时间加上1000ms
//						return lastCompletion != null ? lastCompletion.plusMillis(1000) : Instant.now();
//					}
//				});
//			}
//		};
//	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dataSource());
//		transactionManager.setGlobalRollbackOnParticipationFailure(false);
		return transactionManager;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource());
		return sqlSessionFactoryBean.getObject();
	}

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/my_db");
		dataSource.setUsername("root");
		dataSource.setPassword("Zhouyu123456...");
		return dataSource;
	}


}
