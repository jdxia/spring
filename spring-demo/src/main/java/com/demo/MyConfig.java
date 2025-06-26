package com.demo;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.Arrays;

@ComponentScan("com.demo")
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@EnableAsync
@Configuration
@EnableScheduling
//@PropertySource("application.properties")
public class MyConfig {


//	@Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
	@Bean()
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
		executor.setThreadNamePrefix("myDemo-");
		executor.initialize();

		return executor;
	}

//	@Bean
//	public TaskScheduler taskScheduler() {
//		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
//		taskScheduler.setPoolSize(5);
//		taskScheduler.setThreadNamePrefix("myDemo-");
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




}
