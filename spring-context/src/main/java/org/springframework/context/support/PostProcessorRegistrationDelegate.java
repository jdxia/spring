/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			// 这两个 list 主要用来分别收集 BeanFactoryPostProcessor 和 BeanDefinitionRegistryPostProcessor
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			/**
			 * beanFactoryPostProcessors 一般情况下是空的, 除非手动添加了
			 * ioc.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor()); 这种方式注册的, 这边是可以拿到的
			 * beanFactoryPostProcessors包含了 BeanDefinitionRegistryPostProcessor 和 BeanFactoryPostProcessor, BeanDefinitionRegistryPostProcessor是BeanFactoryPostProcessor子接口
			 * 对于BeanDefinitionRegistryPostProcessor, 会先执行自己的 postProcessBeanDefinitionRegistry 方法, 然后再执行 postProcessBeanFactory 方法
			 */
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					// 如果不是BeanDefinitionRegistryPostProcessor的实例
					// 则是BeanFactoryPostProcessor的实例，存放在List中，后面会进行回调
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			// 定义了一个集合来存放当前需要执行的BeanDefinitionRegistryPostProcessor
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// 执行扫描出来的BeanDefinitionRegistryPostProcessor

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			/**
			 * 首先执行实现了 PriorityOrdered 的BeanDefinitionRegistryPostProcessor, 那就是 ConfigurationClassPostProcessor
			 * 这里只能获取到Spring内部注册的BeanDefinitionRegistryPostProcessor，因为到这里spring还没有去扫描Bean，获取不到我们通过@Component标志的自定义的BeanDefinitionRegistryPostProcessor
			 * 一般默认情况下,这里只有一个beanName, org.springframework.context.annotation.internalConfigurationAnnotationProcessor
			 * 对应的BeanClass：ConfigurationClassPostProcessor
			 */
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			// 升序排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			// registryProcessors存放的是BeanDefinitionRegistryPostProcessor
			registryProcessors.addAll(currentRegistryProcessors);
			/**
			 * 重点：
			 * 执行 BeanDefinitionRegistryPostProcessor，一般默认情况下，只有 ConfigurationClassPostProcessor
			 * ConfigurationClassPostProcessor，这里先认为它执行完扫描，并且注册BeanDefinition
			 *
			 * 核心会进行扫描
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			// 清空临时变量，后面再使用
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			/**
			 * 第二步执行实现了Ordered的BeanDefinitionRegistryPostProcessor
			 *  这里为什么要再一次从beanFactory中获取所有的BeanDefinitionRegistryPostProcessor，是因为上面的操作有可能注册了
			 *  新的BeanDefinitionRegistryPostProcessor，所以再获取一次
			 */
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					// 保存调用过的beanName
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			/**
			 * 上面两步的套路是一模一样的，唯一不一样的地方是
			 * 第一步执行的是实现了PriorityOrdered的BeanDefinitionRegistryPostProcessor，
			 * 第二步是执行的是实现了Ordered的 BeanDefinitionRegistryPostProcessor
			 */

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			/**
			 * 最后一步，执行没有实现 PriorityOrdered 或者Ordered的BeanDefinitionRegistryPostProcessor
			 * 比较不一样的是，这里有个while循环，是因为在实现BeanDefinitionRegistryPostProcessor的方法的过程中有可能会注册新的BeanDefinitionRegistryPostProcessor，
			 * 所以需要处理，直到不会出现新的BeanDefinitionRegistryPostProcessor为止
			 */
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						/**
						 * 发现还有未处理过的 BeanDefinitionRegistryPostProcessor，按照套路放进list中
						 * reiterate 标记为true，后面还要再执行一次这个循环，因为执行新的 BeanDefinitionRegistryPostProcessor 有可能会注册新的 BeanDefinitionRegistryPostProcessor
						 */
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			/**
			 * 方法开头前几行分类的两个list就是在这里调用
			 * BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor
			 * 刚刚执行了 BeanDefinitionRegistryPostProcessor 的方法, 现在要执行父类 BeanFactoryPostProcessor 的方法
			 */
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			// 执行手动添加的普通 BeanFactoryPostProcessor 的 postProcessBeanFactory
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		// 上面BeanFactoryPostProcessor的回调可能又注册了一些类，下面需要再走一遍之前的逻辑
		// 这边可能是scan扫描到的一些 BeanFactoryPostProcessor 类
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		// 根据不同的优先级，分成三类
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			// 上面处理 BeanDefinitionRegistryPostProcessor 的时候已经处理过，这里不再重复处理
			// 因为 BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		/**
		 * 下面的逻辑跟上面是一致的，先处理实现了 PriorityOrdered 接口的
		 * 再处理实现了 Ordered 接口的
		 * 最后处理普通的 BeanFactoryPostProcessor
		 */
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}

		// 里面会调用 EventListenerMethodProcessor#postProcessBeanFactory方法，获取EventListenerFactory 类型的 Bean
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		// 因为各种BeanFactoryPostProcessor可能修改了BeanDefinition
		// 所以这里需要清除缓存，需要的时候再通过merge的方式获取
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		// 获取所有的 BeanPostProcessor 的 beanName
		// 这些 beanName 都已经全部加载到容器中去，但是没有实例化
		// 注意：此处只会拿到Bean的定义信息
		// 已经被实例化的Bean最终都会调用`beanFactory.addBeanPostProcessor`而缓存在AbstractBeanFactory的字段：beanPostProcessors里，它是个CopyOnWriteArrayList
		// 更重要的是：最终最终所有的BeanPostProcessor的执行都会从这个List里面拿出来执行
		// 所以这一步很关键：那就是按照顺序，把`BeanPostProcessor`们都实例化好，然后添加进List里
		// 因此顺序是关键~~~~~如果某些Bean提前被实例化，它就很有可能不能被所有的`BeanPostProcessor`处理到了
		// 这也是我们BeanPostProcessorChecker的作用，它就是检查这个然后输出日志的~
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		// 记录所有的beanProcessor数量
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;

		// 注册 BeanPostProcessorChecker，它主要是用于在 BeanPostProcessor 实例化期间记录日志
		// 当 Spring 中高配置的后置处理器还没有注册就已经开始了 bean 的实例化过程，这个时候便会打印 BeanPostProcessorChecker 中的内容
		// 把BeanPostProcessorChecker加进去，它其实就是做了一个检查而已~~~~~~~输出一个info日志~
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		// PriorityOrdered 保证顺序
		// 定义不同的变量用于区分实现PriorityOrdered, Ordered 接口的 BeanPostProcessor 和普通的BeanPostProcessor
		// priorityOrderedPostProcessors 存储实现PriorityOrdered 接口的BeanPostProcessor
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// MergedBeanDefinitionPostProcessor, internalPostProcessors 存储Spring内部的BeanPostProcessor
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		// 使用 Ordered 保证顺序, orderedPostProcessorNames 存储实现Ordered接口BeanPostProcessor 的Name
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 没有顺序, 存储普通的BeanPostProcessor 的BeanName
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		// 遍历postProcessorNames
		for (String ppName : postProcessorNames) {
			// 实现PriorityOrdered 接口的BeanPostProcessor 处理
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				// 调用 getBean 获取 bean 实例对象
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				// 如果 pp对应Bean实例也实现了 MergedBeanDefinitionPostProcessor接口，则添加到internalPostProcessors
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				// 有序 Ordered, 实现了Ordered接口， 添加到 orderedPostProcessorNames
				orderedPostProcessorNames.add(ppName);
			}
			else {
				// 无序, 普通的 nonOrderedPostProcessorNames
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		// 第一步，注册所有实现了 PriorityOrdered 的 BeanPostProcessor
		// 先排序
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		// 后注册
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		// 第二步，注册所有实现了 Ordered 的 BeanPostProcessor
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		// 先排序
		sortPostProcessors(orderedPostProcessors, beanFactory);
		// 后注册
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		// 第三步注册所有无序的 BeanPostProcessor
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			// 只要实现了MergedBeanDefinitionPostProcessor接口就放最后
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		// 注册，无需排序
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		// 最后，注册所有的 MergedBeanDefinitionPostProcessor 类型的 BeanPostProcessor (相当于移动到链表的末尾)
		// 排序
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		// 加入ApplicationListenerDetector（探测器）
		// 重新注册 BeanPostProcessor 以检测内部 bean，因为 ApplicationListeners 将其移动到处理器链的末尾
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		// Nothing to sort?
		if (postProcessors.size() <= 1) {
			return;
		}
		// 获得 Comparator 对象
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {// 依赖的 Comparator 对象
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {// 默认 Comparator 对象
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			/**
			 * BeanFactoryPostProcessor 子类 BeanDefinitionRegistryPostProcessor接口 能注册beanDefinition (mybatis整合spring也利用了这个)
			 *  如果是 BeanDefinitionRegistryPostProcessor 这个子类 这边就执行 postProcessBeanDefinitionRegistry
			 *  registryProcessor 有个子类 ConfigurationClassPostProcessor
			 *
			 *  它可以在Spring应用上下文前期先被实例化且回调相关接口方法，向Spring容器注册或移除BeanDefinition甚至可以在get出一个BeanDefinition后直接修改内部属性，让Bean变成你想要的模样
			 */
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					// 如果你的bean被提前加载了，那就会看到这么一条日志
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
