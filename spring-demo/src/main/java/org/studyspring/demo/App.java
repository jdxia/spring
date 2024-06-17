package org.studyspring.demo;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.*;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.cglib.proxy.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.studyspring.demo.bean.aop.A;
import org.studyspring.demo.bean.aop.MyAroundAdvice;
import org.studyspring.demo.bean.event.genericEvent.RegisterService;
import org.studyspring.demo.bean.event.normalEvent.TestEventListener;
import org.studyspring.demo.bean.transaction.MyUserService;
import org.studyspring.demo.config.AppConfig;
import org.studyspring.demo.config.AsyncConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class App {


	// 源码核心: AbstractApplicationContext类refresh⽅法
	public static void main(String[] args) throws Exception {
		System.out.println("=====================> debug start");

//		aopTest();

		eventTest();

//		transactionTest();

//		enhancerTest();
//		proxyFactoryTestAdvice();

//		xmlTest();

		System.out.println("=====================> debug stop");

	}

	private static void aopTest() throws Exception {
		/**
		 * 想知道这个的原理, 可以看
		 * {@link JdkDynamicAopProxy#invoke(Object, Method, Object[])} 里面的 oldProxy = AopContext.setCurrentProxy(proxy);
		 * 也有还原的
		 */
//		 AopContext.currentProxy();

		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class, AsyncConfig.class);
		A aBean = ac.getBean(A.class);
		aBean.test();
	}

	private static void eventTest() throws Exception {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class, AsyncConfig.class);


		RegisterService registerService = ac.getBean(RegisterService.class);

//		SimpleApplicationEventMulticaster multicaster =
//				(SimpleApplicationEventMulticaster) ac.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME);
//
//		// 获取所有监听器
//		ac.getBeansOfType(ApplicationListener.class)
//				.values()
//				.forEach(listener -> {
//					// 找到特定的监听器并移除
//					if (listener instanceof TestEventListener) {
//						multicaster.removeApplicationListener(listener);
//						multicaster.removeApplicationListenerBean("testEventListener");
//					}
//				});
//
//		System.out.println(multicaster);

		registerService.testEvent("testEvent");


		//		registerService.register("xjd");

	}

	private static void transactionTest() {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class, AsyncConfig.class);
		MyUserService myUserService = ac.getBean(MyUserService.class);
		myUserService.test1();
	}

	private static void xmlTest() {

		ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
		// 2.向容器要User对象
//		User u = (User) ac.getBean("user");
//		// 打印User对象
//		System.out.println(u);

		System.out.println("=====================================");

	}

	public static Object getOriginBean(Object proxy) throws Exception {

		// spring.objenesis.ignore 设置为true, 默认生成的代理对象的属性就有值了

		// 判断是否是代理对象
		if (AopUtils.isAopProxy(proxy)) {
			//cglib 代理
			if (AopUtils.isCglibProxy(proxy)) {
				//通过暴力反射拿到代理对象的拦截器属性，从拦截器获取目标对象
				Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
				h.setAccessible(true);
				Object dynamicAdvisedInterceptor = h.get(proxy);

				Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
				advised.setAccessible(true);
				Object target = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
				//返回目标对象
				return target;
			}
			//jdk代理
			if (AopUtils.isJdkDynamicProxy(proxy)) {
				//通过暴力反射拿到代理对象的拦截器属性，从拦截器获取目标对象
				Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
				h.setAccessible(true);
				AopProxy aopProxy = (AopProxy) h.get(proxy);

				Field advised = aopProxy.getClass().getDeclaredField("advised");
				advised.setAccessible(true);

				Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();

				return target;
			}
		}
		return null;

	}


	private static void enhancerTest() throws Exception {

		A a = new A();

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(A.class);
		enhancer.setCallbacks(new Callback[]{
				// 第一个拦截器
				new MethodInterceptor() {
					// 这个o是代理对象
					@Override
					public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
						System.out.println("before...");
						// Object result = methodProxy.invoke(a, objects);

						// 执行被代理的父类的方法, 那这个就是a, 不能造成代理循环
						Object result = methodProxy.invokeSuper(o, objects);
						System.out.println("after...");
						return result;
					}
				}, NoOp.INSTANCE});

		enhancer.setCallbackFilter(new CallbackFilter() {
			@Override
			public int accept(Method method) {
				// 如果方法名是test2, 则使用第一个拦截器, 否则使用第二个拦截器
				if (method.getName().equals("test2")) {
					return 0;
				}
				return 1;
			}
		});

		A aProxy = (A) enhancer.create();
		aProxy.test2();

	}

	public static void proxyFactoryTestAdvice() {
		A a = new A();

		// 会自动选择cgllib还是jdk动态代理
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(a);
		// 设置了这个接口, 可能就会走jdk的动态代理
		proxyFactory.setInterfaces(A.class.getInterfaces());

		//设置这2个可能会走cglib
		proxyFactory.setOptimize(true);
		proxyFactory.setProxyTargetClass(true);  // 这个属性代表强制使用cglib, 注解那边也有这个属性
		proxyFactory.setExposeProxy(true);	// 把代理对象放到threadLocal里面

		/**
		 *  可以添加多个, 按顺序执行
		 * 	MethodBeforeAdvice, AfterReturningAdvice 等等, 这都会封装成 MethodInterceptor, 然后组装成一个链路
		 * 	MethodInterceptor 核心
		 */
//		proxyFactory.addAdvice(new MyAroundAdvice());

		// Advisor: 指定了在哪里（即在哪些方法上）以及在什么条件下执行这些动作
		// Advisor是Advice和Pointcut的组合，Advice定义了在什么时候执行，Pointcut定义了在什么地方执行
		proxyFactory.addAdvisor(new PointcutAdvisor() {
			@Override
			public Pointcut getPointcut() {
				return new StaticMethodMatcherPointcut() {
					@Override
					public boolean matches(Method method, Class<?> targetClass) {
						return method.getName().equals("test2");
					}
				};
			}

			@Override
			public Advice getAdvice() {
				return new MyAroundAdvice();
			}

			@Override
			public boolean isPerInstance() {
				return false;
			}
		});

		A aProxy = (A) proxyFactory.getProxy();
		aProxy.test2();

	}

}


