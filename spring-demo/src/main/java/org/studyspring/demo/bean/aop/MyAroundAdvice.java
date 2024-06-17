package org.studyspring.demo.bean.aop;

import org.aopalliance.intercept.MethodInvocation;

public class MyAroundAdvice implements org.aopalliance.intercept.MethodInterceptor {

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		System.out.println("方法执行around前...");

		// 非常核心
		// 看下 org.springframework.aop.framework.ReflectiveMethodInvocation.proceed
		Object proceed = invocation.proceed();

		System.out.println("方法执行around后...");
		return proceed;
	}
}
