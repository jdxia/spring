package com.study.xmlapp.interceptor;

import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class AsyncHelloInterceptor implements AsyncHandlerInterceptor {

	// 这是Spring3.2提供的方法，专门拦截异步请求的方式
	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		/**
		 * 在servlet线程中执行的
		 *
		 * 这个方法会在Controller方法异步执行时开始执行, 而Interceptor的postHandle方法则是需要等到Controller的异步执行完才能执行
		 * 如果我们不是异步请求，afterConcurrentHandlingStarted是不会执行的。所以我们可以把它当做加强版的HandlerInterceptor来用
		 *
		 * 注意里面有异常的话也只是打印日志, 不会抛出来
		 */

		System.out.println(Thread.currentThread().getName() + "---Async Interceptor afterConcurrentHandlingStarted-->" + request.getRequestURI());
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		DispatcherType dispatcherType = request.getDispatcherType();
		if (dispatcherType == DispatcherType.REQUEST) {
			// 会先进这里, 因为先是 普通的请求
			System.out.println("============> Request is a normal synchronous request.");
		} else if (dispatcherType == DispatcherType.ASYNC) {
			// 然后会进这里, 异步会来
			System.out.println("============> Request is an asynchronous request.");
		}

		System.out.println(Thread.currentThread().getName() + "---Async Interceptor preHandle-->" + request.getRequestURI());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		System.out.println(Thread.currentThread().getName() + "---Async Interceptor postHandle-->" + request.getRequestURI());
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		System.out.println(Thread.currentThread().getName() + "---Async Interceptor afterCompletion-->" + request.getRequestURI());
	}
}
