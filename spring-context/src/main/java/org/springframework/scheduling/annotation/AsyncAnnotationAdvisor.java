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

package org.springframework.scheduling.annotation;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Pointcut;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.function.SingletonSupplier;

/**
 * Advisor that activates asynchronous method execution through the {@link Async}
 * annotation. This annotation can be used at the method and type level in
 * implementation classes as well as in service interfaces.
 *
 * <p>This advisor detects the EJB 3.1 {@code javax.ejb.Asynchronous}
 * annotation as well, treating it exactly like Spring's own {@code Async}.
 * Furthermore, a custom async annotation type may get specified through the
 * {@link #setAsyncAnnotationType "asyncAnnotationType"} property.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see Async
 * @see AnnotationAsyncExecutionInterceptor
 */
@SuppressWarnings("serial")
public class AsyncAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

	private Advice advice;

	private Pointcut pointcut;


	/**
	 * Create a new {@code AsyncAnnotationAdvisor} for bean-style configuration.
	 */
	public AsyncAnnotationAdvisor() {
		this((Supplier<Executor>) null, (Supplier<AsyncUncaughtExceptionHandler>) null);
	}

	/**
	 * Create a new {@code AsyncAnnotationAdvisor} for the given task executor.
	 * @param executor the task executor to use for asynchronous methods
	 * (can be {@code null} to trigger default executor resolution)
	 * @param exceptionHandler the {@link AsyncUncaughtExceptionHandler} to use to
	 * handle unexpected exception thrown by asynchronous method executions
	 * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)
	 */
	@SuppressWarnings("unchecked")
	public AsyncAnnotationAdvisor(
			@Nullable Executor executor, @Nullable AsyncUncaughtExceptionHandler exceptionHandler) {

		this(SingletonSupplier.ofNullable(executor), SingletonSupplier.ofNullable(exceptionHandler));
	}

	/**
	 * Create a new {@code AsyncAnnotationAdvisor} for the given task executor.
	 * @param executor the task executor to use for asynchronous methods
	 * (can be {@code null} to trigger default executor resolution)
	 * @param exceptionHandler the {@link AsyncUncaughtExceptionHandler} to use to
	 * handle unexpected exception thrown by asynchronous method executions
	 * @since 5.1
	 * @see AnnotationAsyncExecutionInterceptor#getDefaultExecutor(BeanFactory)
	 */
	@SuppressWarnings("unchecked")
	public AsyncAnnotationAdvisor(
			@Nullable Supplier<Executor> executor, @Nullable Supplier<AsyncUncaughtExceptionHandler> exceptionHandler) {

		// 添加异步注解，默认为@Async和EJB 3.1规范下的@Asynchronous
		Set<Class<? extends Annotation>> asyncAnnotationTypes = new LinkedHashSet<>(2);
		asyncAnnotationTypes.add(Async.class);
		try {
			asyncAnnotationTypes.add((Class<? extends Annotation>)
					ClassUtils.forName("javax.ejb.Asynchronous", AsyncAnnotationAdvisor.class.getClassLoader()));
		}
		catch (ClassNotFoundException ex) {
			// If EJB 3.1 API not present, simply ignore.
		}
		// 通知, 重点
		this.advice = buildAdvice(executor, exceptionHandler);
		// 切点
		this.pointcut = buildPointcut(asyncAnnotationTypes);
	}


	/**
	 * Set the 'async' annotation type.
	 * <p>The default async annotation type is the {@link Async} annotation, as well
	 * as the EJB 3.1 {@code javax.ejb.Asynchronous} annotation (if present).
	 * <p>This setter property exists so that developers can provide their own
	 * (non-Spring-specific) annotation type to indicate that a method is to
	 * be executed asynchronously.
	 * @param asyncAnnotationType the desired annotation type
	 */
	public void setAsyncAnnotationType(Class<? extends Annotation> asyncAnnotationType) {
		Assert.notNull(asyncAnnotationType, "'asyncAnnotationType' must not be null");
		Set<Class<? extends Annotation>> asyncAnnotationTypes = new HashSet<>();
		asyncAnnotationTypes.add(asyncAnnotationType);
		this.pointcut = buildPointcut(asyncAnnotationTypes);
	}

	/**
	 * Set the {@code BeanFactory} to be used when looking up executors by qualifier.
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (this.advice instanceof BeanFactoryAware) {
			((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
		}
	}


	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}


	protected Advice buildAdvice(
			@Nullable Supplier<Executor> executor, @Nullable Supplier<AsyncUncaughtExceptionHandler> exceptionHandler) {

		/**
		 * 初始化了一个拦截器
		 * AnnotationAsyncExecutionInterceptor 这个类很重要
		 * 如果对AOP比较熟悉的，就知道里面它父类 AsyncExecutionInterceptor 有个 invoke 方法, 这是@Async的核心
		 */
		AnnotationAsyncExecutionInterceptor interceptor = new AnnotationAsyncExecutionInterceptor(null);
		interceptor.configure(executor, exceptionHandler);
		return interceptor;
	}

	/**
	 * Calculate a pointcut for the given async annotation types, if any.
	 * @param asyncAnnotationTypes the async annotation types to introspect
	 * @return the applicable Pointcut object, or {@code null} if none
	 */
	protected Pointcut buildPointcut(Set<Class<? extends Annotation>> asyncAnnotationTypes) {
		ComposablePointcut result = null;
		for (Class<? extends Annotation> asyncAnnotationType : asyncAnnotationTypes) {
			/**
			 * Pointcut作为Spring AOP最顶级的抽象，主要负责对系统相应 JoinPoint 的捕获，
			 * 如果把JoinPoint比做数据，那么Pointcut就是查询条件，一个Pointcut可以对应多个 JoinPoint
			 * ClassFilter和MethodMatcher分别限定在不同级别上对于Joinpoint的匹配，ClassFilter是类级别，MethodMatcher是方法级别，
			 * AnnotationMatchingPointcut实现了Pointcut
			 */

			// 类级别注解的捕获
			Pointcut cpc = new AnnotationMatchingPointcut(asyncAnnotationType, true);
			// 方法级别注解的捕获
			Pointcut mpc = new AnnotationMatchingPointcut(null, asyncAnnotationType, true);
			// 存在多个注解的情况下，会合并起来，union方法内部做了合并
			if (result == null) {
				result = new ComposablePointcut(cpc);
			}
			else {
				result.union(cpc);
			}
			result = result.union(mpc);
		}
		return (result != null ? result : Pointcut.TRUE);
	}

}
