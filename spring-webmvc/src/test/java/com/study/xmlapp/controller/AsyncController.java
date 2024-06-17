package com.study.xmlapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/async")
public class AsyncController {


	// 实际使用中，我并不建议直接使用Callable ，而是使用Spring提供的WebAsyncTask 代替，它包装了Callable，功能更强大些
	// Get http://127.0.0.1:8272/async/hello
	@ResponseBody
	@GetMapping("/hello")
	public Callable<String> helloGet() throws Exception {
		System.out.println(Thread.currentThread().getName() + " 主线程start");

		Callable<String> callable = () -> {
			System.out.println(Thread.currentThread().getName() + " 子线程start");
			TimeUnit.SECONDS.sleep(5); //模拟处理业务逻辑，话费了5秒钟
			System.out.println(Thread.currentThread().getName() + " 子线程end");

			// 这里稍微注意一下：最终返回的不是Callable对象，而是它里面的内容
			return Thread.currentThread().getName() + " 子线程===========> hello world";
		};

		// 主线程早早就结束了（需要注意，此时还并没有把response返回的，此处一定要注意）
		// 真正干事的是子线程（交给TaskExecutor去处理的）
		System.out.println(Thread.currentThread().getName() + " 主线程end");
		return callable;
	}

	// Get http://127.0.0.1:8272/async/task
	@ResponseBody
	@GetMapping("/task")
	public WebAsyncTask<String> taskGet() throws Exception {
		System.out.println(Thread.currentThread().getName() + " 主线程start");

		Callable<String> callable = () -> {
			System.out.println(Thread.currentThread().getName() + " 子线程start");
			TimeUnit.SECONDS.sleep(5); //模拟处理业务逻辑，话费了5秒钟
			System.out.println(Thread.currentThread().getName() + " 子线程end");

			return "hello world";
		};

		// 采用WebAsyncTask 返回 这样可以处理超时和错误 同时也可以指定使用的 executor 名称
		WebAsyncTask<String> webAsyncTask = new WebAsyncTask<>(3000, callable);
		// 注意：onCompletion表示完成，不管你是否超时、是否抛出异常，这个函数都会执行的
		webAsyncTask.onCompletion(() -> System.out.println("程序[正常执行]完成的回调"));

		// 这两个返回的内容，最终都会放进response里面去===========
		webAsyncTask.onTimeout(() -> "程序[超时]的回调");
		// 备注：这个是Spring5新增的
		webAsyncTask.onError(() -> "程序[出现异常]的回调");


		System.out.println(Thread.currentThread().getName() + " 主线程end");
		return webAsyncTask;
	}

	// Get http://127.0.0.1:8272/async/test2
	@GetMapping("/test2")
	@ResponseBody
	public String test2(HttpServletRequest request){
		AsyncContext asyncContext = request.startAsync();
		asyncContext.start(() -> {
			try {
				TimeUnit.SECONDS.sleep(3);
				System.out.println(Thread.currentThread().getName() + " ===>测试任务开始");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		asyncContext.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) {
				System.out.println(Thread.currentThread().getName() + " ===>onComplete");
			}

			@Override
			public void onTimeout(AsyncEvent event) {
				System.out.println(Thread.currentThread().getName() + " ===>onTimeout");
			}

			@Override
			public void onError(AsyncEvent event) {
				System.out.println(Thread.currentThread().getName() + " ===>onError");
			}

			@Override
			public void onStartAsync(AsyncEvent event) {
				System.out.println(Thread.currentThread().getName() + " ===>onStartAsync");
			}
		});
		return "success";
	}


	/**
	 * DeferredResult使用方式与Callable类似，但在返回结果上不一样，它返回的时候实际结果可能没有生成，实际的结果可能会在另外的线程里面设置到DeferredResult中去。
	 *
	 * 这个特性非常非常的重要，可以实现复杂的功能（比如服务端推技术、订单过期时间处理、长轮询、模拟MQ的功能等等高级应用）
	 */
	private volatile List<DeferredResult<String>> deferredResultList = new LinkedList<>();

	// Get http://127.0.0.1:8272/async/deferred
	@ResponseBody
	@GetMapping("/deferred")
	public DeferredResult<String> deferredGet() throws Exception {
		DeferredResult<String> deferredResult = new DeferredResult<>();

		// servlet的线程
		System.out.println(Thread.currentThread().getName() + " deferredGet method");

		//先存起来，等待触发
		deferredResultList.add(deferredResult);

		/**
		 这样主线程就能立刻释放了
		 executorService.execute(()->{
			 try {
				 Thread.sleep(6000);
				 dr.setResult("成功");
			 } catch (InterruptedException e) {
				e.printStackTrace();
			 }
		 });
		 */
		return deferredResult;
	}

	// Get http://127.0.0.1:8272/async/setHelloToAll
	@ResponseBody
	@GetMapping("/setHelloToAll")
	public void helloSet() throws Exception {
		// 让所有hold住的请求给与响应
		deferredResultList.forEach(d -> {
			// servlet的线程, 可以转移到其他线程中set值
			System.out.println(Thread.currentThread().getName() + " setHelloToAll method");
			d.setResult("say hello to all");
		});
	}


}
