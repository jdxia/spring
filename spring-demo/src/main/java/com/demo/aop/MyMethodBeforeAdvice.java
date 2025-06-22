package com.demo.aop;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;


public class MyMethodBeforeAdvice implements MethodBeforeAdvice {

	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		System.out.println("切面逻辑 before");
	}
}
