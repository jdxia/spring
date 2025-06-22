package com.demo;

import com.demo.service.AService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

import java.io.IOException;

public class MyApplication {

	public static void main(String[] args) throws IOException {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(MyConfig.class);
//		applicationContext.setAllowCircularReferences(false);
		applicationContext.refresh();

		AService aService = (AService) applicationContext.getBean("AService");
		aService.test();


//
//		System.out.println("end");
	}
}
