package com.demo.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Component
@Aspect
public class MyAspect {

//	@DeclareParents(value = "com.zhouyu.service.UserService", defaultImpl = UserImplement.class)
//	private UserInterface userInterface;


	@Pointcut("execution(public void com.demo.service.AService.test())")
	public void a() {
	}

	@Before("a()")
	public void zhouyuBefore2(JoinPoint joinPoint) {
		System.out.println("my Before");
	}

	@Before("execution(public void com.demo.service.UserService.test())")
	public void zhouyuBefore1(JoinPoint joinPoint) {
		System.out.println("my Before1");
	}



}
