package org.studyspringmybatis.demo;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.studyspringmybatis.demo.bean.UserService;
import org.studyspringmybatis.demo.config.AppConfig;

import java.lang.reflect.Field;

public class SpringMybatis {

	public static void main(String[] args) throws Exception {
		System.out.println("=====================> debug start");

		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

		UserService userService = ac.getBean(UserService.class);
		userService.testMybatisMapper();

		System.out.println("=====================> debug stop");

	}


}
