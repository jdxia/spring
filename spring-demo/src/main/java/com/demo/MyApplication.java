package com.demo;

import com.demo.factory.Order;
import com.demo.service.AService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

import java.io.IOException;

public class MyApplication {

	public static void main(String[] args) throws IOException {

		System.out.println("=====================> debug start");

//		testBean();

		testFactoryBean();

		System.out.println("=====================> debug stop");
	}


	private static void testBean() {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(MyConfig.class);
		applicationContext.setAllowCircularReferences(true);
		applicationContext.refresh();

		AService aService = (AService) applicationContext.getBean("AService");
		aService.test();
	}

	private static void testFactoryBean() {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(MyConfig.class);
		applicationContext.setAllowCircularReferences(true);
		applicationContext.refresh();

		// &orderFactoryBean  --> orderFactoryBean  --> orderFactoryBean对象 -->有&就获取 --> orderFactoryBean对象
		System.out.println("factoryBean对象: " + applicationContext.getBean("&orderFactoryBean"));

		// &orderFactoryBean  --> orderFactoryBean  --> orderFactoryBean对象 -->没有有&就调用 --> getObject --> Order对象
		Order order = (Order) applicationContext.getBean("orderFactoryBean", Order.class);
		System.out.println("order打印: " + order);
	}




}
