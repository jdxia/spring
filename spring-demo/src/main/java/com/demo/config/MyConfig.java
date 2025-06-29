package com.demo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

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
		// 优雅地关闭线程池
		// 该方法用来设置线程池关闭的时候等待所有任务都完成后，再继续销毁其他的Bean，
		// 这样异步任务的销毁就会先于数据库连接池对象的销毁。
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(10);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();

		return executor;
	}

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
