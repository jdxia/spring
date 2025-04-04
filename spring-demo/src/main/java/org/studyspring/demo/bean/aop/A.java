package org.studyspring.demo.bean.aop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
 public class A {

	//注入系统参数、环境变量或者配置文件中的值
	@Value("${ip}")
	private String ip;

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

