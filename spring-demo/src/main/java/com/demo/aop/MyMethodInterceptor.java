package com.demo.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class MyMethodInterceptor implements MethodInterceptor {
	@Nullable
	@Override
	public Object invoke(@Nonnull MethodInvocation mi) throws Throwable {

		System.out.println("around before");
		Object result = mi.proceed();  // 下一个MethodInterceptor的invoke()
		System.out.println("around after");

		return result;
	}
}
