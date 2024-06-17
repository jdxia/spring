package org.studyspring.demo.bean.aop;

import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.studyspring.demo.bean.xml.User;

import javax.annotation.Resource;

@Component
public class B {

	@Resource
	@Lazy
	public A a;

	public B() {
		System.out.println("bean B create");
	}


	@MyAop
	@Async("customExecutor")
	public void test() {
		System.out.println("B test");
	}

	public A getA() {
		return a;
	}

	public void setA(A a) {
		this.a = a;
	}
}
