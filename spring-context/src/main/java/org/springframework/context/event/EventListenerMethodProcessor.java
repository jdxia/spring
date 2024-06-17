/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.context.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * Registers {@link EventListener} methods as individual {@link ApplicationListener} instances.
 * Implements {@link BeanFactoryPostProcessor} (as of 5.1) primarily for early retrieval,
 * avoiding AOP checks for this processor bean and its {@link EventListenerFactory} delegates.
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 4.2
 * @see EventListenerFactory
 * @see DefaultEventListenerFactory
 */
// 它是一个SmartInitializingSingleton，所以他会在preInstantiateSingletons()的最后一步执行~~~
// 并且它还是实现了BeanFactoryPostProcessor，所以它需要实现方法`postProcessBeanFactory`
public class EventListenerMethodProcessor
		implements SmartInitializingSingleton, ApplicationContextAware, BeanFactoryPostProcessor {

	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private ConfigurableApplicationContext applicationContext;

	@Nullable
	private ConfigurableListableBeanFactory beanFactory;

	// 记录从容器中找到的所有 EventListenerFactory
	@Nullable
	private List<EventListenerFactory> eventListenerFactories;

	// 解析注解中的Condition的
	private final EventExpressionEvaluator evaluator = new EventExpressionEvaluator();

	// 缓存机制，记住那些根本任何方法上没有使用注解 @EventListener 的类，避免处理过程中二次处理
	private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		Assert.isTrue(applicationContext instanceof ConfigurableApplicationContext,
				"ApplicationContext does not implement ConfigurableApplicationContext");
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	// 这个方法是BeanFactoryPostProcessor的方法，它在容器的BeanFactory准备完成后，会执行此后置处理器
	// 它的作用：BeanFactory工厂准备好后，就去找所有的EventListenerFactory  然后保存起来
	// 此处：默认情况下Spring在准备Bean工厂的时候，会给我们注册一个`DefaultEventListenerFactory`，
	// 如果你使用了注解驱动的Spring事务如@EnableTransactionManagement，它就会额外再添加一个`TransactionalEventListenerFactory`
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;

		/**
		 *  获取或创建 EventListenerFactory 类型的 Bean
		 *  从容器中找到所有的  EventListenerFactory 组件
		 *  常见的一些 EventListenerFactory :
		 *  TransactionalEventListenerFactory -- 用于支持使用 @TransactionalEventListener 注解的事件监听器,
		 *  @TransactionalEventListener 是一种特殊的 @EventListener ，它定义的事件监听器应用于事务提交或者回滚的某些特殊时机，由 ProxyTransactionManagementConfiguration 注册到容器
		 *
		 * 	DefaultEventListenerFactory -- 系统缺省, 最低优先级，如果其他 EventListenerFactory 都不支持的时候使用
		 */
		Map<String, EventListenerFactory> beans = beanFactory.getBeansOfType(EventListenerFactory.class, false, false);
		List<EventListenerFactory> factories = new ArrayList<>(beans.values());
		// 会根据@Order进行排序
		AnnotationAwareOrderComparator.sort(factories);
		this.eventListenerFactories = factories;
	}


	@Override
	public void afterSingletonsInstantiated() {
		ConfigurableListableBeanFactory beanFactory = this.beanFactory;
		Assert.state(this.beanFactory != null, "No ConfigurableListableBeanFactory set");
		// 把bean工厂里面所有的bean都拿出来
		String[] beanNames = beanFactory.getBeanNamesForType(Object.class);
		for (String beanName : beanNames) {
			// 遍历每个bean组件，检测其中`@EventListener`注解方法，生成和注册`ApplicationListener`实例

			// 不处理Scope作用域代理的类。 和@Scope类似相关
			if (!ScopedProxyUtils.isScopedTarget(beanName)) {
				// 拿到当前bean对象的类型
				Class<?> type = null;
				try {
					type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
				}
				catch (Throwable ex) {
					// An unresolvable bean type, probably from a lazy bean - let's ignore it.
					if (logger.isDebugEnabled()) {
						logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
					}
				}
				if (type != null) {
					// 对专门的作用域对象进行兼容~~~~（绝大部分都用不着）
					if (ScopedObject.class.isAssignableFrom(type)) {
						try {
							Class<?> targetClass = AutoProxyUtils.determineTargetClass(
									beanFactory, ScopedProxyUtils.getTargetBeanName(beanName));
							if (targetClass != null) {
								type = targetClass;
							}
						}
						catch (Throwable ex) {
							// An invalid scoped proxy arrangement - let's ignore it.
							if (logger.isDebugEnabled()) {
								logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
							}
						}
					}
					try {
						/**
						 * 注意这一行，针对一个bean的真正的`@EventListener`注解方法检测，
						 * `ApplicationListener`实例生成注册发生在这里
						 * 核心
						 */
						processBean(beanName, type);
					}
					catch (Throwable ex) {
						throw new BeanInitializationException("Failed to process @EventListener " +
								"annotation on bean with name '" + beanName + "'", ex);
					}
				}
			}
		}
	}

	// 该方法拿到某个bean的名称和它的目标类，再这个范围上检测@EventListener注解方法，生成和注册 ApplicationListenerMethodAdapter 实例
	private void processBean(final String beanName, final Class<?> targetType) {
		// 缓存下没有被注解过的Class，这样再次解析此Class就不用再处理了
		// 这是为了加速父子容器的情况  做的特别优化
		if (!this.nonAnnotatedClasses.contains(targetType) &&
				AnnotationUtils.isCandidateClass(targetType, EventListener.class) &&
				!isSpringContainerClass(targetType)) {

			// 找到所有加了 @EventListener 注解的方法
			Map<Method, EventListener> annotatedMethods = null;
			try {
				// 检测当前类targetType上使用了注解@EventListener的方法, 就是找到这个Class里面被标注此注解的Methods们
				annotatedMethods = MethodIntrospector.selectMethods(targetType,
						(MethodIntrospector.MetadataLookup<EventListener>) method ->
								AnnotatedElementUtils.findMergedAnnotation(method, EventListener.class));
			}
			catch (Throwable ex) {
				// An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
				}
			}

			// 若一个都没找到，那就标注此类没有标注注解，那就标记一下此类  然后拉到算了  输出一句trace日志足矣
			if (CollectionUtils.isEmpty(annotatedMethods)) {
				// 如果当前类targetType中没有任何使用了注解@EventListener的方法，则将该类保存到缓存nonAnnotatedClasses，从而
				// 避免当前处理方法重入该类，避免二次处理
				this.nonAnnotatedClasses.add(targetType);
				if (logger.isTraceEnabled()) {
					logger.trace("No @EventListener annotations found on bean class: " + targetType.getName());
				}
			}
			//若存在对应的@EventListener标注的方法，那就走这里
			// 最终此Method是交给`EventListenerFactory`这个工厂，适配成一个ApplicationListener的
			// 适配类为ApplicationListenerMethodAdapter，它也是个ApplicationListener
			else {
				// Non-empty set of methods
				// 如果当前类targetType中有些方法使用了注解@EventListener，那么根据方法上的信息对应的创建和注册ApplicationListener实例
				ConfigurableApplicationContext context = this.applicationContext;
				Assert.state(context != null, "No ApplicationContext set");
				// 此处使用了this.eventListenerFactories,这些EventListenerFactory是在该类postProcessBeanFactory方法调用时被记录的
				List<EventListenerFactory> factories = this.eventListenerFactories;
				Assert.state(factories != null, "EventListenerFactory List not initialized");

				// 处理这些带有@EventListener注解的方法们
				for (Method method : annotatedMethods.keySet()) {

					// 这里面注意：拿到每个EventListenerFactory (一般情况下只有DefaultEventListenerFactory,但是若是注解驱动的事务还会有它：TransactionalEventListenerFactory)
					// factories 有 DefaultEventListenerFactory 和 TransactionalEventListenerFactory 事务事件
					for (EventListenerFactory factory : factories) {
						// 利用EventListenerFactory来对加了 @EventListener注解的方法生成ApplicationListener对象
						// DefaultEventListenerFactory 最后执行,如果是事务事件 这个Factory会被忽略, 原因是下面的break
						if (factory.supportsMethod(method)) {

							// 简单的说，就是把这个方法弄成一个可以执行的方法（主要和访问权限有关）
							// 这里注意：若你是JDK的代理类，请不要在实现类里书写@EventListener注解的监听器，否则会报错的。（CGLIB代理的没关系）
							Method methodToUse = AopUtils.selectInvocableMethod(method, context.getType(beanName));
							// 把这个方法构造成一个ApplicationListener
							// 如果当前EventListenerFactory支持处理该@EventListener注解的方法，则使用它创建 ApplicationListenerMethodAdapter
							ApplicationListener<?> applicationListener =
									factory.createApplicationListener(beanName, targetType, methodToUse);
							if (applicationListener instanceof ApplicationListenerMethodAdapter) {
								((ApplicationListenerMethodAdapter) applicationListener).init(context, this.evaluator);
							}
							// 将创建的ApplicationListener加入到容器中
							context.addApplicationListener(applicationListener);

							// 这个break意思是：只要有一个工厂处理了这个方法，接下来的工厂就不需要再处理此方法了~~~~（所以工厂之间的排序也比较重要）
							// DefaultEventListenerFactory 是最后执行
							break;
						}
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(annotatedMethods.size() + " @EventListener methods processed on bean '" +
							beanName + "': " + annotatedMethods);
				}
			}
		}
	}

	/**
	 * Determine whether the given class is an {@code org.springframework}
	 * bean class that is not annotated as a user or test {@link Component}...
	 * which indicates that there is no {@link EventListener} to be found there.
	 * @since 5.1
	 */
	private static boolean isSpringContainerClass(Class<?> clazz) {
		return (clazz.getName().startsWith("org.springframework.") &&
				!AnnotatedElementUtils.isAnnotated(ClassUtils.getUserClass(clazz), Component.class));
	}

}
