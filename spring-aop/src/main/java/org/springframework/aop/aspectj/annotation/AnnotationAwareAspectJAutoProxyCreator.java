/*
 * Copyright 2002-2017 the original author or authors.
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
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link AspectJAwareAdvisorAutoProxyCreator} subclass that processes all AspectJ
 * annotation aspects in the current application context, as well as Spring Advisors.
 *
 * <p>Any AspectJ annotated classes will automatically be recognized, and their
 * advice applied if Spring AOP's proxy-based model is capable of applying it.
 * This covers method execution joinpoints.
 *
 * <p>If the &lt;aop:include&gt; element is used, only @AspectJ beans with names matched by
 * an include pattern will be considered as defining aspects to use for Spring auto-proxying.
 *
 * <p>Processing of Spring Advisors follows the rules established in
 * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {
	/**
	 * 这是个 BeanPostProcessor, 其主要逻辑在AbstractAutoProxyCreator 中实现
	 * Spring事务的实现也依赖于 AbstractAutoProxyCreator 类，并且逻辑与Aop 的实现基本一致，因为事务的实现的方式也是Aop代理
	 * 初始化后是 {@link AbstractAutoProxyCreator#postProcessAfterInitialization(Object, String)}
	 */

	@Nullable
	private List<Pattern> includePatterns;

	@Nullable
	private AspectJAdvisorFactory aspectJAdvisorFactory;

	@Nullable
	private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;


	/**
	 * Set a list of regex patterns, matching eligible @AspectJ bean names.
	 * <p>Default is to consider all @AspectJ beans as eligible.
	 */
	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<>(patterns.size());
		for (String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}

	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}

	@Override
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.initBeanFactory(beanFactory);
		if (this.aspectJAdvisorFactory == null) {
			this.aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
		}
		this.aspectJAdvisorsBuilder =
				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
	}


	/**
	 * Aop 重写了这个, 但是事务没有
	 * 相较于 Aop 的 ，事务的 InfrastructureAdvisorAutoProxyCreator，不仅没有添加新逻辑(关键逻辑)，还砍掉了动态生成Advisor 的逻辑
	 */
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		/**
		 * 1. super.findCandidateAdvisors(); 调用了父类的 AbstractAdvisorAutoProxyCreator#findCandidateAdvisors 的方法来获取 Advisor
		 * 2. 调用 this.aspectJAdvisorsBuilder.buildAspectJAdvisors() 方法来获取Advisor
		 *
		 * 这两个方法都是为了获取 Advisor，区别在于
		 *
		 * 	super.findCandidateAdvisors(); ： 一般获取的都是通过直接注册的 Advisors。比如事务的，直接通过 @Bean 注入到Spring容器中。
		 *
		 * 	this.aspectJAdvisorsBuilder.buildAspectJAdvisors() ： 主要获取我们通过注解方式动态注册的 Advisors。
		 * 			比如 在 Aop 中根据不同的表达式，每个@Pointcut 注解的切点不同，也就会对不同的Bean起作用，并且对于每个@Pointcut来说都有@Before、@After 等不同的操作，那么每个@Pointcut 以及其对应的操作都会被封装成一个一个的 Advisor 返回。
		 */

		// Add all the Spring advisors found according to superclass rules.
		// 调用父类方法从容器中查找所有的通知器, 根据规则找出实现了Advisor接口的bean, 就是直接获取 容器中的 Advisor 类型的Bean
		List<Advisor> advisors = super.findCandidateAdvisors();
		// Build Advisors for all AspectJ aspects in the bean factory.
		// 从所有切面中解析得到的Advisor对象
		if (this.aspectJAdvisorsBuilder != null) {
			// 在当前的bean工厂中查找带有AspectJ注解的 Aspect bean，并封装成代表他们的Spring Aop Advisor，注入到Spring 中
			/**
			 * 大概流程:
			 * 获取所有beanName，这一步所有在beanFactory中注册的bean都会被提取出来
			 * 遍历所有的beanName, 找出声明AspectJ注解的类，进行进一步处理
			 * 对标记为AspectJ注解的类进行Advisors 提取
			 * 将提取的结果保存到缓存中。
			 */
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		return advisors;
	}

	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		// Previously we setProxyTargetClass(true) in the constructor, but that has too
		// broad an impact. Instead we now override isInfrastructureClass to avoid proxying
		// aspects. I'm not entirely happy with that as there is no good reason not
		// to advise aspects, except that it causes advice invocation to go through a
		// proxy, and if the aspect implements e.g the Ordered interface it will be
		// proxied by that interface and fail at runtime as the advice method is not
		// defined on the interface. We could potentially relax the restriction about
		// not advising aspects in the future.
		return (super.isInfrastructureClass(beanClass) ||
				(this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
	}

	/**
	 * Check whether the given aspect bean is eligible for auto-proxying.
	 * <p>If no &lt;aop:include&gt; elements were used then "includePatterns" will be
	 * {@code null} and all beans are included. If "includePatterns" is non-null,
	 * then one of the patterns must match.
	 */
	protected boolean isEligibleAspectBean(String beanName) {
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (Pattern pattern : this.includePatterns) {
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}


	/**
	 * Subclass of BeanFactoryAspectJAdvisorsBuilderAdapter that delegates to
	 * surrounding AnnotationAwareAspectJAutoProxyCreator facilities.
	 */
	private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {

		public BeanFactoryAspectJAdvisorsBuilderAdapter(
				ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {

			super(beanFactory, advisorFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
		}
	}

}
