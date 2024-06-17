package org.studyspring.demo.bean.aop;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
 public class A {

	@Resource
	@Lazy
	public B b;

	public A() {
		System.out.println("bean A create");
	}

	@MyAop
	@Async("customExecutor")
	public void test() {

		b.test();
		System.out.println("A test ========>");
	}

	public void test2() {

		System.out.println("A test2");
	}

	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}

}

