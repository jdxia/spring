package com.demo;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ScheduledThreadPoolExecutorDemo {

	public static void main(String[] args) throws IOException {

		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);

		// 延迟执行任务，只执行一次
//		executor.schedule(new Runnable() {
//			@Override
//			public void run() {
//				System.out.println("执行任务");
//			}
//		}, 3, TimeUnit.SECONDS);


		// 按照固定频率执行任务
//		executor.scheduleAtFixedRate(new Runnable() {
//			@Override
//			public void run() {
//				System.out.println("执行任务");
//				System.out.println(Thread.currentThread().getName());
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					throw new RuntimeException(e);
//				}
//			}
//		}, 0, 1000, TimeUnit.MILLISECONDS);

//		// 按照固定延迟执行任务
		executor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				System.out.println("执行任务");
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

}
