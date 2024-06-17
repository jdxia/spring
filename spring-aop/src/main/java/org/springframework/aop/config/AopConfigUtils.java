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

package org.springframework.aop.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Utility class for handling registration of AOP auto-proxy creators.
 *
 * <p>Only a single auto-proxy creator should be registered yet multiple concrete
 * implementations are available. This class provides a simple escalation protocol,
 * allowing a caller to request a particular auto-proxy creator and know that creator,
 * <i>or a more capable variant thereof</i>, will be registered as a post-processor.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see AopNamespaceUtils
 */
public abstract class AopConfigUtils {

	/**
	 * The bean name of the internally managed auto-proxy creator.
	 */
	public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
			"org.springframework.aop.config.internalAutoProxyCreator";

	// 保存候选的自动代理创建器集合。
	/**
	 * Stores the auto proxy creator classes in escalation order.
	 */
	private static final List<Class<?>> APC_PRIORITY_LIST = new ArrayList<>(3);

	static {
		// Set up the escalation list...
		/**
		 * 这里面三种都是 自动代理创建器，会根据情况选择一个自动代理创建器加载。
		 * 需要注意的是，自动代理创建器只能加载一种，若已经加载一种，则会根据优先级选择优先级高的重新加载
		 *
		 * 关于自动代理创建器优先级的问题, 所谓的优先级顺序实际上是在 APC_PRIORITY_LIST 集合的顺序
		 * aop 优先级最高
		 * InfrastructureAdvisorAutoProxyCreator < AspectJAwareAdvisorAutoProxyCreator < AnnotationAwareAspectJAutoProxyCreator
		 *
		 * Aop 的使用的是 AnnotationAwareAspectJAutoProxyCreator 自动代理创建器；事务使用的是InfrastructureAdvisorAutoProxyCreator自动代理创建器
		 * 自动代理创建器实现逻辑基本一致
		 * 最大的不同之处在于，Aop 重写了
		 * AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors 方法。而事务并没有重写这一部分。
		 * 所以事务调用的实际上是 AbstractAdvisorAutoProxyCreator#findCandidateAdvisors
		 *
		 * 相较于 Aop 的 ，事务的 InfrastructureAdvisorAutoProxyCreator，不仅没有添加新逻辑(关键逻辑)，还砍掉了动态生成Advisor 的逻辑
		 */
		// 事务使用
		APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
		APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);
		// Spring AOP 使用
		APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);
	}


	@Nullable
	public static BeanDefinition registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		return registerAutoProxyCreatorIfNecessary(registry, null);
	}

	@Nullable
	public static BeanDefinition registerAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		// 注册了 InfrastructureAdvisorAutoProxyCreator 类型的bean
		return registerOrEscalateApcAsRequired(InfrastructureAdvisorAutoProxyCreator.class, registry, source);
	}

	@Nullable
	public static BeanDefinition registerAspectJAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		return registerAspectJAutoProxyCreatorIfNecessary(registry, null);
	}

	@Nullable
	public static BeanDefinition registerAspectJAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		return registerOrEscalateApcAsRequired(AspectJAwareAdvisorAutoProxyCreator.class, registry, source);
	}

	@Nullable
	public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
		// 往下看
		return registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null);
	}

	@Nullable
	public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		// 注册了一个bean
		return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
	}

	// 所谓的优先级顺序实际上是在 APC_PRIORITY_LIST 集合的顺序
	public static void forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition definition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			// 给BeanDefinition 的 proxyTargetClass 属性赋值
			definition.getPropertyValues().add("proxyTargetClass", Boolean.TRUE);
		}
	}

	public static void forceAutoProxyCreatorToExposeProxy(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition definition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			// 设置 exposeProxy 属性
			definition.getPropertyValues().add("exposeProxy", Boolean.TRUE);
		}
	}

	/**
	 * spring aop的话, 这里的cls是 AnnotationAwareAspectJAutoProxyCreator.class
	 *
	 * 事务的话, 这里cls是 InfrastructureAdvisorAutoProxyCreator.class
	 */
	@Nullable
	private static BeanDefinition registerOrEscalateApcAsRequired(
			Class<?> cls, BeanDefinitionRegistry registry, @Nullable Object source) {
		/**
		 * 先看下这个类的静态代码块
		 */

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

		/**
		 * 如果有注册，则判断优先级，将优先级的高的保存
		 * 如果已经存在了自动代理创建器，且存在的自动代理创建器与现在的并不一致，那么需要根据优先级来判断到底要使用哪个
		 */
		if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
			BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
			if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
				// 重点, 往下看
				int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
				int requiredPriority = findPriorityForClass(cls);
				if (currentPriority < requiredPriority) {
					// 改变bean所对应的className 属性
					apcDefinition.setBeanClassName(cls.getName());
				}
			}
			// 如果已经存在自动代理创建器，并且与将要创建的一致，那么无需再次创建
			return null;
		}

		RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
		beanDefinition.setSource(source);
		beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		/**
		 * 核心代码, 注册到容器里面, 名字 AUTO_PROXY_CREATOR_BEAN_NAME 为 org.springframework.aop.config.internalAutoProxyCreator
		 * 但是value 不确定, spring aop value是一个, 事务又是另一个
		 *
		 * 这里之所以 beanName (AUTO_PROXY_CREATOR_BEAN_NAME) 和 bean的类型并不相同，是因为这个beanName 特指内部的自动代理创建器，但是自动创建代理器会对应多种不同的实现方式。
		 * 比如在默认的事务中，注入的bean类型却为InfrastructureAdvisorAutoProxyCreator，而AOP的实现却是 AnnotationAwareAspectJAutoProxyCreator。
		 * 之所以注册不同是因为实现功能上的区别。对于事务的自动代理创建器来说，他只需要扫描被事务注解修饰的方法，并进行代理。
		 * 而Spring Aop 则需要根据 @PointCut 注解 来动态的解析代理哪些方法
		 *
		 * 关于优先级的问题，我们可以看到APC_PRIORITY_LIST 集合的顺序，下标越大，优先级越高。因此可以得知优先级的顺序应该是
		 * InfrastructureAdvisorAutoProxyCreator < AspectJAwareAdvisorAutoProxyCreator < AnnotationAwareAspectJAutoProxyCreator
		 */
		registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
		return beanDefinition;
	}

	private static int findPriorityForClass(Class<?> clazz) {
		return APC_PRIORITY_LIST.indexOf(clazz);
	}

	//findPriorityForClass这个方法非常有意思：相当于找到index角标，然后
	//APC_PRIORITY_LIST 的内容是下面这几个  按照顺序排好的
	private static int findPriorityForClass(@Nullable String className) {
		for (int i = 0; i < APC_PRIORITY_LIST.size(); i++) {
			Class<?> clazz = APC_PRIORITY_LIST.get(i);
			if (clazz.getName().equals(className)) {
				return i;
			}
		}
		throw new IllegalArgumentException(
				"Class name [" + className + "] is not a known auto-proxy creator class");
	}

}
