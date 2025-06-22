package com.demo;

import com.demo.aop.MyMethodBeforeAdvice;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;


//@Component
//@Order(5)
public class MyAdvisor implements PointcutAdvisor {
	@Override
	public Advice getAdvice() {
		return new MyMethodBeforeAdvice();
	}

	@Override
	public Pointcut getPointcut() {

		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		pointcut.addMethodName("a");
		return pointcut;
	}
}
