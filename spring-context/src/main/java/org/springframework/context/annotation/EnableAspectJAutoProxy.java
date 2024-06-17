/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables support for handling components marked with AspectJ's {@code @Aspect} annotation,
 * similar to functionality found in Spring's {@code <aop:aspectj-autoproxy>} XML element.
 * To be used on @{@link Configuration} classes as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAspectJAutoProxy
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public FooService fooService() {
 *         return new FooService();
 *     }
 *
 *     &#064;Bean
 *     public MyAspect myAspect() {
 *         return new MyAspect();
 *     }
 * }</pre>
 *
 * Where {@code FooService} is a typical POJO component and {@code MyAspect} is an
 * {@code @Aspect}-style aspect:
 *
 * <pre class="code">
 * public class FooService {
 *
 *     // various methods
 * }</pre>
 *
 * <pre class="code">
 * &#064;Aspect
 * public class MyAspect {
 *
 *     &#064;Before("execution(* FooService+.*(..))")
 *     public void advice() {
 *         // advise FooService methods as appropriate
 *     }
 * }</pre>
 *
 * In the scenario above, {@code @EnableAspectJAutoProxy} ensures that {@code MyAspect}
 * will be properly processed and that {@code FooService} will be proxied mixing in the
 * advice that it contributes.
 *
 * <p>Users can control the type of proxy that gets created for {@code FooService} using
 * the {@link #proxyTargetClass()} attribute. The following enables CGLIB-style 'subclass'
 * proxies as opposed to the default interface-based JDK proxy approach.
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAspectJAutoProxy(proxyTargetClass=true)
 * public class AppConfig {
 *     // ...
 * }</pre>
 *
 * <p>Note that {@code @Aspect} beans may be component-scanned like any other.
 * Simply mark the aspect with both {@code @Aspect} and {@code @Component}:
 *
 * <pre class="code">
 * package com.foo;
 *
 * &#064;Component
 * public class FooService { ... }
 *
 * &#064;Aspect
 * &#064;Component
 * public class MyAspect { ... }</pre>
 *
 * Then use the @{@link ComponentScan} annotation to pick both up:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan("com.foo")
 * &#064;EnableAspectJAutoProxy
 * public class AppConfig {
 *
 *     // no explicit &#064Bean definitions required
 * }</pre>
 *
 * <b>Note: {@code @EnableAspectJAutoProxy} applies to its local application context only,
 * allowing for selective proxying of beans at different levels.</b> Please redeclare
 * {@code @EnableAspectJAutoProxy} in each individual context, e.g. the common root web
 * application context and any separate {@code DispatcherServlet} application contexts,
 * if you need to apply its behavior at multiple levels.
 *
 * <p>This feature requires the presence of {@code aspectjweaver} on the classpath.
 * While that dependency is optional for {@code spring-aop} in general, it is required
 * for {@code @EnableAspectJAutoProxy} and its underlying facilities.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see org.aspectj.lang.annotation.Aspect
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 *  @Import 注解：可以引入一个类，将这个类注入到Spring IOC容器中被当前Spring管理,
 *  AspectJAutoProxyRegistrar 这个类注册了一个 AnnotationAwareAspectJAutoProxyCreator 类,
 *  AnnotationAwareAspectJAutoProxyCreator 是一个 BeanPostProcessor，用于处理@Aspect注解的类,
 *  AnnotationAwareAspectJAutoProxyCreator 有个父抽象类是 AbstractAutoProxyCreator,
 *  AbstractAutoProxyCreator 有2个子类 DefaultAdvisorAutoProxyCreator 和 AnnotationAwareAspectJAutoProxyCreator
 */
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {
	/**
	 * 名词解释:
	 *
	 * AOP中重要的三个要素：Aspect、Pointcut、Advice
	 * 		意思是说：在Advice的时间、在Pointcut的位置，执行Aspect
	 *
	 * Advisor: 指定了在哪里（即在哪些方法上）以及在什么条件下执行这些动作
	 * 		Advisor是Advice和Pointcut的组合，Advice定义了在什么时候执行，Pointcut定义了在什么地方执行
	 *
	 * 通知（Advice）:
	 * 		前置通知（Before）：在目标方法执行之前执行的通知。 @Before 相当于 BeforeAdvice
	 * 		后置通知（After）：在目标方法执行之后执行的通知，无论方法执行是否成功。 @AfterReturning 相当于 AfterReturningAdvice
	 * 		返回通知（After-returning）：在目标方法成功执行之后执行的通知。 @After 不管是否异常，该通知都会执行
	 * 		异常通知（After-throwing）：在目标方法抛出异常后执行的通知。 @AfterThrowing 相当于 ThrowAdvice
	 * 		环绕通知（Around）：在被通知的方法调用之前和之后执行的通知，可以在通知中直接控制目标方法的调用。 @Around 相当于 MethodInterceptor
	 *
	 * @After 先执行，@AfterReturning 后执行
	 * @AfterReturning 它能拿到目标方法执行完的返回值，但是 @After 不行
	 * @After 它在 finally 里面，所以它不管怎么样都会执行（哪怕目标方法抛出异常），但是@AfterReturning 如果目标方法没有正常return（比如抛出异常了），它是不会执行的
	 * 	try{
	 *     try{
	 *         //@Before
	 *         method.invoke(..);
	 *     } finally {
	 *         //@After
	 *     }
	 *     //@AfterReturning
	 * } catch() {
	 *     //@AfterThrowing
	 * }
	 *
	 * 切点（Pointcut）: 定义了在哪些连接点上执行切面的通知（Advice），是对连接点的一种过滤规则
	 * 		@Pointcut("execution(* com.example.demo.service.SimpleService.performOperation(...))")
	 *
	 * 连接点（JoinPoint）: 程序执行的某个特定位置，如类初始化前、类初始化后、方法调用前、方法调用后、方法抛出异常后等
	 * 		比如 performOperation方法(看上面切点表达式)的调用就是一个连接点
	 *
	 * 切面（Aspect）: 可以将切面理解为一个观察者，它在幕后观察你的程序运行。当特定的事件（如方法调用）发生时，它会执行一些操作（如打印日志）
	 * 		 @Aspect public class LoggingAspect { 定义了一些aop的切点和具体执行逻辑 }
	 * 		 LoggingAspect 就是一个切面
	 *
	 * 织入 weaving:Spring采用动态织入，而aspectj采用静态织入
	 * 		织入是一个过程，是将切面应用到目标对象从而创建出AOP代理对象的过程，织入可以在编译期，类装载期，运行期进行。
	 *
	 * 代理Proxy: 一个类被AOP织入增强后，就产生一个结果代理类
	 *
	 * AOP底层实现:
	 * 		AOP分为静态AOP和动态AOP。静态AOP是指AspectJ实现的AOP，他是将切面代码直接编译到Java类文件中。
	 * 		动态AOP是指将切面代码进行动态织入实现的AOP。Spring的AOP为动态AOP，
	 * 		实现的技术为： JDK提供的动态代理技术 和 CGLIB(动态字节码增强技术)
	 *
	 *  引介增强：org.springframework.aop.IntroductionInterceptor，表示在目标类中添加一些新的方法和属性。
	 *  引介增强是一种比较特殊的增强类型，它不是在目标方法周围织入增强，而是为目标类创建新的方法和属性，所以引介增强的连接点是类级别的，而非方法级别的。通过引介增强，可以为目标类创建实现某接口的代理。
	 *  引介增强的配置与一般的配置有较大的区别：首先，需要指定引介增强所实现的接口；其次，由于只能通过为目标类创建子类的方式生成引介增强的代理，所以必须将proxyTargetClass设置为true。
	 *
	 */

	//proxyTargetClass属性，默认false，尝试采用JDK动态代理织入增强(如果当前类没有实现接口则还是会使用CGLIB)；如果设为true，则强制采用CGLIB动态代理织入增强
	/**
	 * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies. The default is {@code false}.
	 */
	boolean proxyTargetClass() default false;

	//通过aop框架暴露该代理对象，aopContext能够访问。为了解决类内部方法之间调用时无法增强的问题
	// true 才能使用 AopContext.currentProxy()
	/**
	 * Indicate that the proxy should be exposed by the AOP framework as a {@code ThreadLocal}
	 * for retrieval via the {@link org.springframework.aop.framework.AopContext} class.
	 * Off by default, i.e. no guarantees that {@code AopContext} access will work.
	 * @since 4.3.1
	 */
	boolean exposeProxy() default false;

}
