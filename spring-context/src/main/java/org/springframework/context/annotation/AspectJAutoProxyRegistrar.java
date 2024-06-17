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

package org.springframework.context.annotation;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Registers an {@link org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 * AnnotationAwareAspectJAutoProxyCreator} against the current {@link BeanDefinitionRegistry}
 * as appropriate based on a given @{@link EnableAspectJAutoProxy} annotation.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableAspectJAutoProxy
 */
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * Register, escalate, and configure the AspectJ auto proxy creator based on the value
	 * of the @{@link EnableAspectJAutoProxy#proxyTargetClass()} attribute on the importing
	 * {@code @Configuration} class.
	 */
	@Override
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		/**
		 *  如有必要，注册AspectJ注释自动代理创建器。这里注册的自动代理创建器Aop实现的核心
		 *  这里之所以说如有必要，是因为在调用该方法时，容器中可能已经创建了一个自动代理创建器，
		 * 	如果这个自动代理创建器优先级更高或者与当前需要创建的自动代理创建器是同一类型，则不需要创建。
		 *
		 * 	注册一个 AnnotationAwareAspectJAutoProxyCreator 类型的bean
		 * 	重点 往下看, 里面注册了 AnnotationAwareAspectJAutoProxyCreator
		 */
		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

		// 为组件AnnotationAwareAspectJAutoProxyCreator添加属性
		// 获取@EnableAspectJAutoProxy注解
		AnnotationAttributes enableAspectJAutoProxy =
				AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			// 如果有这2个属性, 就给BeanDefinition 对应的属性赋值

			// 解析proxyTargetClass属性
			if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
				// 若为true，表示强制指定了要使用CGLIB，那就强制告知到时候使用CGLIB的动态代理方式
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			//  解析exposeProxy属性
			if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
				// 告知，强制暴露Bean的代理对象到AopContext
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

}
