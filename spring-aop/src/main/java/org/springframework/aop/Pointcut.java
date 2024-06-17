/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.aop;

/**
 * Core Spring pointcut abstraction.
 *
 * <p>A pointcut is composed of a {@link ClassFilter} and a {@link MethodMatcher}.
 * Both these basic terms and a Pointcut itself can be combined to build up combinations
 * (e.g. through {@link org.springframework.aop.support.ComposablePointcut}).
 *
 * @author Rod Johnson
 * @see ClassFilter
 * @see MethodMatcher
 * @see org.springframework.aop.support.Pointcuts
 * @see org.springframework.aop.support.ClassFilters
 * @see org.springframework.aop.support.MethodMatchers
 */
public interface Pointcut {
	/**
	 * Pointcut作为Spring AOP最顶级的抽象，主要负责对系统相应 JoinPoint 的捕获，
	 * 如果把 JoinPoint 比做数据，那么Pointcut就是查询条件，一个Pointcut可以对应多个 JoinPoint。
	 * ClassFilter和MethodMatcher分别限定在不同级别上对于 JoinPoint 的匹配，
	 * ClassFilter是类级别，MethodMatcher是方法级别
	 *
	 * 切点类型：Spring提供了六种类型切点
	 * 静态方法切点：org.springframework.aop.support.StaticMethodMatcherPointcut 是静态方法切点的抽象基类，默认情况下它匹配所有的类。（NameMatchMethodPointcut提供简单字符串匹配方法签名，AbstractRegexpMethodPointcut 使用正则表达式匹配方法签名。） 它不考虑方法入参个数、类型匹配
	 * 动态方法切点：org.springframework.aop.support.DynamicMethodMatcherPointcut 是动态方法的抽象基类，默认情况下它匹配所有的类 它会考虑方法入参个数、类型匹配
	 * 注解切点：org.springframework.aop.support.annotation.AnnotationMatchingPointcut 实现类表示注解切点。使用AnnotationMatchingPointcut支持在Bean中直接通过JDK 5.0注解标签定义切点
	 * 表达式切点：org.springframework.aop.support.ExpressionPointcut 接口主要是为了支持 AspectJ 切点表达式语法而定义的接口。 这个是最强大的，Spring支持11种切点表达式
	 * 流程切点：org.springframework.aop.support.ControlFlowPointcut 实现类表示控制流程切点。ControlFlowPointcut 是一种特殊的切点，它根据程序执行堆栈的信息查看目标方法是否由某一个方法直接或间接调用，以此判断是否为匹配的连接点。
	 * 复合切点：org.springframework.aop.support.ComposablePointcut 实现类是为创建多个切点而提供的方便操作类。它所有的方法都返回 ComposablePointcut 类。
	 *
	 *
	 */

	/**
	 * Return the ClassFilter for this pointcut.
	 * @return the ClassFilter (never {@code null})
	 */
	ClassFilter getClassFilter();

	/**
	 * Return the MethodMatcher for this pointcut.
	 * @return the MethodMatcher (never {@code null})
	 */
	MethodMatcher getMethodMatcher();


	/**
	 * Canonical Pointcut instance that always matches.
	 */
	Pointcut TRUE = TruePointcut.INSTANCE;

}
