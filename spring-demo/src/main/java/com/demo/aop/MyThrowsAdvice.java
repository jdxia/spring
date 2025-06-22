package com.demo.aop;

import org.springframework.aop.ThrowsAdvice;

import java.lang.reflect.Method;

public class MyThrowsAdvice implements ThrowsAdvice {

	public void afterThrowing(Method method, Object[] args, Object target, NullPointerException ex) {
		System.out.println("afterThrowing NullPointerException");
	}

	public void afterThrowing(Method method, Object[] args, Object target, Exception ex) {
		System.out.println("afterThrowing Exception");
	}
}
