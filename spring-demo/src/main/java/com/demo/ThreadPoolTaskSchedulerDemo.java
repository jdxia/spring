package com.demo;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Instant;


public class ThreadPoolTaskSchedulerDemo {

	public static void main(String[] args) {

		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(5);
		taskScheduler.initialize();

//		taskScheduler.scheduleAtFixedRate()

		taskScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				System.out.println("执行任务");
			}
		}, new Trigger() {
			@Override
			public Instant nextExecution(TriggerContext triggerContext) {
				// 获取上一次任务执行完成的时间
				Instant lastCompletion = triggerContext.lastCompletion();
				// 获取上一次任务执行完成的时间加上1000ms
				return lastCompletion != null ? lastCompletion.plusMillis(1000) : Instant.now();
			}
		});


	}
}
