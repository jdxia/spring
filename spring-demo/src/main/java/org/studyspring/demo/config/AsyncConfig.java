package org.studyspring.demo.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Async 若把注解放在类上或者接口上，那么他所有的方法都会异步执行了~~~~（包括私有方法）
 * 可以放在任何方法上，哪怕你是private的（若是同类调用，请务必注意注解失效的情况~~~）
 * @Async 可以放在接口处（或者接口方法上）。但是只有使用的是JDK的动态代理时才有效，CGLIB会失效。因此建议：统一写在实现类的方法上
 * <p>
 * <br> 若你希望得到异步调用的返回值，请你的返回值用 Future 变量包装起来 </br>
 */
@EnableAsync // 开启异步注解的支持
@Configuration
public class AsyncConfig implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 这一步千万不能忘了，否则报错： java.lang.IllegalStateException: ThreadPoolTaskExecutor not initialized
		executor.initialize();

		executor.setCorePoolSize(4); //核心线程数
		executor.setMaxPoolSize(8);  //最大线程数
		executor.setQueueCapacity(50); //队列大小
		executor.setKeepAliveSeconds(300); //线程最大空闲时间
		executor.setThreadNamePrefix("Async-Executor-"); //指定用于新创建的线程名称的前缀。
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略（一共四种，此处省略）
		// 优雅地关闭线程池
		// 该方法用来设置线程池关闭的时候等待所有任务都完成后，再继续销毁其他的Bean，
		// 这样异步任务的销毁就会先于数据库连接池对象的销毁。
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(10);

		return executor;
	}

	// 异常处理器：当然你也可以自定义的，这里我就这么简单写了
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}
}

