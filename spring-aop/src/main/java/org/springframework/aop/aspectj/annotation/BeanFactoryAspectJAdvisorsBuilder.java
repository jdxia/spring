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

package org.springframework.aop.aspectj.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Helper for retrieving @AspectJ beans from a BeanFactory and building
 * Spring Advisors based on them, for use with auto-proxying.
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AnnotationAwareAspectJAutoProxyCreator
 */
public class BeanFactoryAspectJAdvisorsBuilder {

	private final ListableBeanFactory beanFactory;

	private final AspectJAdvisorFactory advisorFactory;

	@Nullable
	private volatile List<String> aspectBeanNames;

	private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

	private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();


	/**
	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {
		this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
	}

	/**
	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 * @param advisorFactory the AspectJAdvisorFactory to build each Advisor with
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		Assert.notNull(advisorFactory, "AspectJAdvisorFactory must not be null");
		this.beanFactory = beanFactory;
		this.advisorFactory = advisorFactory;
	}


	/**
	 * Look for AspectJ-annotated aspect beans in the current bean factory,
	 * and return to a list of Spring AOP Advisors representing them.
	 * <p>Creates a Spring Advisor for each AspectJ advice method.
	 * @return the list of {@link org.springframework.aop.Advisor} beans
	 * @see #isEligibleBean
	 */
	public List<Advisor> buildAspectJAdvisors() {
		// aspectBeanNames 是用来缓存BeanFactory中所存在的切面beanName的, 第一次为null, 后面就不为null了
		List<String> aspectNames = this.aspectBeanNames;

		// 如果为空表示尚未缓存，进行缓存解析。这里用了DLC 方式来进行判断
		if (aspectNames == null) {
			synchronized (this) {
				aspectNames = this.aspectBeanNames;
				if (aspectNames == null) {
					// 保存解析处理出来的Advisor对象
					List<Advisor> advisors = new ArrayList<>();
					aspectNames = new ArrayList<>();
					// 把所有的beanNames 拿来遍历, 判断某个bean的类型是否是Aspect
					String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
							this.beanFactory, Object.class, true, false);
					// 遍历beanname, 找出对应的增强方法
					for (String beanName : beanNames) {
						// 不合法的bean略过，由子类定义规则，默认true
						if (!isEligibleBean(beanName)) {
							continue;
						}
						// 注释 ：我们必须小心，不要急于实例化bean，因为在这种情况下，它们将由Spring容器缓存，但不会被编织。
						// We must be careful not to instantiate beans eagerly as in this case they
						// would be cached by the Spring container but would not have been weaved.
						// 获取对应 bean 的类型
						Class<?> beanType = this.beanFactory.getType(beanName);
						if (beanType == null) {
							continue;
						}
						// 如果bean 被 @AspectJ 注解修饰 且不是Ajc 编译, 则进一步处理
						if (this.advisorFactory.isAspect(beanType)) {
							// 切面类则加入到缓存中
							aspectNames.add(beanName);
							// 封装成AspectMetadata
							AspectMetadata amd = new AspectMetadata(beanType, beanName);

							// aspect 存在 SINGLETON、PERTHIS、PERTARGET、PERCFLOW、PERCFLOWBELOW、PERTYPEWITHIN模式。默认为SINGLETON 。
							if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
								// 把切面bean封装成一个工厂
								MetadataAwareAspectInstanceFactory factory =
										new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
								// 利用 BeanFactoryAspectInstanceFactory 来解析 Aspect 类
								// 一个切面bean里面可能有很多 Advisor
								// 解析标记AspectJ注解中的增强方法，也就是被 @Before、@Around 等注解修饰的方法，并将其封装成 Advisor
								// 核心
								List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
								// 加入到缓存中
								if (this.beanFactory.isSingleton(beanName)) {
									// 缓存切面所对应的所有Advisor对象
									this.advisorsCache.put(beanName, classAdvisors);
								}
								else {
									this.aspectFactoryCache.put(beanName, factory);
								}
								// 利用 PrototypeAspectInstanceFactory 来解析 Aspect 类
								// PrototypeAspectInstanceFactory 的父类 是 BeanFactoryAspectInstanceFactory
								// 这2个factory区别在于是PrototypeAspectInstanceFactory的构造方法中会判断切面bean
								// 所以主要就是 BeanFactoryAspectInstanceFactory来负责生成切面实例对象
								advisors.addAll(classAdvisors);
							}
							else {
								// Per target or per this.
								// 如果当前Bean是单例，但是 Aspect 不是单例则抛出异常
								if (this.beanFactory.isSingleton(beanName)) {
									throw new IllegalArgumentException("Bean with name '" + beanName +
											"' is a singleton, but aspect instantiation model is not singleton");
								}
								MetadataAwareAspectInstanceFactory factory =
										new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
								this.aspectFactoryCache.put(beanName, factory);
								advisors.addAll(this.advisorFactory.getAdvisors(factory));
							}
						}
					}
					this.aspectBeanNames = aspectNames;
					return advisors;
				}
			}
		}

		// aspectNames不为空，说明之前已经解析过了，不需要重复解析，直接获取缓存中的数据
		if (aspectNames.isEmpty()) {
			return Collections.emptyList();
		}
		// 将所有的增强方法保存到缓存中
		List<Advisor> advisors = new ArrayList<>();
		for (String aspectName : aspectNames) {
			List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
			if (cachedAdvisors != null) {
				advisors.addAll(cachedAdvisors);
			}
			else {
				MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
				advisors.addAll(this.advisorFactory.getAdvisors(factory));
			}
		}
		return advisors;
	}

	/**
	 * Return whether the aspect bean with the given name is eligible.
	 * @param beanName the name of the aspect bean
	 * @return whether the bean is eligible
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}
