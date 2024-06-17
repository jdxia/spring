package org.studyspring.demo.bean.aop;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Component
@Aspect
public class MyAspect {

	@Pointcut("@annotation(MyAop)")  // 定义切入点
	public void myAopPointcut() {
	}

	@Before("myAopPointcut()")
	public void myAsppectBefore() {
		System.out.println("<=== myAsppectBefore ===>");
	}

	@After("myAopPointcut()")
	public void myAsppectAfter() {
		System.out.println("<=== myAsppectAfter ===>");
	}

}
